package cz.su.GroupClearing;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

/** An adapter providing data for a list of transactions. The layout
 * of an item is described in <code>transactions_list_item.xml</code>.
 * An item contains the name, the date, and the amount of the
 * transaction. Data are retrieved from the database. This adapter is
 * used by <code>TransactionsListActivity</code> activity.
 *
 * @see cz.su.GroupClearing.TransactionsListActivity
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class TransactionsListAdapter implements ListAdapter {
	/** Inflater used for inflating layout of the item.
	 */
	LayoutInflater inflater;
	/** Enclosing context (activity).
	 */
	Context context;
	/** Id of the event of the transactions in the list.
	 */
	long myEventId = -1;
	/** List of observers of data changes in this adapter.
	 */
	ArrayList<DataSetObserver> observers;
	/** Transactions in the list.
	 */
	Vector<ClearingTransaction> transactions;
	/** Database interface object. 
	 */
	GCDatabase db = null;

	/** Id of the normal transaction item type.
	 */
	public static final int NORMAL_TRANSACTION_TYPE = 0;

	/** Constructs new adapter.
	 * @param context Enclosing context (activity)
	 * @param myEventId Id of the activity whose transactions should
     * be displayed.
	 */
	public TransactionsListAdapter(Context context, long myEventId) {
		this.myEventId = myEventId;
		this.context = context;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		observers = new ArrayList<DataSetObserver>();
		readTransactionsFromDB();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return transactions.size();
	}

	/** Returns the number of transactions in the list.
	 * @return The number of transactions in the list.
	 */
	public int getNumberOfTransactions() {
		return transactions.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		if (position < transactions.size()) {
			return transactions.get(position);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		if (position < transactions.size()) {
			return transactions.get(position).getId();
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		return NORMAL_TRANSACTION_TYPE;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		TransactionsListItemWrapper wrapper = null;
		if (convertView != null) {
			rowView = convertView;
			wrapper = (TransactionsListItemWrapper) (rowView.getTag());
		} else {
			rowView = inflater.inflate(R.layout.transactions_list_item, null);
			wrapper = new TransactionsListItemWrapper(rowView);
			rowView.setTag(wrapper);
		}
		ClearingTransaction transaction = transactions.get(position);
		DateFormat df = DateFormat.getDateInstance();
		wrapper.getName().setText(transaction.getName());
		wrapper.getDate().setText(df.format(transaction.getDate()) + " ");
		wrapper.getAmount().setText(
				GroupClearingApplication.getInstance()
						.formatCurrencyValueWithSymbol(transaction.getAmount(),
								transaction.getCurrency())
						+ " ");
		return rowView;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#hasStableIds()
	 */
	@Override
	public boolean hasStableIds() {
		return true;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return transactions.isEmpty();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#registerDataSetObserver(android.database.DataSetObserver)
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		observers.add(observer);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#unregisterDataSetObserver(android.database.DataSetObserver)
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		observers.remove(observer);
	}

	/** Notifies observers about data change.
	 */
	public void notifyDataSetChanged() {
		for (DataSetObserver observer : observers) {
			observer.onChanged();
		}
	}

	/* (non-Javadoc)
	 * @see android.widget.ListAdapter#areAllItemsEnabled()
	 */
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see android.widget.ListAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	/** Reads transactions from the database. Transactions are read
     * using <code>GCDatabase.readTransactionsOfEvent(long)</code>
     * function. After that observers are notified about data change
     * by calling <code>notifyDataSetChanged()</code> function.
	 */
	public void readTransactionsFromDB() {
		if (db == null) {
			db = new GCDatabase(context);
		}
		transactions = db.readTransactionsOfEvent(myEventId);
		notifyDataSetChanged();
	}

	/** Creates new transaction and adds it to the list. It is created
     * using <code>GCDatabase.createNewTransaction(long)</code>
     * function.
	 * @return Newly created transaction.
	 */
	public ClearingTransaction createTransaction() {
		ClearingTransaction aTransaction = null;
		try {
			aTransaction = db.createNewTransaction(myEventId);
			transactions.add(aTransaction);
			notifyDataSetChanged();
		} catch (GCEventDoesNotExistException e) {
			// TODO: We should warn user somehow.
		}
		return aTransaction;
	}

	/** Removes transaction at given position from the list and the
     * database.
	 * @param position Position of the transaction to be removed.
	 */
	public void removeTransactionAtPosition(int position) {
		if (position < transactions.size()) {
			db.deleteTransactionWithId(transactions.get(position).getId());
			transactions.remove(position);
			notifyDataSetChanged();
		}
	}

	/** Closes the database connection.
	 */
	public void closeDB() {
		if (db != null) {
			db.close();
		}
		db = null;
	}
}
