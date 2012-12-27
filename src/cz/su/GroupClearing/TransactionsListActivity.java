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
/** Class representing the activity showing a list of transactions for
 * given event. The layout of this activity consists of a
 * <code>ListView</code> for showing the list of transactions and a
 * button for adding a new transaction. The layout of this activity is
 * stored in <code>transactions_list.xml</code>. The list items are
 * described in <code>transactions_list_item.xml</code>. Options menu
 * contains item for creating new transactions. Context menu of each
 * transaction item contains possibility to delete the transaction.
 * Data to the list of transactions are provided by an object of class
 * <code>TransactionsListAdapter</code>.
 *
 * @see cz.su.GroupClearing.TransactionsListAdapter
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class TransactionsListActivity extends FragmentActivity {

    /** Adapter providing data for the list of the transactions. */
	private TransactionsListAdapter transactionsListAdapter = null;
    /** Id of the enclosing event. */
	private long myEventId = -1;
    /** Tag determining the event id parameter of the activity.
     */
    public static final String EVENT_ID_PARAM_TAG = "cz.su.GroupClearing.EventId";

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// myEvent = myApplication.getActiveEvent();
		setContentView(R.layout.transactions_list);
		ListView lv = (ListView) findViewById(R.id.transactions_list_view);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onTransactionClicked(position, id);
			}
		});
		myEventId = getIntent().getLongExtra(EVENT_ID_PARAM_TAG, -1);
		transactionsListAdapter = new TransactionsListAdapter(this, myEventId);
		lv.setAdapter(transactionsListAdapter);
		registerForContextMenu(lv);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		refreshData();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (transactionsListAdapter != null) {
			transactionsListAdapter.closeDB();
		}
	}

    /** Called when a transaction item of the list was clicked. Opens
     * activity <code>TransactionEditActivity</code> for editing the
     * transaction. If <code>id</code>&lt;0,
     * then new transaction is created first by callin
     * <code>TransactionsListAdapter.createTransaction()</code> function.
     *
	 * @param position Position of the transaction within the list.
	 * @param id Id of the transaction.
	 */
	public void onTransactionClicked(final int position, long id) {
		Intent intent = new Intent(this, TransactionEditActivity.class);
		ClearingTransaction aTransaction = null;
		if (id < 0) {
			aTransaction = transactionsListAdapter.createTransaction();
			refreshData();
		} else {
			aTransaction = (ClearingTransaction) transactionsListAdapter
					.getItem(position);
		}
		intent.putExtra("cz.su.GroupClearing.EventId", myEventId);
		intent.putExtra("cz.su.GroupClearing.TransactionId",
				aTransaction.getId());
		startActivity(intent);
	}

    /** Refreshes the list of transactions from the database. Only
     * calls
     * <code>TransactionsListAdapter.readTransactionsFromDB()</code>
     * function.
	 */
	public void refreshData() {
		transactionsListAdapter.readTransactionsFromDB();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.transactions_list_menu, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_add_transaction : {
				onTransactionClicked(
						transactionsListAdapter.getNumberOfTransactions(), -1);
				return true;
			}
			default :
				return super.onOptionsItemSelected(item);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.transaction_context_menu, menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.menu_transaction_delete : {
				transactionsListAdapter
						.removeTransactionAtPosition(info.position);
				refreshData();
			}
				return true;
			default :
				return super.onContextItemSelected(item);
		}
	}

	/** Called when button for adding new transaction was clicked.
     * Only calls <code>onTransactionClicked</code> with
     * <code>id</code> equal to -1.
	 * @param v The <code>View</code> of the button.
	 */
	public void onAddNewTransactionClicked(View v) {
		onTransactionClicked(transactionsListAdapter.getNumberOfTransactions(),
				-1);
	}
}
