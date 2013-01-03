package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Vector;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Class representing the activity for editing properties and data of a
 * transaction. The layout of this activity contains elements necessary for
 * editing all data of a transaction. There are two kinds of layouts,
 * <code>transaction_edit_nocurrency</code> and <code>transaction_edit</code>,
 * the latter version allows to change the currency of transaction while the
 * former version lacks this ability. Which of these layouts is used depends on
 * the status of <code>supportMultipleCurrencies</code> preference. The options
 * menu of this activity is described in <code>transaction_edit_menu.xml</code>
 * and contains only possibility to delete the transaction.
 * 
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class TransactionEditActivity extends FragmentActivity
		implements
			EditParticipantValueDialog.EditParticipantValueListener,
			WarningDialogWithCheck.WarningDialogClickListener {
	/**
	 * Inflater used to inflate layouts. Used when adding participants to the
	 * list of participants with checkboxes.
	 */
	private LayoutInflater inflater;
	/**
	 * Widget for editing the name of the transaction.
	 */
	private EditText nameEdit = null;
	/**
	 * Widget for editing the note of the transaction.
	 */
	private EditText noteEdit = null;
	/**
	 * Widget for editing the amount of the transaction.
	 */
	private EditText amountEdit = null;
	/**
	 * <code>Button</code> for setting date of the transaction.
	 */
	private Button dateButton = null;
	/**
	 * <code>CheckBox</code> for selecting the type of transaction (split
	 * evenly/general).
	 */
	private CheckBox splitEvenlyCheck = null;
	/**
	 * Array with participants of the transaction. Usually contains all
	 * participants of the event sorted by their names, this is the order in
	 * which they appear in the list of participants with checkboxes.
	 */
	private Vector<ClearingPerson> participants = null;
	/**
	 * Adapter for populating the <code>Spinner</code> for choosing
	 * receiver/payee of the transaction.
	 */
	private ArrayAdapter<ClearingPerson> receiversAdapter = null;
	/**
	 * <code>Spinner</code> for choosing receiver/payee of the transaction.
	 */
	private Spinner receiverSpinner = null;
	/**
	 * List of the participants of the transaction. Each name of participant is
	 * accompanied with a CheckBox for choosing whether he/she participates in
	 * this transaction and a label with value of the participant.
	 */
	private LinearLayout participantsList = null;
	/**
	 * Id of the enclosing event.
	 */
	private long myEventId = -1;
	/**
	 * Id of the transaction.
	 */
	private long myTransactionId = -1;
	/**
	 * Transaction object.
	 */
	private ClearingTransaction myTransaction = null;
	/**
	 * Connection to the database.
	 */
	private GCDatabase db = null;
	/**
	 * A special no receiver person. In the <code>Spinner</code> for choosing
	 * the receiver this value represents the possibility in which no receiver
	 * is chosen.
	 */
	private ClearingPerson noReceiver = null;
	/**
	 * The application object. For accessing the global and shared data and
	 * preferences.
	 */
	private final GroupClearingApplication myApp = GroupClearingApplication
			.getInstance();
	/**
	 * <code>TextView</code> for showing the total balance of the transaction.
	 */
	private TextView balanceText = null;
	/**
	 * <code>Spinner</code> for choosing the currency of the transaction. Only
	 * shown if <code>supportMultipleCurrencies</code> preference is true.
	 */
	private Spinner currencySpinner = null;
	/**
	 * Event object.
	 */
	private ClearingEvent myEvent = null;
	/**
	 * Line with widgets for editing the rate. Shown only if currency of this
	 * transaction is different with currency of the event and
	 * <code>supportMultipleCurrencies</code> is true.
	 */
	private LinearLayout rateEditorLine = null;
	/**
	 * <code>TextView</code> for displaying the left currency in rate editor.
	 */
	private TextView rateLeftText = null;
	/**
	 * <code>TextView</code> for displaying the right currency in rate editor.
	 */
	private TextView rateRightText = null;
	/**
	 * Edit field for editing the rate value.
	 */
	private EditText rateEdit = null;

	/**
	 * Id of the dialog for changing the date of transaction.
	 */
	private static final int DATE_PICK_DIALOG_ID = 0;
	/**
	 * Listener to the dialog for changing the date of transaction.
	 */
	private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			onDateChanged(year, monthOfYear, dayOfMonth);
		}
	};

	/**
	 * Class implementing listener to events of <code>Spinner</code> for
	 * choosing the receiver/payee of the transaction.
	 * 
	 * @author Strašlivý Uragán
	 * @version 1.0
	 * @since 1.0
	 */
	public class ReceiverSpinnerOnItemSelected
			implements
				OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			onReceiverChanged(pos, id);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			onReceiverChanged(0, -1);
		}
	}

	/**
	 * Class wrapping an item of the list of participants.
	 * 
	 * @author Strašlivý Uragán
	 * @version 1.0
	 * @since 1.0
	 */
	public class ParticipantItemWrapper {
		/**
		 * Base view of the item. Is expected to be layout defined in
		 * <code>trans_participants_list_item.xml</code>.
		 */
		private View base;
		/**
		 * <code>CheckBox</code> for selecting the participant. In case of split
		 * evenly transaction this check box determines whether a person is or
		 * is not participating in this transaction.
		 */
		private CheckBox check;
		/**
		 * <code>TextView</code> for showing balance of the participant in this
		 * transaction.
		 */
		private TextView participantBalanceText;
		/**
		 * Balance of the participant in this transaction.
		 */
		private BigDecimal balance = BigDecimal.ZERO;
		/**
		 * Id of the participant being shown in this item.
		 */
		private long participantId = 0;
		/**
		 * Position of this item within the list of participants.
		 */
		private int position = 0;

		/**
		 * Creates new wrapper with given data.
		 * 
		 * @param v
		 *            Base view of the item.
		 * @param aPosition
		 *            Position of the item within the list.
		 * @param aParticipantId
		 *            Id of the participant.
		 * @param name
		 *            Name of the participant.
		 * @param state
		 *            State of the <code>CheckBox</code> determining whether
		 *            given person is really participating in this transaction.
		 * @param aBalance
		 *            Balance value of the participant.
		 */
		public ParticipantItemWrapper(View v, int aPosition,
				long aParticipantId, String name, boolean state,
				BigDecimal aBalance) {
			base = v;
			participantId = aParticipantId;
			position = aPosition;
			check = (CheckBox) base.findViewById(R.id.trans_part_name);
			setCheckState(state);
			check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton button,
						boolean isChecked) {
					onParticipantCheckedChange(position, participantId,
							isChecked);
				}
			});
			View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					onParticipantLongClick(position, participantId);
					return true;
				}
			};
			base.setOnLongClickListener(longClickListener);
			// check.setOnLongClickListener(longClickListener);
			balance = aBalance;
			participantBalanceText = (TextView) base
					.findViewById(R.id.trans_part_balance);
			check.setText(name);
			setBalance(aBalance);
		}

		/**
		 * Returns the state of the <code>CheckBox</code> <code>check</code>.
		 * I.e. the <code>CheckBox</code> determining if the person is really
		 * participating in this transaction.
		 * 
		 * @return State of the <code>CheckBox</code> determining if the person
		 *         is really participating in this transaction.
		 */
		boolean getCheckState() {
			return check.isChecked();
		}

		/**
		 * Sets the new state of the <code>CheckBox</code> <code>check</code>.
		 * I.e. the <code>CheckBox</code> determining if the person is really
		 * participating in this transaction.
		 * 
		 * @param state
		 *            New state of the <code>CheckBox</code> determining if the
		 *            person is really participating in this transaction.
		 */
		void setCheckState(boolean state) {
			check.setChecked(state);
		}

		/**
		 * Sets new balance of the participant.
		 * 
		 * @param aBalance
		 *            New balance of the participant.
		 */
		void setBalance(BigDecimal balance) {
			this.balance = balance;
			participantBalanceText.setText(myApp.formatCurrencyValueWithSymbol(
					balance, myTransaction.getCurrency()) + " ");
			if (balance.signum() > 0) {
				participantBalanceText
						.setTextColor(android.graphics.Color.GREEN);
			} else if (balance.signum() < 0) {
				participantBalanceText.setTextColor(android.graphics.Color.RED);
			} else {
				participantBalanceText.setTextColor(getResources().getColor(
						android.R.color.primary_text_dark));
			}
		}

		/**
		 * Returns the balance of the participant in this transaction.
		 * 
		 * @return Balance of the participant in this transaction.
		 */
		BigDecimal getBalance() {
			return balance;
		}

		/**
		 * Id of the participant.
		 * 
		 * @return Id of the participant.
		 */
		public long getParticipantId() {
			return participantId;
		}
	}

	/**
	 * Tag for tagging an editor of participant value in a fragment manager.
	 */
	public final static String EDIT_PARTICIPANT_VALUE_DLG_TAG = "edit_participant_value_dialog";
	/**
	 * Tag for tagging split warning tag.
	 */
	public final static String SPLIT_WARNING_TAG = "split_warning_dialog";
	/**
	 * Tag of parameter with event id for passing through <code>Intent</code>
	 * extra information.
	 */
	public final static String EVENT_ID_PARAM_TAG = "cz.su.GroupClearing.EventId";
	/**
	 * Tag of parameter with transaction id for passing through
	 * <code>Intent</code> extra information.
	 */
	public final static String TRANSACTION_ID_PARAM_TAG = "cz.su.GroupClearing.TransactionId";

	/**
	 * Array of wrappers of items in the list of participants.
	 */
	Vector<ParticipantItemWrapper> participantWrappers = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (myApp.getSupportMultipleCurrencies()) {
			setContentView(R.layout.transaction_edit);
		} else {
			setContentView(R.layout.transaction_edit_nocurrency);
		}
		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		balanceText = (TextView) findViewById(R.id.transaction_balance);
		nameEdit = (EditText) findViewById(R.id.transaction_name_edit);
		nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					onNameChanged();
					return true;
				}
				return false;
			}
		});
		nameEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					onNameChanged();
				}
			}
		});
		noteEdit = (EditText) findViewById(R.id.transaction_note_edit);
		noteEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					onNoteChanged();
					return true;
				}
				return false;
			}
		});
		noteEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					onNoteChanged();
				}
			}
		});
		amountEdit = (EditText) findViewById(R.id.transaction_amount_edit);
		amountEdit
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							onAmountChanged();
						}
						return false;
					}
				});
		amountEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					onAmountChanged();
				}
			}
		});
		dateButton = (Button) findViewById(R.id.transaction_date_button);
		splitEvenlyCheck = (CheckBox) findViewById(R.id.transaction_split_check);
		receiverSpinner = (Spinner) findViewById(R.id.transaction_receiver_spinner);
		receiversAdapter = new ArrayAdapter<ClearingPerson>(this,
				android.R.layout.simple_spinner_item);
		receiversAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		receiverSpinner.setAdapter(receiversAdapter);
		receiverSpinner
				.setOnItemSelectedListener(new ReceiverSpinnerOnItemSelected());
		participantsList = (LinearLayout) findViewById(R.id.transaction_participants_list);
		myEventId = getIntent().getLongExtra(EVENT_ID_PARAM_TAG, -1);
		myTransactionId = getIntent()
				.getLongExtra(TRANSACTION_ID_PARAM_TAG, -1);
		db = new GCDatabase(this);
		myEvent = db.readEventWithId(myEventId);
		noReceiver = new ClearingPerson(-1);
		noReceiver.setName(getString(R.string.transaction_no_receiver));
		if (myApp.getSupportMultipleCurrencies()) {
			currencySpinner = (Spinner) findViewById(R.id.trans_currency_spinner);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter
					.createFromResource(this, R.array.currency_names,
							android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			currencySpinner.setAdapter(adapter);
			currencySpinner
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent,
								View view, int pos, long id) {
							onCurrencySelected(pos, id);
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {
						}
					});
			rateEditorLine = (LinearLayout) findViewById(R.id.trans_rate_editor);
			rateLeftText = (TextView) findViewById(R.id.trans_re_left_text);
			rateRightText = (TextView) findViewById(R.id.trans_re_right_text);
			rateEdit = (EditText) findViewById(R.id.trans_re_edit);
			rateEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						onRateChanged();
					}
					return false;
				}
			});
			rateEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						onRateChanged();
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (db == null) {
			db = new GCDatabase(this);
		}
		if (myTransactionId < 0) {
			try {
				myTransaction = db.createNewTransaction(myEventId);
			} catch (GCEventDoesNotExistException e) {
				return;
			}
		} else {
			myTransaction = db.readTransactionWithId(myTransactionId);
		}
		nameEdit.setText(myTransaction.getName());
		nameEdit.clearFocus();
		noteEdit.setText(myTransaction.getNote());
		amountEdit.setText(myApp.formatCurrencyValue(myTransaction.getAmount(),
				myTransaction.getCurrency()));
		DateFormat df = DateFormat.getDateInstance();
		Date date = myTransaction.getDate();
		if (date != null) {
			dateButton.setText(df.format(date));
		} else {
			dateButton.setText("--");
		}
		splitEvenlyCheck.setChecked(myTransaction.getSplitEvenly());
		amountEdit.setEnabled(myTransaction.getSplitEvenly());
		receiverSpinner.setEnabled(myTransaction.getSplitEvenly());
		if (myApp.getSupportMultipleCurrencies()) {
			CurrencyList cl = CurrencyList.getInstance();
			Currency cur = myTransaction.getCurrency();
			String currencyCode = cur.toString();
			currencySpinner.setSelection(cl.getPosition(currencyCode));
			rateEditorLine.setVisibility(cur.equals(myEvent
					.getDefaultCurrency()) ? View.GONE : View.VISIBLE);
			String leftText = String.format(
					getString(R.string.re_currency_left_fmt), myTransaction
							.getCurrency().toString());
			rateLeftText.setText(leftText);
			String rightText = String.format(
					getString(R.string.re_currency_right_fmt), myEvent
							.getDefaultCurrency().toString());
			rateRightText.setText(rightText);
			rateEdit.setText(myTransaction.getRate().toString());
		}
		refreshParticipants();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		onNameChanged();
		onNoteChanged();
		onAmountChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
			db = null;
		}
	}

	/**
	 * Reacts on click of the transaction date button. Shows dialog for picking
	 * the date of transaction.
	 * 
	 * @param v
	 *            The <code>View</code> of button.
	 */
	public void onTransactionDateButtonClicked(View v) {
		showDialog(DATE_PICK_DIALOG_ID);
	}

	/**
	 * Reads the list of participants from the database. It populates the array
	 * <code>participantWrappers</code> of participant item wrappers. And sets
	 * new transaction balance by calling <code>setBalanceText()</code>.
	 */
	public void refreshParticipants() {
		participants = db.readParticipantsOfEvent(myEventId,
				GCDatabase.ComputeBalance.DO_NOT_COMPUTE);
		participantWrappers = new Vector<ParticipantItemWrapper>(
				participants.size());
		receiversAdapter.clear();
		receiversAdapter.add(noReceiver);
		int selectedPosition = 0;
		participantsList.removeAllViews();
		for (int i = 0; i < participants.size(); ++i) {
			ClearingPerson participant = participants.get(i);
			if (participant.getId() == myTransaction.getReceiverId()) {
				selectedPosition = i + 1;
			}
			receiversAdapter.add(participants.get(i));
			View rowView = inflater.inflate(
					R.layout.trans_participants_list_item, null);
			BigDecimal value = myTransaction.getParticipantValue(participant
					.getId());
			ParticipantItemWrapper wrapper = new ParticipantItemWrapper(
					rowView, i, participant.getId(), participant.getName(),
					myTransaction.isParticipantMarked(participant.getId()),
					value);
			participantWrappers.add(wrapper);
			participantsList.addView(rowView);
		}
		receiverSpinner.setSelection(selectedPosition);
		setBalanceText();
	}

	/**
	 * Sets new transaction balance text. Sets the text of
	 * <code>balanceText</code> to value obtained from
	 * <code>ClearingTransaction.getBalance()</code>. The color of the balance
	 * text is set to red, green, or default text color depending on the signum
	 * of balance value.
	 */
	public void setBalanceText() {
		balanceText.setText(myApp.formatCurrencyValueWithSymbol(
				myTransaction.getBalance(), myTransaction.getCurrency())
				+ " ");
		if (myTransaction.getBalance().signum() > 0) {
			balanceText.setTextColor(android.graphics.Color.GREEN);
		} else if (myTransaction.getBalance().signum() < 0) {
			balanceText.setTextColor(android.graphics.Color.RED);
		} else {
			balanceText.setTextColor(getResources().getColor(
					android.R.color.primary_text_dark));
		}
	}

	/**
	 * Called when name was modified. Stores the new value in the database and
	 * the transaction object.
	 */
	public void onNameChanged() {
		String newName = nameEdit.getText().toString();
		if (newName.compareTo(myTransaction.getName()) != 0) {
			myTransaction.setName(newName);
			db.updateTransactionName(myTransaction);
		}
	}

	/**
	 * Called when note was modified. Stores the new value in the database and
	 * the transaction object.
	 */
	public void onNoteChanged() {
		String newNote = noteEdit.getText().toString();
		if (newNote.compareTo(myTransaction.getNote()) != 0) {
			myTransaction.setNote(newNote);
			db.updateTransactionNote(myTransaction);
		}
	}

	/**
	 * Recomputes the values of participants and visualizes the changes.
	 * Recompute is done through
	 * <code>ClearingTransaction.recomputeAndSaveChanges(GCDatabase db)</code>.
	 * Values in participant item wrappers are then set accordingly. Also
	 * receiver is found again as it could have changed during the recompute.
	 * 
	 * @see cz.su.GroupClearing.ClearingTransaction#recomputeAndSaveChanges(cz.su.GroupClearing.GCDatabase)
	 */
	private void recomputeValues() {
		myTransaction.recomputeAndSaveChanges(db);
		for (ParticipantItemWrapper wrapper : participantWrappers) {
			long participantId = wrapper.getParticipantId();
			wrapper.setBalance(myTransaction.getParticipantValue(participantId));
			wrapper.setCheckState(myTransaction
					.isParticipantMarked(participantId));
		}
		amountEdit.setText(myApp.formatCurrencyValue(myTransaction.getAmount(),
				myTransaction.getCurrency()));
		int selectedPosition = 0;
		if (myTransaction.getReceiverId() >= 0) {
			while (selectedPosition < participants.size()
					&& participants.get(selectedPosition).getId() != myTransaction
							.getReceiverId()) {
				++selectedPosition;
			}
			if (selectedPosition == participants.size()) {
				selectedPosition = 0;
			} else {
				++selectedPosition;
			}
		}
		receiverSpinner.setSelection(selectedPosition);
		setBalanceText();
	}

	/**
	 * Called when the amount changed in the <code>EditText</code>. New amount
	 * value is parsed, possibly stored and then the values are recomputed by
	 * calling <code>recomputeValues()</code>.
	 */
	public void onAmountChanged() {
		try {
			BigDecimal newAmount = myApp.parseCurrencyValue(amountEdit
					.getText().toString(), myTransaction.getCurrency());
			if (newAmount.compareTo(myTransaction.getAmount()) != 0) {
				myTransaction.setAmount(newAmount);
				db.updateTransactionAmount(myTransaction);
				recomputeValues();
			}
		} catch (NumberFormatException e) {
		}
		amountEdit.setText(myApp.formatCurrencyValue(myTransaction.getAmount(),
				myTransaction.getCurrency()));

	}

	/**
	 * Called when user picks new date of transaction. New date is stored and
	 * displayed.
	 * 
	 * @param year
	 *            New year part of the date.
	 * @param monthOfYear
	 *            New month part of the date.
	 * @param day
	 *            New day part of the date.
	 */
	public void onDateChanged(int year, int monthOfYear, int day) {
		if (day != myTransaction.getDayOfMonth()
				|| monthOfYear != myTransaction.getMonth()
				|| year != myTransaction.getYear()) {
			myTransaction.setDate(year, monthOfYear, day);
			DateFormat df = DateFormat.getDateInstance();
			Date date = myTransaction.getDate();
			dateButton.setText(df.format(date));
			db.updateTransactionDate(myTransaction);
		}
	}

	/**
	 * Shows given dialog. Used only for date picking dialog.
	 * 
	 * @param id
	 *            Id of the dialog to be created.
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DATE_PICK_DIALOG_ID :
				return new DatePickerDialog(this, dateSetListener,
						myTransaction.getYear(), myTransaction.getMonth(),
						myTransaction.getDayOfMonth());
		}
		return null;
	}

	/**
	 * Called when receiver was changed. New receiver is specified by its
	 * position within the list provided by <code>receiversAdapter</code>. New
	 * receiver is stored and the values are recomputed by calling
	 * <code>recomputeValues()</code> function.
	 * 
	 * @param position
	 * @param id
	 */
	public void onReceiverChanged(int position, long id) {
		ClearingPerson receiver = receiversAdapter.getItem(position);
		if (receiver.getId() != myTransaction.getReceiverId()) {
			myTransaction.setReceiverId(receiver.getId());
			db.updateTransactionReceiverId(myTransaction);
		}
		recomputeValues();
	}

	/**
	 * Called when the status of split evenly check box changed. In case
	 * <code>noSplitChangeWarning</code> preference value is true or transaction
	 * has only zero values <code>onSplitEvenlyConfirmed(true)</code> is called
	 * directly to finish the change. Otherwise split warning dialog is
	 * presented to the user. It is a dialog of type
	 * <code>WarningDialogWithCheck</code> and is shown using
	 * <code>FragmentManeger</code>.
	 * 
	 * @param v
	 *            View of the <code>CheckBox</code> in question.
	 * @see su.cz.GroupClearing.WarningDialogWithCheck
	 */
	public void onSplitEvenlyChanged(View v) {
		if (myApp.getNoSplitChangeWarning()
				|| !myTransaction.hasNonzeroValues()) {
			onWarningConfirmed(true);
		} else {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag(
					SPLIT_WARNING_TAG);
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);
			WarningDialogWithCheck dialog = new WarningDialogWithCheck();
			Bundle bundle = new Bundle();
			bundle.putString(WarningDialogWithCheck.MESSAGE_TAG, getResources()
					.getString(R.string.split_warning));
			dialog.setArguments(bundle);
			dialog.show(ft, SPLIT_WARNING_TAG);
		}
	}

	/**
	 * Called when split evenly change was confirmed. The <code>checked</code>
	 * parameter determines new value of <code>noSplitChangeWarning</code>
	 * preference value. The new status of split evenly flag is stored in the
	 * transaction and the values of transaction are reseted. The values are
	 * then updated in the database as well. In the end
	 * <code>recomputeValues</code> is called to finish the change.
	 * 
	 * @param checked
	 *            New status of <code>noSplitChangeWarning</code> preference
	 *            value.
	 */
	@Override
	public void onWarningConfirmed(boolean checked) {
		myApp.setNoSplitChangeWarning(checked);
		if (splitEvenlyCheck.isChecked() != myTransaction.getSplitEvenly()) {
			myTransaction.setSplitEvenly(splitEvenlyCheck.isChecked());
			db.updateTransactionSplitEvenly(myTransaction);
			myTransaction.resetValues();
			db.resetTransactionParticipantsValues(myTransaction,
					myTransaction.getSplitEvenly());
			db.updateTransactionAmount(myTransaction);
			db.updateTransactionReceiverId(myTransaction);
			amountEdit.setEnabled(myTransaction.getSplitEvenly());
			receiverSpinner.setEnabled(myTransaction.getSplitEvenly());
			recomputeValues();
		}
	}

	/**
	 * Called when split evenly change was cancelled by user.
	 * <code>CheckBox</code> <code>splitEvenlyCheck</code> status is changed to
	 * the original value.
	 * 
	 * @param checked
	 *            New status of <code>noSplitChangeWarning</code> preference
	 *            value.
	 */
	@Override
	public void onWarningCancelled(boolean checked) {
		myApp.setNoSplitChangeWarning(checked);
		splitEvenlyCheck.setChecked(myTransaction.getSplitEvenly());
	}

	/**
	 * Called when the status of a CheckBox within a participant item changed.
	 * If this is a split evenly transaction, participants mark status is
	 * changed in the transaction. If the transaction is not split evenly, then
	 * value editor is opened (by calling <code>openValueEditor(int,long)</code>
	 * for changing the value of participant.
	 * 
	 * @param position
	 *            Position of the item of participant within the list.
	 * @param participantId
	 *            Id of the participant.
	 * @param isChecked
	 *            The new status of the <code>CheckBox</code>.
	 */
	public void onParticipantCheckedChange(int position, long participantId,
			boolean isChecked) {
		if (isChecked != myTransaction.isParticipantMarked(participantId)) {
			if (!myTransaction.getSplitEvenly()) {
				openValueEditor(position, participantId);
			} else {
				myTransaction.setParticipantMark(participantId, isChecked);
				recomputeValues();
			}
		}
	}

	/**
	 * Shows the dialog for changing the value of participant within the
	 * transaction. It shows the <code>EditParticipantValueDialog</code> using
	 * <code>FragmentManager</code>.
	 * 
	 * @param position
	 *            Position of the participant within the list.
	 * @param participantId
	 *            Id of the participant.
	 * @see cz.su.GroupClearing.EditParticipantValueDialog
	 */
	public void openValueEditor(int position, long participantId) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(
				EDIT_PARTICIPANT_VALUE_DLG_TAG);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		ClearingPerson participant = participants.get(position);
		EditParticipantValueDialog editParticipantValueDialog = new EditParticipantValueDialog();
		Bundle bundle = new Bundle();
		bundle.putString(EditParticipantValueDialog.NAME_TAG,
				participant.getName());
		bundle.putLong(EditParticipantValueDialog.ID_TAG, participantId);
		bundle.putInt(EditParticipantValueDialog.POSITION_TAG, position);
		BigDecimal value = myTransaction.getParticipantValue(participantId);
		bundle.putString(EditParticipantValueDialog.VALUE_TAG, value.toString());
		BigDecimal precomputed = value.add(myTransaction.getBalance());
		bundle.putString(EditParticipantValueDialog.PRECOMPUTED_TAG,
				precomputed.toString());
		bundle.putString(EditParticipantValueDialog.CURRENCY_TAG, myTransaction
				.getCurrency().toString());
		editParticipantValueDialog.setArguments(bundle);
		editParticipantValueDialog.show(ft, EDIT_PARTICIPANT_VALUE_DLG_TAG);
	}

	/**
	 * Opens a value editor for participant after the corresponding item in the
	 * list was long clicked. Calls <code>openValueEditor(int,long)</code>.
	 * 
	 * @param position
	 *            Position of the participant within the list.
	 * @param participantId
	 *            Id of the participant.
	 */
	public void onParticipantLongClick(int position, long participantId) {
		if (!myTransaction.getSplitEvenly()) {
			openValueEditor(position, participantId);
		}
	}

	/**
	 * Finishes editing the value of participant after user clicked on OK button
	 * in the editor dialog. Stores the new value in the transaction object
	 * <code>myTransaction</code> and in the database. Updates the value text
	 * and balance text in the layout.
	 * 
	 * @param position
	 *            Position of the participant within the list.
	 * @param participantId
	 *            Id of the participant.
	 * @param value
	 *            New value of the participant.
	 */
	@Override
	public void onValueEditorOK(int position, long participantId,
			BigDecimal value) {
		myTransaction.setParticipantValue(participantId, value);
		db.updateTransactionParticipantValue(myTransaction, participantId);
		db.updateTransactionSplitEvenly(myTransaction);
		db.updateTransactionAmount(myTransaction);
		amountEdit.setText(myApp.formatCurrencyValue(myTransaction.getAmount(),
				myTransaction.getCurrency()));
		ParticipantItemWrapper wrapper = participantWrappers.get(position);
		wrapper.setCheckState(myTransaction.isParticipantMarked(participantId));
		wrapper.setBalance(myTransaction.getParticipantValue(participantId));
		setBalanceText();
		splitEvenlyCheck.setChecked(myTransaction.getSplitEvenly());
	}

	/**
	 * Called when user clicke on Cancel button in the value editor dialog.
	 * 
	 * @param position
	 *            Position of the participant within the list.
	 * @param participantId
	 *            Id of the participant.
	 */
	@Override
	public void onValueEditorCancelled(int position, long participantId) {
		ParticipantItemWrapper wrapper = participantWrappers.get(position);
		wrapper.setCheckState(myTransaction.isParticipantMarked(participantId));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.transaction_edit_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_trans_delete : {
				db.deleteTransactionWithId(myTransactionId);
				finish();
				return true;
			}
			default :
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Called when new currency was selected in the <code>Spinner</code>.
	 * Updates the new currency in the <code>myTransaction</code> object and in
	 * the database. Rate editor line is updated and default rate is retrieved
	 * from the database. In the end values are recomputed using
	 * <code>recomputeValues</code> function. The currencies are accessed via
	 * <code>CurrencyList</code> instance.
	 * 
	 * @param position
	 *            Position of new currency within the list.
	 * @param id
	 *            Id of the new transaction (this is not used).
	 */
	void onCurrencySelected(int position, long id) {
		CurrencyList cl = CurrencyList.getInstance();
		Currency chosenCurrency = cl.getCurrency(position);
		Currency oldCurrency = myTransaction.getCurrency();
		if (oldCurrency == null || !oldCurrency.equals(chosenCurrency)) {
			myTransaction.setCurrency(chosenCurrency);
			db.updateTransactionCurrency(myTransaction);
			rateEditorLine.setVisibility(chosenCurrency.equals(myEvent
					.getDefaultCurrency()) ? View.GONE : View.VISIBLE);
			String leftText = String.format(
					getString(R.string.re_currency_left_fmt), myTransaction
							.getCurrency().toString());
			rateLeftText.setText(leftText);
			BigDecimal rate = db.getDefaultRate(chosenCurrency,
					myEvent.getDefaultCurrency());
			myTransaction.setRate(rate);
			rateEdit.setText(rate.toString());
			recomputeValues();
		}
	}

	/**
	 * Called when rate text was changed in <code>rateEdit</code>
	 * <code>EditText</code>. Rate is updated in <code>myTransaction</code>
	 * object and in the database, if bad value is contained in
	 * <code>rateEdit</code>, it is changed back to the value stored in
	 * <code>myTransaction</code>.
	 */
	public void onRateChanged() {
		String rateText = rateEdit.getText().toString();
		rateText = rateText.replace(',', '.');
		try {
			BigDecimal newRate = new BigDecimal(rateText);
			myTransaction.setRate(newRate);
			db.updateTransactionRate(myTransaction);
			db.setDefaultRate(myTransaction.getCurrency(),
					myEvent.getDefaultCurrency(), newRate);
		} catch (NumberFormatException e) {
			rateEdit.setText(myTransaction.getRate().toString());
		}
	}
}
