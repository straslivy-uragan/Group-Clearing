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

public class ParticipantsListAdapter implements ListAdapter {
	LayoutInflater inflater;
	Context context;
	ArrayList<DataSetObserver> observers;
	Vector<ClearingPerson> participants;
	long myEventId = -1;
	ClearingEvent myEvent = null;
	GCDatabase db = null;
	GroupClearingApplication myApp = GroupClearingApplication.getInstance();
    SortedMap<String, BigDecimal> sums = null;

	public static final int NORMAL_PARTICIPANT_TYPE = 0;

	public ParticipantsListAdapter(Context aContext, long eventId) {
		context = aContext;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		observers = new ArrayList<DataSetObserver>();
		myEventId = eventId;
		readParticipantsFromDB();
	}

	@Override
	public int getCount() {
		return participants.size();
	}

	public int getNumberOfParticipants() {
		return participants.size();
	}

	@Override
	public Object getItem(int position) {
		if (position < participants.size()) {
			return participants.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (position < participants.size()) {
			return participants.get(position).getId();
		}
		return -1;
	}

	@Override
	public int getItemViewType(int position) {
		return NORMAL_PARTICIPANT_TYPE;
	}

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

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return participants.isEmpty();
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

	public void setNameOfParticipantAtPosition(int position, String name) {
		if (position < participants.size()) {
			ClearingPerson person = participants.get(position);
			person.setName(name);
			db.updateParticipantName(person);
			notifyDataSetChanged();
		}
	}

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

	public void removeParticipantAtPosition(int position) {
		if (position < participants.size()) {
			db.deleteParticipantWithId(participants.get(position).getId());
			participants.remove(position);
            sums = null;
			notifyDataSetChanged();
		}
	}

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

	public BigDecimal getParticipantsValuesSum() {
		BigDecimal value = BigDecimal.ZERO;
		for (ClearingPerson participant : participants) {
			value = value.add(participant.getBalance());
		}
		return value;
	}

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

	public ClearingEvent getEvent() {
		return myEvent;
	}

	public void closeDB() {
		if (db != null) {
			db.close();
		}
		db = null;
	}

}
