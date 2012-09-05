/**
 * 
 */
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
 * @author su
 * 
 */
public class EventViewActivity extends FragmentActivity {
	private GCDatabase db;

	private long myEventId = -1;
	private final GroupClearingApplication myApp = GroupClearingApplication
			.getInstance();
	private ClearingEvent myEvent;
	private EditText eventName;
	private EditText eventNote;
	private Button startDateButton;
	private Button finishDateButton;
	private Spinner currencySpinner;

	private static final int START_DATE_PICK_DIALOG_ID = 0;
	private static final int FINISH_DATE_PICK_DIALOG_ID = 1;

	private final DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			startDateSet(year, monthOfYear, dayOfMonth);
		}
	};

	private final DatePickerDialog.OnDateSetListener finishDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			finishDateSet(year, monthOfYear, dayOfMonth);
		}
	};

	private static final String SUGGEST_RECEIVER_TAG = "suggest_receiver_dialog";

	/** Called when the activity is first created. */
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
		/*
		 * eventName.setOnKeyListener(new View.OnKeyListener() { public boolean
		 * onKey(View v, int keyCode, KeyEvent event) { if ((event.getAction()
		 * == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		 * onNameChanged(); return true; } return false; } });
		 */
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
		myEventId = getIntent().getLongExtra("cz.su.GroupClearing.EventId", -1);
		currencySpinner = (Spinner) findViewById(R.id.event_currency_spinner);
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
	}

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
        CurrencyList cl = CurrencyList.getInstance();
        Currency cur = myEvent.getDefaultCurrency();
        String currencyCode = cur.toString();
        currencySpinner.setSelection(cl.getPosition(currencyCode));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.close();
			db = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.event_properties_menu, menu);
		return true;
	}

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

	@Override
	protected void onPause() {
		super.onPause();
		onNameChanged();
		onNoteChanged();
	}

	public void startDateButtonClicked(View v) {
		showDialog(START_DATE_PICK_DIALOG_ID);
	}

	public void finishDateButtonClicked(View v) {
		showDialog(FINISH_DATE_PICK_DIALOG_ID);
	}

	private void showParticipantsList() {
		Intent intent = new Intent(this, ParticipantsListActivity.class);
		intent.putExtra("cz.su.GroupClearing.EventId", myEventId);
		startActivity(intent);
	}

	private void showTransactionsList() {
		Intent intent = new Intent(this, TransactionsListActivity.class);
		intent.putExtra("cz.su.GroupClearing.EventId", myEventId);
		startActivity(intent);
	}

	public void viewParticipantsButtonClicked(View v) {
		showParticipantsList();
	}

	public void viewTransactionsButtonClicked(View v) {
		showTransactionsList();
	}

	public void onNameChanged() {
		String newName = eventName.getText().toString();
		if (newName.compareTo(myEvent.getName()) != 0) {
			myEvent.setName(newName);
			db.updateEventName(myEvent);
		}
	}

	public void onNoteChanged() {
		String newNote = eventNote.getText().toString();
		if (newNote.compareTo(myEvent.getNote()) != 0) {
			myEvent.setNote(newNote);
			db.updateEventNote(myEvent);
		}
	}

	public void startDateSet(int year, int monthOfYear, int dayOfMonth) {
		myEvent.setStartDate(year, monthOfYear, dayOfMonth);
		DateFormat df = DateFormat.getDateInstance();
		Date startDate = myEvent.getStartDate();
		startDateButton.setText(df.format(startDate));
		db.updateEventStartDate(myEvent);
	}

	public void finishDateSet(int year, int monthOfYear, int dayOfMonth) {
		myEvent.setFinishDate(year, monthOfYear, dayOfMonth);
		DateFormat df = DateFormat.getDateInstance();
		Date finishDate = myEvent.getFinishDate();
		finishDateButton.setText(df.format(finishDate));
		db.updateEventFinishDate(myEvent);
	}

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
		ClearingPerson participant = db.readPersonWithId(valueInfo.getId());
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

	void onSuggestReceiverOkClicked(YesNoDialog dlg) {
		try {
			ClearingTransaction aTransaction = db
					.createNewTransaction(myEventId);
			Long idObject = (Long) dlg.getDialogTag();
			aTransaction.setReceiverId(idObject.longValue());
			db.updateTransactionReceiverId(aTransaction);
			Intent intent = new Intent(this, TransactionEditActivity.class);
			intent.putExtra("cz.su.GroupClearing.EventId", myEventId);
			intent.putExtra("cz.su.GroupClearing.TransactionId",
					aTransaction.getId());
			startActivity(intent);
		} catch (GCEventDoesNotExistException e) {
			// Warn user perhaps?
		}
	}

	void onSuggestReceiverCancelled(YesNoDialog dlg) {
	}

	void suggestClearance() {
		Intent intent = new Intent(this, SuggestClearanceActivity.class);
		intent.putExtra("cz.su.GroupClearing.EventId", myEventId);
		startActivity(intent);
	}

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
