package cz.su.GroupClearing;

import java.text.DateFormat;
import java.util.Currency;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Class representing the activity showing event properties. This class
 * corresponds to activity showing event properties with possibility of
 * modifying them. This corresponds to layout stored in
 * <code>event_properties.xml</code>.
 * 
 * @author Strašlivý Uragán <straslivy.uragan@gmail.com>
 * @version 1.0
 * @since 1.0
 */
public class EventViewActivity extends FragmentActivity {
	/** Object representing interface to the database. */
	private GCDatabase db;

	/** Id of event associated with this activity. */
	private long myEventId = -1;
	/** Application object. */
	private final GroupClearingApplication myApp = GroupClearingApplication
			.getInstance();
	/** Event associated with this activity. */
	private ClearingEvent myEvent = null;
	/**
	 * <code>EditText</code> widget within the activity layout with name of the
	 * event.
	 */
	private EditText eventName = null;
	/**
	 * <code>EditText</code> widget within the activity layout with note
	 * associated with the event.
	 */
	private EditText eventNote = null;
	/**
	 * <code>Button</code> widget within the activity layout for setting the
	 * start date of the event.
	 */
	private Button startDateButton = null;
	/**
	 * <code>Button</code> widget within the activity layout for setting the
	 * finish date of the event.
	 */
	private Button finishDateButton = null;
	/**
	 * <code>Spinner</code> widget within the activity layout for choosing the
	 * currency of the event.
	 */
	private Spinner currencySpinner = null;
	/**
	 * <code>View</code> within the activity layout representing the layout for
	 * showing currency and rate.
	 */
	private View currencyLayout = null;

	/**
	 * Identification of dialog for picking the start date of the event. Method
	 * <code>showDialog</code> and <code>onCreateDialog</code> are used for
	 * showing the date picking dialogs in which start date picking dialog is
	 * determined with this identification number.
	 */
	private static final int START_DATE_PICK_DIALOG_ID = 0;
	/**
	 * Identification of dialog for picking the finish date of the event. Method
	 * <code>showDialog</code> and <code>onCreateDialog</code> are used for
	 * showing the date picking dialogs in which finish date picking dialog is
	 * determined with this identification number.
	 */
	private static final int FINISH_DATE_PICK_DIALOG_ID = 1;

	/**
	 * Tag determining the event id parameter of the activity.
	 */
	public static final String EVENT_ID_PARAM_TAG = "cz.su.GroupClearing.EventId";

