package cz.su.GroupClearing;

import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;

import android.app.Activity;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;

public class TransactionEditActivity extends Activity
{
   private EditText nameEdit = null;
   private EditText noteEdit = null;
   private EditText amountEdit = null;
   private Button dateButton = null;
   private CheckBox splitEvenlyCheck = null;
   Vector<ClearingPerson> participants = null;
   private ArrayAdapter<ClearingPerson> receiversAdapter = null;
   private Spinner receiverSpinner = null;
   private LinearLayout participantsList = null;
   private long myEventId = -1;
   private long myTransactionId = -1;
   private ClearingTransaction myTransaction = null;
   private GCDatabase db = null;
   private ClearingPerson noReceiver = null;

   private static final int DATE_PICK_DIALOG_ID = 0;

   private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

      public void onDateSet(DatePicker view, int year, int monthOfYear,
            int dayOfMonth)
      {
         onDateChanged(year, monthOfYear, dayOfMonth);
      }
   };

   public class ReceiverSpinnerOnItemSelected implements OnItemSelectedListener
   {
      public void onItemSelected(AdapterView<?> parent, View view, int pos,
            long id)
      {
         onReceiverChanged(pos, id);
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0)
      {
         onReceiverChanged(0, -1);
      }
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.transaction_edit);

      nameEdit = (EditText) findViewById(R.id.transaction_name_edit);
      nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
         {
            if (actionId == EditorInfo.IME_ACTION_DONE)
            {
               onNameChanged();
               return true;
            }
            return false;
         }
      });
      nameEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus)
         {
            if (!hasFocus)
            {
               onNameChanged();
            }
         }
      });
      noteEdit = (EditText) findViewById(R.id.transaction_note_edit);
      noteEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
         {
            if (actionId == EditorInfo.IME_ACTION_DONE)
            {
               onNoteChanged();
               return true;
            }
            return false;
         }
      });
      noteEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus)
         {
            if (!hasFocus)
            {
               onNoteChanged();
            }
         }
      });
      amountEdit = (EditText) findViewById(R.id.transaction_amount_edit);
      amountEdit
            .setOnEditorActionListener(new TextView.OnEditorActionListener() {
               @Override
               public boolean onEditorAction(TextView v, int actionId,
                     KeyEvent event)
               {
                  if (actionId == EditorInfo.IME_ACTION_DONE)
                  {
                     onAmountChanged();
                  }
                  return false;
               }
            });
      amountEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus)
         {
            if (!hasFocus)
            {
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
      noReceiver = new ClearingPerson(-1);
      noReceiver.setName(getString(R.string.transaction_no_receiver));
   }

   @Override
   protected void onResume()
   {
      super.onResume();
      if (db == null)
      {
         db = new GCDatabase(this);
      }
      if (myTransactionId < 0)
      {
         myTransaction = db.createNewTransaction(myEventId);
      }
      else
      {
         myTransaction = db.readTransactionWithId(myTransactionId);
      }
      nameEdit.setText(myTransaction.getName());
      noteEdit.setText(myTransaction.getNote());
      amountEdit.setText(GroupClearingApplication.getInstance()
            .formatCurrencyValue(myTransaction.getAmount(),
                  myTransaction.getCurrency()));
      DateFormat df = DateFormat.getDateInstance();
      Date date = myTransaction.getDate();
      if (date != null)
      {
         dateButton.setText(df.format(date));
      }
      else
      {
         dateButton.setText("--");
      }
      splitEvenlyCheck.setChecked(myTransaction.getSplitEvenly());
      refreshParticipants();
   }

   @Override
   protected void onPause()
   {
      super.onPause();
      onNameChanged();
      onNoteChanged();
      onAmountChanged();
   }

   @Override
   protected void onDestroy()
   {
      super.onDestroy();
      if (db != null)
      {
         db.close();
         db = null;
      }
   }

   public void onTransactionDateButtonClicked(View v)
   {
      showDialog(DATE_PICK_DIALOG_ID);
   }

   public void refreshParticipants()
   {
      participants = db.readParticipantsOfEvent(myEventId);
      receiversAdapter.clear();
      receiversAdapter.add(noReceiver);
      int selectedPosition = 0;
      participantsList.removeAllViews();
      for (int i = 0; i < participants.size(); ++i)
      {
         ClearingPerson participant = participants.get(i);
         if (participant.getId() == myTransaction.getReceiverId())
         {
            selectedPosition = i + 1;
         }
         receiversAdapter.add(participants.get(i));
         addParticipantView(i);
      }
      receiverSpinner.setSelection(selectedPosition);
   }

   public void onParticipantClicked(View v)
   {

   }

   public void addParticipantView(int position)
   {
      CheckBox participantCheck = new CheckBox(this);
      participantCheck.setText(participants.get(position).getName());
      participantCheck.setTag(participants.get(position));
      participantCheck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               onParticipantClicked(v);
            }
            }
            );
      participantsList.addView(participantCheck);
   }

   public void onNameChanged()
   {
      String newName = nameEdit.getText().toString();
      if (newName.compareTo(myTransaction.getName()) != 0)
      {
         myTransaction.setName(newName);
         db.updateTransactionName(myTransaction);
      }
   }

   public void onNoteChanged()
   {
      String newNote = noteEdit.getText().toString();
      if (newNote.compareTo(myTransaction.getNote()) != 0)
      {
         myTransaction.setNote(newNote);
         db.updateTransactionNote(myTransaction);
      }
   }

   public void onAmountChanged()
   {
      GroupClearingApplication app = GroupClearingApplication.getInstance();
      try
      {
         long newAmount = app.parseCurrencyValue(amountEdit.getText()
               .toString(), myTransaction.getCurrency());
         if (newAmount != myTransaction.getAmount())
         {
            myTransaction.setAmount(newAmount);
            db.updateTransactionAmount(myTransaction);
            // TODO: Do some real computations here
         }
      }
      catch (GCSyntaxException e)
      {
      }
      amountEdit.setText(GroupClearingApplication.getInstance()
            .formatCurrencyValue(myTransaction.getAmount(),
                  myTransaction.getCurrency()));
   }

   public void onDateChanged(int year, int monthOfYear, int day)
   {
      if (day != myTransaction.getDayOfMonth()
            || monthOfYear != myTransaction.getMonth()
            || year != myTransaction.getYear())
      {
         myTransaction.setDate(year, monthOfYear, day);
         DateFormat df = DateFormat.getDateInstance();
         Date date = myTransaction.getDate();
         dateButton.setText(df.format(date));
         db.updateTransactionDate(myTransaction);
      }
   }

   @Override
   protected Dialog onCreateDialog(int id)
   {
      switch (id)
      {
      case DATE_PICK_DIALOG_ID:
         return new DatePickerDialog(this, dateSetListener,
               myTransaction.getYear(), myTransaction.getMonth(),
               myTransaction.getDayOfMonth());
      }
      return null;
   }

   public void onReceiverChanged(int position, long id)
   {
      ClearingPerson receiver = receiversAdapter.getItem(position);
      if (receiver.getId() != myTransaction.getId())
      {
         myTransaction.setReceiverId(receiver.getId());
         db.updateTransactionReceiverId(myTransaction);
      }
   }

   public void onSplitEvenlyChanged(View v)
   {
      if (splitEvenlyCheck.isChecked() != myTransaction.getSplitEvenly())
      {
         myTransaction.setSplitEvenly(splitEvenlyCheck.isChecked());
         db.updateTransactionSplitEvenly(myTransaction);
         // TODO: Also, do some necessary computations here.
      }
   }
}
