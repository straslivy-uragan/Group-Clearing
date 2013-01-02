package cz.su.GroupClearing;

import android.view.View;
import android.widget.TextView;

/** Wrapper of a layout view of an item of a list of transactions.
 * Used in <code>TransactionsListAdapter</code> to wrap a layout of an
 * item of the list.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class TransactionsListItemWrapper {
	/** Base view of the item layout.
	 */
	View base = null;
	/** <code>TextView</code> displaying the name of the transaction.
	 */
	TextView name = null;
	/** <code>TextView</code> displaying the date of the transaction.
	 */
	TextView date = null;
	/** <code>TextView</code> displaying the amount of the
     * transaction.
     */
	TextView amount = null;

	/** Constructs new item wrapper for given base view.
	 * @param base Base view of the layout of the item.
	 */
	TransactionsListItemWrapper(View base) {
		this.base = base;
	}

	/** Returns the <code>TextView</code> displaying the name of the
     * transaction.
	 * @return The <code>TextView</code> displaying the name of the
     * transaction.
	 */
	TextView getName() {
		if (name == null) {
			name = (TextView) base.findViewById(R.id.transactionsListItemName);
		}
		return name;
	}

	/** Returns the <code>TextView</code> displaying the date of the
     * transaction.
	 * @return The <code>TextView</code> displaying the date of the
     * transaction.
	 */
	TextView getDate() {
		if (date == null) {
			date = (TextView) base.findViewById(R.id.transactionsListItemDate);
		}
		return date;
	}

    /** Returns the <code>TextView</code> displaying the amount of the
     * transaction.
	 * @return The <code>TextView</code> displaying the amount of the
     * transaction.
	 */
	TextView getAmount() {
		if (amount == null) {
			amount = (TextView) base
					.findViewById(R.id.transactionsListItemAmount);
		}
		return amount;
	}

}
