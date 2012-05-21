package cz.su.GroupClearing;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class TransactionEditActivity extends Activity
{
   private GroupClearingApplication myApplication;
   private ClearingEvent myEvent;
   private ClearingTransaction myTransaction;
   private EditText transactionNameEdit;

   /** Called when the activity is first created. */
   @Override
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.transaction_edit);

         myApplication = GroupClearingApplication.getInstance();
         //	myEvent = myApplication.getActiveEvent();
         transactionNameEdit = (EditText) findViewById(R.id.transaction_name_edit);
         int transactionId = getIntent().getIntExtra(
               "cz.su.GroupClearing.TransactionId", 10);
         transactionNameEdit.setText((new Integer(transactionId)).toString());
         myTransaction = null;
      }

   @Override
      protected void onResume()
      {
         super.onResume();
      }

   public void onTransactionDateButtonClicked(View v)
   {
   }

}
