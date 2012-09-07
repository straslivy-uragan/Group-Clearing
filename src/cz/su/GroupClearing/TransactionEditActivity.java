package cz.su.GroupClearing;

import java.text.DateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Vector;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.view.ViewGroup;
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

public class TransactionEditActivity extends FragmentActivity {
	private LayoutInflater inflater;
	private EditText nameEdit = null;
	private EditText noteEdit = null;
	private EditText amountEdit = null;
	private Button dateButton = null;
	private CheckBox splitEvenlyCheck = null;
	private Vector<ClearingPerson> participants = null;
	private ArrayAdapter<ClearingPerson> receiversAdapter = null;
	private Spinner receiverSpinner = null;
	private LinearLayout participantsList = null;
	private long myEventId = -1;
	private long myTransactionId = -1;
	private ClearingTransaction myTransaction = null;
	private GCDatabase db = null;
	private ClearingPerson noReceiver = null;
	private GroupClearingApplication myApp = null;
	private TextView balanceText = null;
    private Spinner currencySpinner = null;
    private Button rateButton = null;
    private ClearingEvent myEvent = null;

	private static final int DATE_PICK_DIALOG_ID = 0;

	private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			onDateChanged(year, monthOfYear, dayOfMonth);
		}
	};

	public class ReceiverSpinnerOnItemSelected implements
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

	public class ParticipantItemWrapper {
		private View base;
		private CheckBox check;
		private TextView participantBalanceText;
		private long balance = 0;
		private long participantId = 0;
		private int position = 0;

		public ParticipantItemWrapper(View v, int aPosition,
				long aParticipantId, String name, boolean state, long aBalance) {
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

		boolean getCheckState() {
			return check.isChecked();
		}

		void setCheckState(boolean state) {
			check.setChecked(state);
		}

		void setBalance(long aBalance) {
			balance = aBalance;
			participantBalanceText.setText(myApp.formatCurrencyValueWithSymbol(
					balance, myTransaction.getCurrency()) + " ");
			if (balance > 0) {
				participantBalanceText
						.setTextColor(android.graphics.Color.GREEN);
			} else if (balance < 0) {
				participantBalanceText.setTextColor(android.graphics.Color.RED);
			} else {
				participantBalanceText.setTextColor(getResources().getColor(
						android.R.color.primary_text_dark));
			}
		}

		long getBalance() {
			return balance;
		}

		public long getParticipantId() {
			return participantId;
		}
	}

	public class EditParticipantValueDialog extends DialogFragment {
		String name = "";
		long value = 0;
		TextView nameTextView = null;
		EditText valueEdit = null;
		long participantId = 0;
		int position = 0;

		public EditParticipantValueDialog(String aName, int aPosition,
				long anId, long aValue) {
			participantId = anId;
			position = aPosition;
			name = aName;
			value = aValue;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getDialog().setTitle(getString(R.string.app_name));
			View v = inflater.inflate(R.layout.participant_value_edit,
					container, false);
			nameTextView = (TextView) v.findViewById(R.id.pev_name);
			if (name != null) {
				nameTextView.setText(name);
			}
			valueEdit = (EditText) v.findViewById(R.id.pev_value);
			valueEdit.setText(myApp.formatCurrencyValue(value,
					myTransaction.getCurrency()));
			Button okButton = (Button) v.findViewById(R.id.pev_ok);
			okButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onOkButtonClicked(v);
				}
			});
			Button cancelButton = (Button) v.findViewById(R.id.pev_cancel);
			cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onCancelButtonClicked(v);
				}
			});
			Button computeButton = (Button) v.findViewById(R.id.pev_compute);
			computeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onComputeButtonClicked(v);
				}
			});
			return v;
		}

		public void onOkButtonClicked(View v) {
			dismiss();
			try {
				value = myApp.parseCurrencyValue(
						valueEdit.getText().toString(),
						myTransaction.getCurrency());
				onValueEditorOK(position, participantId, value);
			} catch (GCSyntaxException e) {
				onValueEditorCancelled(position, participantId);
			}
		}

		public void onCancelButtonClicked(View v) {
			dismiss();
			onValueEditorCancelled(position, participantId);
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);
			onValueEditorCancelled(position, participantId);
		}

		public void onComputeButtonClicked(View v) {
			valueEdit.setText(myApp.formatCurrencyValue(
					value + myTransaction.getBalance(),
					myTransaction.getCurrency()));
		}
	}
	
    public class EditRateDialog extends DialogFragment {
        Currency left = null;
        Currency right = null;
        double rate = 0.0;
        EditText rateEdit = null;
		
		public EditRateDialog(Currency left, Currency right, double rate) {
            this.left = left;
            this.right = right;
            this.rate = rate;
        }

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getDialog().setTitle(getString(R.string.app_name));
			View v = inflater.inflate(R.layout.rate_edit,
					container, false);
            TextView leftText = (TextView) v.findViewById(R.id.re_left_text);
            String text = String.format(getString(R.string.re_currency_left_fmt),
                    left.toString());
            leftText.setText(text);
            TextView rightText = (TextView) v.findViewById(R.id.re_right_text);
            text = String.format(getString(R.string.re_currency_right_fmt),
                    right.toString());
            rightText.setText(text);
			rateEdit = (EditText) v.findViewById(R.id.re_edit);
			rateEdit.setText(Double.toString(rate));

			Button okButton = (Button) v.findViewById(R.id.re_ok_button);
			okButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onOkButtonClicked(v);
				}
			});
			Button cancelButton = (Button) v.findViewById(R.id.re_cancel_button);
			cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onCancelButtonClicked(v);
				}
			});
			Button saveDefaultButton = (Button) v.findViewById(R.id.re_save_default_button);
			saveDefaultButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onSaveDefaultButtonClicked(v);
				}
			});
			return v;
		}

		public void onOkButtonClicked(View v) {
			dismiss();
            String rateString = rateEdit.getText().toString();
            rateString.replace(',', '.');
            try {
                rate = Double.parseDouble(rateString);
                onRateEditOK(rate);
            } catch (NumberFormatException e) {
                onRateEditCancel();
            }
		}

		public void onCancelButtonClicked(View v) {
			dismiss();
            onRateEditCancel();
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);
            onRateEditCancel();
		}

		public void onSaveDefaultButtonClicked(View v) {
            String rateString = rateEdit.getText().toString();
            rateString.replace(',', '.');
            try {
                rate = Double.parseDouble(rateString);
                onRateEditSaveDefault(rate);
            } catch (NumberFormatException e) {
            }
        }
	}

	public final static String EDIT_PARTICIPANT_VALUE_DLG_TAG = "edit_participant_value_dialog";
	public final static String SPLIT_WARNING_TAG = "split_warning_dialog";
    public final static String EDIT_RATE_DLG_TAG = "edit_rate_dialog";

	Vector<ParticipantItemWrapper> participantWrappers = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transaction_edit);
		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		myApp = GroupClearingApplication.getInstance();
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
		myEventId = getIntent().getLongExtra("cz.su.GroupClearing.EventId", -1);
		myTransactionId = getIntent().getLongExtra(
				"cz.su.GroupClearing.TransactionId", -1);
        db = new GCDatabase(this);
		myEvent = db.readEventWithId(myEventId);
		noReceiver = new ClearingPerson(-1);
		noReceiver.setName(getString(R.string.transaction_no_receiver));
        currencySpinner = (Spinner) findViewById(R.id.trans_currency_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.currency_names,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);
        currencySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, 
                    View view, int pos, long id) {
                onCurrencySelected(pos, id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
                }
                );
        rateButton = (Button) findViewById(R.id.trans_currency_rate);
    }

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
		CurrencyList cl = CurrencyList.getInstance();
        Currency cur = myTransaction.getCurrency();
            String currencyCode = cur.toString();
        currencySpinner.setSelection(cl.getPosition(currencyCode));
        rateButton.setEnabled(! cur.equals(myEvent.getDefaultCurrency()));
        refreshParticipants();
	}

	@Override
	protected void onPause() {
		super.onPause();
		onNameChanged();
		onNoteChanged();
		onAmountChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
			db = null;
		}
	}

	public void onTransactionDateButtonClicked(View v) {
		showDialog(DATE_PICK_DIALOG_ID);
	}

	public void refreshParticipants() {
		participants = db.readParticipantsOfEvent(myEventId);
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
			long value = myTransaction.getParticipantValue(participant.getId());
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

	public void setBalanceText() {
		balanceText.setText(myApp.formatCurrencyValueWithSymbol(
				myTransaction.getBalance(), myTransaction.getCurrency())
				+ " ");
		if (myTransaction.getBalance() > 0) {
			balanceText.setTextColor(android.graphics.Color.GREEN);
		} else if (myTransaction.getBalance() < 0) {
			balanceText.setTextColor(android.graphics.Color.RED);
		} else {
			balanceText.setTextColor(getResources().getColor(
					android.R.color.primary_text_dark));
		}
	}

	public void onNameChanged() {
		String newName = nameEdit.getText().toString();
		if (newName.compareTo(myTransaction.getName()) != 0) {
			myTransaction.setName(newName);
			db.updateTransactionName(myTransaction);
		}
	}

	public void onNoteChanged() {
		String newNote = noteEdit.getText().toString();
		if (newNote.compareTo(myTransaction.getNote()) != 0) {
			myTransaction.setNote(newNote);
			db.updateTransactionNote(myTransaction);
		}
	}

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

	public void onAmountChanged() {
		try {
			long newAmount = myApp.parseCurrencyValue(amountEdit.getText()
					.toString(), myTransaction.getCurrency());
			if (newAmount != myTransaction.getAmount()) {
				myTransaction.setAmount(newAmount);
				db.updateTransactionAmount(myTransaction);
				recomputeValues();
			}
		} catch (GCSyntaxException e) {
		}
		amountEdit.setText(myApp.formatCurrencyValue(myTransaction.getAmount(),
				myTransaction.getCurrency()));

	}

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

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_PICK_DIALOG_ID:
			return new DatePickerDialog(this, dateSetListener,
					myTransaction.getYear(), myTransaction.getMonth(),
					myTransaction.getDayOfMonth());
		}
		return null;
	}

	public void onReceiverChanged(int position, long id) {
		ClearingPerson receiver = receiversAdapter.getItem(position);
		if (receiver.getId() != myTransaction.getReceiverId()) {
			myTransaction.setReceiverId(receiver.getId());
			db.updateTransactionReceiverId(myTransaction);
		}
		recomputeValues();
	}

	public void onSplitEvenlyChanged(View v) {
		if (myApp.getNoSplitChangeWarning()
				|| !myTransaction.hasNonzeroValues()) {
			onSplitEvenlyConfirmed(true);
		} else {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag(
					SPLIT_WARNING_TAG);
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);
			WarningDialogWithCheck dialog = new WarningDialogWithCheck(
					getResources().getString(R.string.split_warning));
			dialog.setOnWarningListener(new WarningDialogWithCheck.WarningDialogClickListener() {
				@Override
				public void onWarningConfirmed(boolean checked) {
					onSplitEvenlyConfirmed(checked);
				}

				@Override
				public void onWarningCancelled(boolean checked) {
					onSplitEvenlyCancelled(checked);
				}
			});
			dialog.show(ft, SPLIT_WARNING_TAG);
		}
	}

	public void onSplitEvenlyConfirmed(boolean checked) {
		myApp.setNoSplitChangeWarning(checked);
		if (splitEvenlyCheck.isChecked() != myTransaction.getSplitEvenly()) {
			myTransaction.setSplitEvenly(splitEvenlyCheck.isChecked());
			db.updateTransactionSplitEvenly(myTransaction);
			myTransaction.resetValues(db);
			amountEdit.setEnabled(myTransaction.getSplitEvenly());
			// amountEdit.setFocusable(myTransaction.getSplitEvenly());
			receiverSpinner.setEnabled(myTransaction.getSplitEvenly());
			recomputeValues();
		}
	}

	public void onSplitEvenlyCancelled(boolean checked) {
		splitEvenlyCheck.setChecked(myTransaction.getSplitEvenly());
	}

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
		EditParticipantValueDialog editParticipantValueDialog = new EditParticipantValueDialog(
				participant.getName(), position, participantId,
				myTransaction.getParticipantValue(participantId));
		editParticipantValueDialog.show(ft, EDIT_PARTICIPANT_VALUE_DLG_TAG);
	}

	public void onParticipantLongClick(int position, long participantId) {
		if (!myTransaction.getSplitEvenly()) {
			openValueEditor(position, participantId);
		}
	}

	public void onValueEditorOK(int position, long participantId, long value) {
		myTransaction.setAndSaveParticipantValue(participantId, value, db);
		amountEdit.setText(myApp.formatCurrencyValue(myTransaction.getAmount(),
				myTransaction.getCurrency()));
		ParticipantItemWrapper wrapper = participantWrappers.get(position);
		wrapper.setCheckState(myTransaction.isParticipantMarked(participantId));
		wrapper.setBalance(myTransaction.getParticipantValue(participantId));
		setBalanceText();
		splitEvenlyCheck.setChecked(myTransaction.getSplitEvenly());
	}

	public void onValueEditorCancelled(int position, long participantId) {
		ParticipantItemWrapper wrapper = participantWrappers.get(position);
		wrapper.setCheckState(myTransaction.isParticipantMarked(participantId));
	}


   @Override
      public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.transaction_edit_menu, menu);
         return true;
      }

   @Override
      public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.menu_trans_delete:
               {
                  db.deleteTransactionWithId(myTransactionId); 
                  finish();
                  return true;
               }
            default:
               return super.onOptionsItemSelected(item);
         }
      }

    void onCurrencySelected(int position, long id) {
        CurrencyList cl = CurrencyList.getInstance();
        Currency chosenCurrency = cl.getCurrency(position);
        Currency oldCurrency = myTransaction.getCurrency();
        if (oldCurrency == null || !oldCurrency.equals(chosenCurrency)) {
            myTransaction.setCurrency(chosenCurrency);
            db.updateTransactionCurrencyAndRate(myTransaction);
            rateButton.setEnabled(
                    !chosenCurrency.equals(myEvent.getDefaultCurrency()));
        }
    }
    
	public void onRateButtonClicked(View v) {
        Currency left = myTransaction.getCurrency();
        Currency right = myEvent.getDefaultCurrency();
        double rate = myTransaction.getRate();
        if (left.equals(right)) {
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(
				EDIT_RATE_DLG_TAG);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
        EditRateDialog editRateDialog = new EditRateDialog(left, right, rate);
		editRateDialog.show(ft, EDIT_RATE_DLG_TAG);
    }

    public void onRateEditOK(double rate) {
    }

    public void onRateEditCancel() {
    }

    public void onRateEditSaveDefault(double rate) {
    }
}
