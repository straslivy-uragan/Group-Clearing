package cz.su.GroupClearing;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

	public static final int NORMAL_PARTICIPANT_TYPE = 0;
	public static final int ADD_PARTICIPANT_TYPE = 1;

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
		return participants.size() + 1;
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
		return (position < participants.size() ? NORMAL_PARTICIPANT_TYPE
				: ADD_PARTICIPANT_TYPE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		ParticipantsListItemWrapper wrapper = null;
		if (convertView != null) {
			rowView = convertView;
			wrapper = (ParticipantsListItemWrapper) (rowView.getTag());
		} else {
			if (getItemViewType(position) == NORMAL_PARTICIPANT_TYPE) {
				rowView = inflater.inflate(R.layout.participants_list_item,
						null);
				wrapper = new ParticipantsListItemWrapper(rowView);
				rowView.setTag(wrapper);
			} else { // ADD_PARTICIPANT_TYPE
				rowView = inflater.inflate(R.layout.add_item, null);
			}
		}
		if (getItemViewType(position) == NORMAL_PARTICIPANT_TYPE) {
			ClearingPerson person = participants.get(position);
			wrapper.getName().setText(person.getName());
			wrapper.getBalance().setText(
					myApp.formatCurrencyValueWithSymbol(person.getBalance(),
							myEvent.getDefaultCurrency()) + " ");
            if (person.getBalance() > 0) {
                wrapper.getBalance().setTextColor(android.graphics.Color.GREEN);
            } else if (person.getBalance() < 0) {
                wrapper.getBalance().setTextColor(android.graphics.Color.RED);
            } else {
                wrapper.getBalance().setTextColor(context.getResources().getColor(
                            android.R.color.primary_text_dark));
            }
		} else { // ADD_PARTICIPANT_TYPE
			TextView text = (TextView) rowView.findViewById(R.id.add_item_text);
			text.setText(context.getString(R.string.add_new_participant) + " ");
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
		notifyDataSetChanged();
	}

	public void removeParticipantAtPosition(int position) {
		if (position < participants.size()) {
			db.deleteParticipantWithId(participants.get(position).getId());
			participants.remove(position);
			notifyDataSetChanged();
		}
	}

	public void readParticipantsFromDB() {
		if (db == null) {
			db = new GCDatabase(context);
		}
		myEvent = db.readEventWithId(myEventId);
		participants = db.readParticipantsOfEvent(myEventId);
		notifyDataSetChanged();
	}

	public void closeDB() {
		if (db != null) {
			db.close();
		}
		db = null;
	}

}
