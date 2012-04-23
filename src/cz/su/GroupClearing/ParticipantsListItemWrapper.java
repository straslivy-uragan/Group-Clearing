package cz.su.GroupClearing;

import android.view.View;
import android.widget.TextView;

public class ParticipantsListItemWrapper {
	View base = null;
	TextView name = null;
	TextView balance = null;

	ParticipantsListItemWrapper(View aBase) {
		base = aBase;
	}

	TextView getName() {
		if (name == null) {
			name = (TextView) base.findViewById(R.id.participantsListItemName);
		}
		return name;
	}

	TextView getBalance() {
		if (balance == null) {
			balance = (TextView) base
					.findViewById(R.id.participantsListItemBalance);
		}
		return balance;
	}
}
