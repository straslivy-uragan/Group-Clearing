package cz.su.GroupClearing;

import android.view.View;
import android.widget.TextView;

/** Wrapper for list item in event list. Object serving as a wrapper
 * for view with item in event list. It is initialized with row view
 * and it then can provide inner <code>TextView</code> object thus
 * saving callings of <code>findViewById</code>.
 * @author Strašlivý Uragán <straslivy.uragan@gmail.com>
 * @version 1.0
 * @since 1.0
 */
public class EventListItemWrapper {
    /** Base view being wrapped. */
	View base = null;
    /** <code>TextView</code> with title. */
	TextView title = null;
    /** <code>TextView</code> with subtitle. */
	TextView subtitle = null;

	/** Initializes object with given base view.
	 * @param aBase Base view being wrapped.
	 */
	EventListItemWrapper(View aBase) {
		base = aBase;
	}

	/** Return <code>TextView</code> with title. First time,
     * <code>View.findViewById(String)</code> is called to set
     * <code>title</code> value, then this value is returned in
     * subsequent calls.
	 * @return <code>TextView</code> object with title.
	 */
	TextView getTitle() {
		if (title == null) {
			title = (TextView) base.findViewById(R.id.eventListItemTitle);
		}
		return title;
	}

    /** Return <code>TextView</code> with subtitle. First time,
     * <code>View.findViewById(String)</code> is called to set
     * <code>subtitle</code> value, then this value is returned in
     * subsequent calls.
	 * @return <code>TextView</code> object with subtitle.
	 */
	TextView getSubtitle() {
		if (subtitle == null) {
			subtitle = (TextView) base.findViewById(R.id.eventListItemSubtitle);
		}
		return subtitle;
	}
}
