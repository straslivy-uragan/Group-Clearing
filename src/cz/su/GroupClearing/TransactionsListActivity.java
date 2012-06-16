package cz.su.GroupClearing;

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

public class TransactionsListActivity extends FragmentActivity
{

   private TransactionsListAdapter transactionsListAdapter = null;
   private long myEventId = -1;

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      // myEvent = myApplication.getActiveEvent();
      setContentView(R.layout.transactions_list);
      ListView lv = (ListView) findViewById(R.id.transactions_list_view);
      lv.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
               int position, long id)
         {
            onTransactionClicked(position, id);
         }
      });
      myEventId = getIntent().getLongExtra("cz.su.GroupClearing.EventId", -1);
      transactionsListAdapter = new TransactionsListAdapter(this, myEventId);
      lv.setAdapter(transactionsListAdapter);
      registerForContextMenu(lv);
   }

   @Override
   protected void onResume()
   {
      super.onResume();
      refreshData();
   }

   @Override
   protected void onDestroy()
   {
      super.onDestroy();
      if (transactionsListAdapter != null)
      {
         transactionsListAdapter.closeDB();
      }
   }

   public void onTransactionClicked(final int position, long id)
   {
      Intent intent = new Intent(this, TransactionEditActivity.class);
      ClearingTransaction aTransaction = null;
      if (id < 0)
      {
         aTransaction = transactionsListAdapter.createTransaction();
         refreshData();
      }
      else
      {
         aTransaction = (ClearingTransaction) transactionsListAdapter
               .getItem(position);
      }
      intent.putExtra("cz.su.GroupClearing.EventId", myEventId);
      intent.putExtra("cz.su.GroupClearing.TransactionId", aTransaction.getId());
      startActivity(intent);
   }

   public void refreshData()
   {
      transactionsListAdapter.readTransactionsFromDB();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.transactions_list_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      // Handle item selection
      switch (item.getItemId())
      {
      case R.id.menu_add_transaction:
      {
         onTransactionClicked(transactionsListAdapter.getNumberOfTransactions(), -1);
         return true;
      }
      default:
         return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v,
         ContextMenuInfo menuInfo)
   {
      super.onCreateContextMenu(menu, v, menuInfo);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.transaction_context_menu, menu);
   }

   @Override
   public boolean onContextItemSelected(MenuItem item)
   {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      switch (item.getItemId())
      {
      case R.id.menu_transaction_delete:
      {
         transactionsListAdapter.removeTransactionAtPosition(info.position);
         refreshData();
      }
         return true;
      default:
         return super.onContextItemSelected(item);
      }
   }
}