	/**
	 * Listener for event of <code>DatePickerDialog</code> dialog for setting
	 * the start date of the event.
	 */
	private final DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			startDateSet(year, monthOfYear, dayOfMonth);
		}
	};

	/**
	 * Listener for event of <code>DatePickerDialog</code> dialog for setting
	 * the finish date of the event.
	 */
	private final DatePickerDialog.OnDateSetListener finishDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			finishDateSet(year, monthOfYear, dayOfMonth);
		}
	};

	/**
	 * Tag for dialog suggesting next receiver. The receiver suggestion is shown
	 * using <code>DialogFragment</code>, that is why it uses this tag.
	 */
	private static final String SUGGEST_RECEIVER_TAG = "suggest_receiver_dialog";

	/**
	 * Called when the activity is newly created. Event id is passed to the
	 * activity using <code>Intent</code> parameters and it is extracted using
	 * <code>Intent.getLongExtra(String, int)</code> with tag stored in
	 * <code>EVENT_ID_PARAM_TAG</code>.
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_properties);

		eventName = (EditText) findViewById(R.id.event_name_edittext);
		eventName
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
		eventName.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					onNameChanged();
				}
			}
		});
		eventNote = (EditText) findViewById(R.id.event_note_edittext);
		eventNote
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
		eventNote.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					onNoteChanged();
				}
			}
		});
		startDateButton = (Button) findViewById(R.id.event_from_date_button);
		finishDateButton = (Button) findViewById(R.id.event_to_date_button);
		myEventId = getIntent().getLongExtra(EVENT_ID_PARAM_TAG, -1);
		if (myApp.getSupportMultipleCurrencies()) {
			currencySpinner = (Spinner) findViewById(R.id.event_currency_spinner);
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
		}
		currencyLayout = findViewById(R.id.currency_layout);
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
		if (myEventId < 0) {
			myEvent = db.createNewEvent();
			myEventId = myEvent.getId();
		} else {
			myEvent = db.readEventWithId(myEventId);
		}
		eventName.setText(myEvent.getName());
		eventNote.setText(myEvent.getNote());
		DateFormat df = DateFormat.getDateInstance();
		Date startDate = myEvent.getStartDate();
		if (startDate != null) {
			startDateButton.setText(df.format(startDate));
		} else {
			startDateButton.setText("--");
		}
		Date finishDate = myEvent.getFinishDate();
		if (finishDate != null) {
			finishDateButton.setText(df.format(finishDate));
		} else {
			finishDateButton.setText("--");
		}
		if (myApp.getSupportMultipleCurrencies()) {
			CurrencyList cl = CurrencyList.getInstance();
			Currency cur = myEvent.getDefaultCurrency();
			String currencyCode = cur.toString();
			currencySpinner.setSelection(cl.getPosition(currencyCode));
			currencyLayout.setVisibility(View.VISIBLE);
		} else {
			currencyLayout.setVisibility(View.GONE);
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.event_properties_menu, menu);
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
			case R.id.menu_event_delete :
				db.deleteEventWithId(myEvent.getId());
				finish();
				return true;
			case R.id.menu_event_suggest_receiver :
				suggestReceiver();
				return true;
			case R.id.menu_event_clearance :
				suggestClearance();
				return true;
			default :
				return super.onOptionsItemSelected(item);
		}
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
	}

	/**
	 * Event handler called when the start date button was clicked. This handler
	 * invokes <code>showDialog</code> to show the start date picking dialog.
	 * 
	 * @param v
	 *            The view with button.
	 */
	public void startDateButtonClicked(View v) {
		showDialog(START_DATE_PICK_DIALOG_ID);
	}

	/**
	 * Event handler called when the finish date button was clicked. This
	 * handler invokes <code>showDialog</code> to show the finish date picking
	 * dialog.
	 * 
	 * @param v
	 */
	public void finishDateButtonClicked(View v) {
		showDialog(FINISH_DATE_PICK_DIALOG_ID);
	}

	/**
	 * Function showing the list of participants. This starts another activity
	 * of class <code>ParticipantsListActivity</code>, the event id (
	 * <code>myEventId</code>) is passed to this activity using intent
	 * parameters.
	 */
	private void showParticipantsList() {
		Intent intent = new Intent(this, ParticipantsListActivity.class);
		intent.putExtra(ParticipantsListActivity.EVENT_ID_PARAM_TAG, myEventId);
		startActivity(intent);
	}

	/**
	 * Function showing the list of transactions. This starts another activity
	 * of class <code>TransactionsListActivity</code>, the event id (
	 * <code>myEventId</code>) is passed to this activity using intent
	 * parameters.
	 */
	private void showTransactionsList() {
		Intent intent = new Intent(this, TransactionsListActivity.class);
		intent.putExtra(TransactionsListActivity.EVENT_ID_PARAM_TAG, myEventId);
		startActivity(intent);
	}

	/**
	 * Event handler called when the “show participants” button was clicked.
	 * Calls <code>showParticipantsList</code> to show the list of participants.
	 * 
	 * @param v
	 *            The view with button.
	 */
	public void viewParticipantsButtonClicked(View v) {
		showParticipantsList();
	}

	/**
	 * Event handler called when the “show transaction” button was clicked.
	 * Calls <code>showTransactionsList</code> to show the list of transactions.
	 * 
	 * @param v
	 *            The view with button.
	 */
	public void viewTransactionsButtonClicked(View v) {
		showTransactionsList();
	}

	/**
	 * Event handler called when name of the event was changed. This handler is
	 * set in <code>onCreate</code> method. It updates the event with new name
	 * both in <code>myEvent</code> object and in database <code>db</code>.
	 */
	public void onNameChanged() {
		String newName = eventName.getText().toString();
		if (newName.compareTo(myEvent.getName()) != 0) {
			myEvent.setName(newName);
			db.updateEventName(myEvent);
		}
	}

	/**
	 * Event handler called when note associated with the event was changed.
	 * This handler is set in <code>onCreate</code> method. It updates the event
	 * with new note both in <code>myEvent</code> object and in database
	 * <code>db</code>.
	 */
	public void onNoteChanged() {
		String newNote = eventNote.getText().toString();
		if (newNote.compareTo(myEvent.getNote()) != 0) {
			myEvent.setNote(newNote);
			db.updateEventNote(myEvent);
		}
	}

	/**
	 * Sets the new start date of the event. Date is given in parameters as a
	 * triple (year, month, day within month). This method is invoked from
	 * <code>startDateSetListener</code>. This method updates the date in
	 * <code>myEvent</code> object and in database <code>db</code>. Moreover it
	 * changes the label on <code>startDateButton</code> to display correct
	 * start date.
	 * 
	 * @param year
	 *            New year of start date of the event.
	 * @param monthOfYear
	 *            New month of year of start date of the event.
	 * @param dayOfMonth
	 *            New day of month of start date of the event.
	 */
	public void startDateSet(int year, int monthOfYear, int dayOfMonth) {
		myEvent.setStartDate(year, monthOfYear, dayOfMonth);
		DateFormat df = DateFormat.getDateInstance();
		Date startDate = myEvent.getStartDate();
		startDateButton.setText(df.format(startDate));
		db.updateEventStartDate(myEvent);
	}

	/**
	 * Sets the new finish date of the event. Date is given in parameters as a
	 * triple (year, month, day within month). This method is invoked from
	 * <code>finishDateSetListener</code>. This method updates the date in
	 * <code>myEvent</code> object and in database <code>db</code>. Moreover it
	 * changes the label on <code>finishDateButton</code> to display correct
	 * finish date.
	 * 
	 * @param year
	 *            New year of finish date of the event.
	 * @param monthOfYear
	 *            New month of year of finish date of the event.
	 * @param dayOfMonth
	 *            New day of month of finish date of the event.
	 */
	public void finishDateSet(int year, int monthOfYear, int dayOfMonth) {
		myEvent.setFinishDate(year, monthOfYear, dayOfMonth);
		DateFormat df = DateFormat.getDateInstance();
		Date finishDate = myEvent.getFinishDate();
		finishDateButton.setText(df.format(finishDate));
		db.updateEventFinishDate(myEvent);
	}

	/**
	 * Creates dialogs for start and finish date picking.
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case START_DATE_PICK_DIALOG_ID :
				return new DatePickerDialog(this, startDateSetListener,
						myEvent.getStartYear(), myEvent.getStartMonth(),
						myEvent.getStartDayOfMonth());
			case FINISH_DATE_PICK_DIALOG_ID :
				return new DatePickerDialog(this, finishDateSetListener,
						myEvent.getFinishYear(), myEvent.getFinishMonth(),
						myEvent.getFinishDayOfMonth());
		}
		return null;
	}

	/**
	 * Suggest the next possible receiver of the next transaction. This is a
	 * person with biggest debt or negative value, which is suggested to pay in
	 * the next transaction and thus will receive the money from others. Right
	 * now, the value is taken cumulatively over all currencies, which is the
	 * easiest option in case of more currencies being used within event.
	 * <code>GCDatabase.findParticipantWithMinValue(long)</code> is used to
	 * determine the next receiver. The receiver is suggested using a dialog
	 * shown as a <code>Fragment</code>. It is a <code>YesNoDialog</code>.
	 * 
	 * @see cz.su.GroupClearing.GCDatabase#findParticipantWithMinValue(long)
	 * @see cz.su.YesNoDialog
	 */
	public void suggestReceiver() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(
				SUGGEST_RECEIVER_TAG);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		String format = getResources().getString(
				R.string.suggest_receiver_msg_format);
		ParticipantValue valueInfo = db.findParticipantWithMinValue(myEventId);
		ClearingPerson participant = db.readPersonWithId(valueInfo.getId(),
				GCDatabase.ComputeBalance.COMPUTE_CUMULATIVE);
		String message = String.format(format, participant.getName(), myApp
				.formatCurrencyValueWithSymbol(valueInfo.getValue(),
						myEvent.getDefaultCurrency()));
		YesNoDialog dialog = new YesNoDialog(message);
		dialog.setDialogTag(Long.valueOf(valueInfo.getId()));
		dialog.setOnClickListener(new YesNoDialog.OnClickListener() {
			@Override
			public void onOkClicked(YesNoDialog dlg) {
				onSuggestReceiverOkClicked(dlg);
			}

			@Override
			public void onCancelled(YesNoDialog dlg) {
				onSuggestReceiverCancelled(dlg);
			}
		});
		dialog.show(ft, SUGGEST_RECEIVER_TAG);
	}

	/**
	 * Event handler called when user clicked on OK button within dialog
	 * suggesting the next receiver. This method then creates new transaction
	 * with suggested receiver and shows its properties in
	 * <code>TransactionEditActivity</code>.
	 * 
	 * @param dlg
	 *            Dialog in which the OK button was clicked.
	 */
	void onSuggestReceiverOkClicked(YesNoDialog dlg) {
		try {
			ClearingTransaction aTransaction = db
					.createNewTransaction(myEventId);
			Long idObject = (Long) dlg.getDialogTag();
			aTransaction.setReceiverId(idObject.longValue());
			db.updateTransactionReceiverId(aTransaction);
			Intent intent = new Intent(this, TransactionEditActivity.class);
			intent.putExtra(TransactionEditActivity.EVENT_ID_PARAM_TAG,
					myEventId);
			intent.putExtra(TransactionEditActivity.TRANSACTION_ID_PARAM_TAG,
					aTransaction.getId());
			startActivity(intent);
		} catch (GCEventDoesNotExistException e) {
			// Warn user perhaps?
		}
	}

	/**
	 * Event handler called when user clicked on Cancel button within dialog
	 * suggesting the next receiver. This method does nothing.
	 * 
	 * @param dlg
	 *            Dialog in which the Cancel button was clicked.
	 */
	void onSuggestReceiverCancelled(YesNoDialog dlg) {
	}

	/**
	 * Invokes new <code>SuggestClearanceActivity</code> for suggesting
	 * clearance of this event.
	 */
	void suggestClearance() {
		Intent intent = new Intent(this, SuggestClearanceActivity.class);
		intent.putExtra(SuggestClearanceActivity.EVENT_ID_PARAM_TAG, myEventId);
		startActivity(intent);
	}

	/**
	 * Called when new currency of the event was selected. Serves as a
	 * corresponding <code>Spinner</code> event handler. First obtains the
	 * currency from the <code>CurrencyList</code> using the
	 * <code>position</code> parameter. If the new currency is different from
	 * the previous one, it is updated both in <code>myEvent</code> object and
	 * in the database.
	 * 
	 * @param position
	 *            Position of the selected currency within the list of
	 *            currencies.
	 * @param id
	 *            Id of the selected currency (this value is not used).
	 */
	void onCurrencySelected(int position, long id) {
		CurrencyList cl = CurrencyList.getInstance();
		Currency chosenCurrency = cl.getCurrency(position);
		Currency oldCurrency = myEvent.getDefaultCurrency();
		if (oldCurrency == null || !oldCurrency.equals(chosenCurrency)) {
			myEvent.setDefaultCurrency(chosenCurrency);
			db.updateEventCurrency(myEvent);
		}
	}
}
