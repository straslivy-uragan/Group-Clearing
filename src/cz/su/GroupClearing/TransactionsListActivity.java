package cz.su.GroupClearing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import cz.su.GroupClearing.TransactionsListAdapter;

public class TransactionsListActivity extends FragmentActivity {

   private ClearingEvent myEvent = null;
   private GroupClearingApplication myApplication = null;
   private TransactionsListAdapter transactionsListAdapter = null;

   @Override
      public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         myApplication = GroupClearingApplication.getInstance();
         myEvent = myApplication.getActiveEvent();
         setContentView(R.layout.transactions_list);
         ListView lv = (ListView) findViewById(R.id.transactions_list_view);
         lv.setOnItemClickListener(new OnItemClickListener() {
               public void onItemClick(AdapterView<?> parent, View view,
                  int position, long id) {
               onTransactionClicked(position, id);
               }
               });
         transactionsListAdapter = new TransactionsListAdapter(this);
         lv.setAdapter(transactionsListAdapter);
         registerForContextMenu(lv);
      }

   @Override
      protected void onResume() {
         super.onResume();
         refreshData();
      }

   @Override
      protected void onPause() {
         super.onPause();
         try {
            myApplication.saveModifiedEvents();
         } catch (GroupClearingException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(e.getMessage());
            builder.setTitle(R.string.alert_title);
            builder.setPositiveButton("OK",
                  new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                  }
                  });
            AlertDialog alert = builder.create();
            alert.show();
         }
      }

   public void onTransactionClicked(final int position, long id) {
      Intent intent = new Intent(this, TransactionEditActivity.class);
      ClearingTransaction aTransaction = null;
      if (position >= myEvent.getNumberOfTransactions())
      {
         aTransaction = myEvent.newTransaction();
         refreshData();
      }
      else
      {
         aTransaction = myEvent.getTransaction(position);
      }
      intent.putExtra("cz.su.GroupClearing.TransactionId", aTransaction.getId());
      startActivity(intent);
   }

   public void refreshData() {
      transactionsListAdapter.notifyDataSetChanged();
   }

   @Override
      public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.transactions_list_menu, menu);
         return true;
      }

   @Override
      public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
            case R.id.menu_add_transaction:
               {
                  onTransactionClicked(myEvent.getNumberOfTransactions(), 0);
                  return true;
               }
            default:
               return super.onOptionsItemSelected(item);
         }
      }

   @Override
      public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.transaction_context_menu, menu);
      }

   @Override
      public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
            case R.id.menu_transaction_delete:
               {
                  myEvent.removeTransaction((int)info.id);
                  refreshData();
               }
               return true;
            default:
               return super.onContextItemSelected(item);
         }
      }


}
