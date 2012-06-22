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
import android.widget.TextView;

public class TransactionsListAdapter implements ListAdapter {
	LayoutInflater inflater;
	Context context;
	long myEventId = -1;
	ArrayList<DataSetObserver> observers;
	Vector<ClearingTransaction> transactions;
	GCDatabase db = null;

	public static final int NORMAL_TRANSACTION_TYPE = 0;
	public static final int ADD_TRANSACTION_TYPE = 1;

	public TransactionsListAdapter(Context aContext, long eventId) {
		myEventId = eventId;
		context = aContext;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		observers = new ArrayList<DataSetObserver>();
		readTransactionsFromDB();
	}

	@Override
	public int getCount() {
		return transactions.size() + 1;
	}

	public int getNumberOfTransactions() {
		return transactions.size();
	}

	@Override
	public Object getItem(int position) {
		if (position < transactions.size()) {
			return transactions.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (position < transactions.size()) {
			return transactions.get(position).getId();
		}
		return -1;
	}

	@Override
	public int getItemViewType(int position) {
		return (position < transactions.size() ? NORMAL_TRANSACTION_TYPE
				: ADD_TRANSACTION_TYPE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		TransactionsListItemWrapper wrapper = null;
		if (convertView != null) {
			rowView = convertView;
			wrapper = (TransactionsListItemWrapper) (rowView.getTag());
		} else {
			if (getItemViewType(position) == NORMAL_TRANSACTION_TYPE) {
				rowView = inflater.inflate(R.layout.transactions_list_item,
						null);
				wrapper = new TransactionsListItemWrapper(rowView);
				rowView.setTag(wrapper);
			} else { // ADD_TRANSACTION_TYPE
				rowView = inflater.inflate(R.layout.add_item, null);
			}
		}
		if (getItemViewType(position) == NORMAL_TRANSACTION_TYPE) {
			ClearingTransaction transaction = transactions.get(position);
			DateFormat df = DateFormat.getDateInstance();
			wrapper.getName().setText(transaction.getName());
			wrapper.getDate().setText(df.format(transaction.getDate()) + " ");
			wrapper.getAmount().setText(
					GroupClearingApplication.getInstance()
							.formatCurrencyValueWithSymbol(
									transaction.getAmount(),
									transaction.getCurrency())
							+ " ");
		} else { // ADD_TRANSACTION_TYPE
			TextView text = (TextView)rowView.findViewById(R.id.add_item_text);
			text.setText(context.getString(R.string.add_new_transaction) + " ");
		}
		return rowView;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return transactions.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		observers.remove(observer);
	}

	public void notifyDataSetChanged() {
		for (DataSetObserver observer : observers) {
			observer.onChanged();
		}
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	public void readTransactionsFromDB() {
		if (db == null) {
			db = new GCDatabase(context);
		}
		transactions = db.readTransactionsOfEvent(myEventId);
		notifyDataSetChanged();
	}

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

	public void removeTransactionAtPosition(int position) {
		if (position < transactions.size()) {
			db.deleteTransactionWithId(transactions.get(position).getId());
			transactions.remove(position);
			notifyDataSetChanged();
		}
	}

	public void closeDB() {
		if (db != null) {
			db.close();
		}
		db = null;
	}
}
