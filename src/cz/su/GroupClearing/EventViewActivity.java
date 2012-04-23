/**
 * 
 */
package cz.su.GroupClearing;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

/**
 * @author su
 *
 */
public class EventViewActivity extends Activity {
   private GroupClearingApplication myApplication;
   private ClearingEvent myEvent;
   private EditText eventName;
   private EditText eventNote;
   private Button startDateButton;
   private Button finishDateButton;

   private static final int START_DATE_PICK_DIALOG_ID = 0;
   private static final int FINISH_DATE_PICK_DIALOG_ID = 1;

   private DatePickerDialog.OnDateSetListener startDateSetListener =
      new DatePickerDialog.OnDateSetListener() {

         public void onDateSet(DatePicker view, int year, 
               int monthOfYear, int dayOfMonth) {
            startDateSet(year, monthOfYear, dayOfMonth);
         }
      };
   private DatePickerDialog.OnDateSetListener finishDateSetListener =
      new DatePickerDialog.OnDateSetListener() {

         public void onDateSet(DatePicker view, int year, 
               int monthOfYear, int dayOfMonth) {
            finishDateSet(year, monthOfYear, dayOfMonth);
         }
      };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
       setContentView(R.layout.event_properties);

       myApplication = GroupClearingApplication.getInstance();
       eventName = (EditText)findViewById(R.id.event_name_edittext);
       eventNote = (EditText)findViewById(R.id.event_note_edittext);
       startDateButton = (Button)findViewById(R.id.event_from_date_button);
       finishDateButton = (Button)findViewById(R.id.event_to_date_button);
    }
    @Override
       protected void onResume() {
          super.onResume();
          myEvent = myApplication.getActiveEvent();
          eventName.setText(myEvent.getName());
          eventNote.setText(myEvent.getNote());
          DateFormat df = DateFormat.getDateInstance();
          Date startDate = myEvent.getStartDate();
          if (startDate != null)
          {
             startDateButton.setText(df.format(startDate));
          }
          else
          {
             startDateButton.setText("--");
          }
          Date finishDate = myEvent.getFinishDate();
          if (finishDate != null)
          {
             finishDateButton.setText(df.format(finishDate));
          }
          else
          {
             finishDateButton.setText("--");
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
             case R.id.menu_event_delete:
                myApplication.deleteActiveEvent();
                finish();
                return true;
             default:
                return super.onOptionsItemSelected(item);
          }
       }

    @Override
       protected void onPause() {
          super.onPause();
          myEvent.setName(eventName.getText().toString());
          myEvent.setNote(eventNote.getText().toString());
          myApplication.saveActiveEvent();
       }

    public void startDateButtonClicked(View v) {
       showDialog(START_DATE_PICK_DIALOG_ID);
    }

    public void finishDateButtonClicked(View v) {
       showDialog(FINISH_DATE_PICK_DIALOG_ID);
    }

    private void showParticipantsList() {
       Intent intent = new Intent(this, ParticipantsListActivity.class);
       startActivity(intent);
    }

    private void showTransactionsList() {
       Intent intent = new Intent(this, TransactionsListActivity.class);
       startActivity(intent);
    }

    public void viewParticipantsButtonClicked(View v) {
    	showParticipantsList();
    }

    public void viewTransactionsButtonClicked(View v) {
       showTransactionsList();
    }

    public void deleteButtonClicked(View v) {
    }

    public void saveButtonClicked(View v) {
    }

    public void startDateSet(int year, int monthOfYear, int dayOfMonth) {
       myEvent.setStartDate(year, monthOfYear, dayOfMonth);
       DateFormat df = DateFormat.getDateInstance();
       Date startDate = myEvent.getStartDate();
       startDateButton.setText(df.format(startDate));
    }
    
    public void finishDateSet(int year, int monthOfYear, int dayOfMonth) {
       myEvent.setFinishDate(year, monthOfYear, dayOfMonth);
       DateFormat df = DateFormat.getDateInstance();
       Date finishDate = myEvent.getFinishDate();
       finishDateButton.setText(df.format(finishDate));
    }

   @Override
      protected Dialog onCreateDialog(int id) {
         switch (id) {
            case START_DATE_PICK_DIALOG_ID:
               return new DatePickerDialog(this,
                     startDateSetListener,
                     myEvent.getStartYear(),
                     myEvent.getStartMonth(),
                     myEvent.getStartDayOfMonth());
            case FINISH_DATE_PICK_DIALOG_ID:
               return new DatePickerDialog(this,
                     finishDateSetListener,
                     myEvent.getFinishYear(),
                     myEvent.getFinishMonth(),
                     myEvent.getFinishDayOfMonth());
         }
         return null;
      }

}
