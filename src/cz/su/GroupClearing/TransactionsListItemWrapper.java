package cz.su.GroupClearing;

import android.view.View;
import android.widget.TextView;

public class TransactionsListItemWrapper {
	View base = null;
	TextView name = null;
	TextView date = null;
   TextView amount = null;

	TransactionsListItemWrapper(View aBase) {
		base = aBase;
	}

	TextView getName() {
		if (name == null) {
			name = (TextView) base.findViewById(R.id.transactionsListItemName);
		}
		return name;
	}

	TextView getDate() {
		if (date == null) {
         date = (TextView) base.findViewById(R.id.transactionsListItemDate);
		}
		return date;
	}

   TextView getAmount() {
      if (amount == null) {
         amount = (TextView) base.findViewById(R.id.transactionsListItemAmount);
      }
      return amount;
   }

}
