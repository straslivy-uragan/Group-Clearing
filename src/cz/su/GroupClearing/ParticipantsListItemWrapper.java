package cz.su.GroupClearing;

import android.view.View;
import android.widget.TextView;

/** A class wrapping an item of participants list. Stores
 * <code>TextView</code> with name of participant and
 * <code>TextView</code> with balance and the base
 * <code>LinearLayout</code> view. All that saves
 * calling <code>View.findViewById</code> too often.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class ParticipantsListItemWrapper {
    /** Base layout view of the item. */
	View base = null;
    /** <code>TextView</code> with name of participant. */
	TextView name = null;
    /** <code>TextView</code> with balance(s) of participant. */
	TextView balance = null;

    /** Constructs the wrapper with the base layout view.
     * @param aBase Base layout view of the item.
     */
	ParticipantsListItemWrapper(View aBase) {
		base = aBase;
	}

    /** Returns the <code>TextView</code> containing name of
     * the participant.
     * @return <code>TextView</code> containing name of the
     * participant.
     */
	TextView getName() {
		if (name == null) {
			name = (TextView) base.findViewById(R.id.participantsListItemName);
		}
		return name;
	}

    /** Returns the <code>TextView</code> containing balance(s) of the
     * participant.
     * @return <code>TextView</code> containing balance(s) of the
     * participant.
     */
	TextView getBalance() {
		if (balance == null) {
			balance = (TextView) base
					.findViewById(R.id.participantsListItemBalance);
		}
		return balance;
	}
}
