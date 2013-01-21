package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

/** Adapter for populating list of participants. Depending on the
 * status of <code>convertToEventCurrency</code> flag, it either shows
 * only one balance in an participant item, or it shows all balances
 * in different currencies used in transactions within the event. The
 * layout for item is described in
 * <code>participants_list_item.xml</code>. The base widget in this
 * layout is <code>LinearLayout</code> in case of multiple currency
 * balances, these are added as <code>TextView</code> widget to this
 * <code>LinearLayout</code>. In case of one balance, the
 * <code>participantsListItemBalance</code> <code>TextView</code> is
 * used. This is done in <code>getView</code> function.
 *
 * The list of participants is kept sorted lexicographically by their
 * names.
 *
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class ParticipantsListAdapter implements ListAdapter {
    /** Inflater used for inflating layouts from resources. */
	LayoutInflater inflater;
    /** Context of this adapter. */
	Context context;
    /** List of observers of this adapter. */
	ArrayList<DataSetObserver> observers;
    /** Array of participants in the list. */
	Vector<ClearingPerson> participants;
    /** Id of event containing the participants in the list. */
	long myEventId = -1;
    /** Event containing the participants in the list. */
	ClearingEvent myEvent = null;
    /** Database object representing connection to the database. */
	GCDatabase db = null;
    /** The application object for accessing global preferences and
     * other shared data. */
	GroupClearingApplication myApp = GroupClearingApplication.getInstance();
    /** Maps sum values to string names of currencies. */
    SortedMap<String, BigDecimal> sums = null;

	/** Constant determining normal participant item type.
	 */
	public static final int NORMAL_PARTICIPANT_TYPE = 0;

	/** Constructs object with given context and event id.
	 * @param aContext Context in which this adapter works.
	 * @param eventId Id of event containing the participants in the
     * list.
	 */
	public ParticipantsListAdapter(Context aContext, long eventId) {
		context = aContext;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		observers = new ArrayList<DataSetObserver>();
		myEventId = eventId;
		readParticipantsFromDB();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return participants.size();
	}

	/** Returns the number of participants in this list.
	 * @return The number of participants in the list.
	 */
	public int getNumberOfParticipants() {
		return participants.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		if (position < participants.size()) {
			return participants.get(position);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		if (position < participants.size()) {
			return participants.get(position).getId();
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		return NORMAL_PARTICIPANT_TYPE;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		ParticipantsListItemWrapper wrapper = null;
		if (convertView != null) {
			rowView = convertView;
			wrapper = (ParticipantsListItemWrapper) (rowView.getTag());
		} else {
			rowView = inflater.inflate(R.layout.participants_list_item, null);
			wrapper = new ParticipantsListItemWrapper(rowView);
			rowView.setTag(wrapper);
		}
		ClearingPerson person = participants.get(position);
		wrapper.getName().setText(person.getName());
		if (myApp.getConvertToEventCurrency()) {
			wrapper.getBalance().setText(
					myApp.formatCurrencyValueWithSymbol(person.getBalance(),
							myEvent.getDefaultCurrency()) + " ");
			if (person.getBalance().signum() > 0) {
				wrapper.getBalance().setTextColor(android.graphics.Color.GREEN);
			} else if (person.getBalance().signum() < 0) {
				wrapper.getBalance().setTextColor(android.graphics.Color.RED);
			} else {
				wrapper.getBalance().setTextColor(
						context.getResources().getColor(
								android.R.color.primary_text_dark));
			}
		} else {
			LinearLayout layout = (LinearLayout) rowView;
			int index = 1;
			for (Map.Entry<String, BigDecimal> entry : person.getAllBalances()
					.entrySet()) {
				TextView balance = null;
				if (index < layout.getChildCount()) {
					balance = (TextView) layout.getChildAt(index);
				} else {
					balance = new TextView(context);
					balance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					balance.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
                    LinearLayout.LayoutParams params = new
                        LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.RIGHT;
					layout.addView(balance, params);
				}
				balance.setText(myApp.formatCurrencyValueWithSymbol(
						entry.getValue(), Currency.getInstance(entry.getKey()))
						+ " ");
				if (entry.getValue().signum() > 0) {
					balance.setTextColor(android.graphics.Color.GREEN);
				} else if (entry.getValue().signum() < 0) {
					balance.setTextColor(android.graphics.Color.RED);
				} else {
					balance.setTextColor(context.getResources().getColor(
							android.R.color.primary_text_dark));
				}
				++index;
			}
			if (index + 1 < layout.getChildCount()) {
				layout.removeViews(index + 1, layout.getChildCount() - index
						- 1);
			}
		}
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
		return participants.isEmpty();
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

	/** Notifies observers of the adapter, that data set has changed.
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

	/** Sets name of the participant at given position. It also
     * updates the name within database.
	 * @param position Position of participant within the list.
	 * @param name New name of participant.
	 */
	public void setNameOfParticipantAtPosition(int position, String name) {
		if (position < participants.size()) {
			ClearingPerson person = participants.get(position);
			person.setName(name);
			db.updateParticipantName(person);
			notifyDataSetChanged();
		}
	}

	/** Creates new participant with given name and adds it to the
     * list. The participant is inserted into the list at position
     * given by the name (the list is sorted lexicographically by
     * names of participants). User is created using
     * <code>GCDatabase.createNewParticipant(long,String)</code>
	 * @param name Name of participant being created.
	 */
	public void createParticipantWithName(String name) {
		ClearingPerson aParticipant = db.createNewParticipant(myEventId, name);
		int index = 0;
		String realName = aParticipant.getName();
		while (index < participants.size()
				&& realName.compareTo(participants.get(index).getName()) > 0) {
			++index;
		}
		participants.add(index, aParticipant);
        sums = null;
		notifyDataSetChanged();
	}

	/** Deletes the participant at given position. The participant is
     * also deleted in database.
	 * @param position Position of participants to be deleted.
	 */
	public void removeParticipantAtPosition(int position) {
		if (position < participants.size()) {
			db.deleteParticipantWithId(participants.get(position).getId());
			participants.remove(position);
            sums = null;
			notifyDataSetChanged();
		}
	}

	/** Reads the participants from database.
	 */
	public void readParticipantsFromDB() {
		if (db == null) {
			db = new GCDatabase(context);
		}
		myEvent = db.readEventWithId(myEventId);
		participants = db.readParticipantsOfEvent(myEventId, myApp
				.getConvertToEventCurrency()
				? GCDatabase.ComputeBalance.COMPUTE_CUMULATIVE
				: GCDatabase.ComputeBalance.COMPUTE_ALL);
        sums = null;
		notifyDataSetChanged();
	}

	/** Returns sum of balances of participants. Cumulative balances
     * over all currencies as returned by
     * <code>ClearingPerson.getBalance()</code> are summed.
	 * @return The sum of balances of participants.
	 */
	public BigDecimal getParticipantsValuesSum() {
		BigDecimal value = BigDecimal.ZERO;
		for (ClearingPerson participant : participants) {
			value = value.add(participant.getBalance());
		}
		return value;
	}

    /** Computes all balances per different currencies. Returns them
     * as a <code>Map</code> mapping a value to a <code>String</code>
     * code of currency. The balances are obtained using
     * <code>ClearingPerson.getAllBalances()</code>.
     * @return <code>SortedMap</code> mapping <code>BigDecimal</code>
     * values to <code>String</code> currency codes.
     */
    SortedMap<String, BigDecimal> getAllParticipantsValuesSums() {
        if (sums != null) {
            return sums;
        }
        sums = new TreeMap<String, BigDecimal>();
        for (ClearingPerson person : participants) {
            for (Map.Entry<String, BigDecimal> entry :
                    person.getAllBalances().entrySet()) {
                BigDecimal curSum = sums.get(entry.getKey());
                if (curSum == null) {
                    sums.put(entry.getKey(), entry.getValue());
                } else {
                    sums.put(entry.getKey(), curSum.add(entry.getValue()));
                }
            }
        }
        return sums;
    }

	/** Returns the event containing persons in the list.
	 * @return Event containing persons in the list.
	 */
	public ClearingEvent getEvent() {
		return myEvent;
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
