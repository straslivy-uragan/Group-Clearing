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

public class EventListAdapter implements ListAdapter {
	public class EventDescription {
		String title;
		String subtitle;
		long id;

		public EventDescription(long id, String title, String subtitle) {
			Reset(id, title, subtitle);
		}

		public void Reset(long id, String title, String subtitle) {
			this.title = title;
			this.subtitle = subtitle;
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public String getSubtitle() {
			return subtitle;
		}

		public long getId() {
			return id;
		}
	}

	public static final int NORMAL_EVENT_TYPE = 0;
	public static final int ADD_EVENT_TYPE = 1;

	LayoutInflater inflater;
	Context context;
	ArrayList<DataSetObserver> observers;
	Vector<EventDescription> events;
	GCDatabase db = null;

	public EventListAdapter(Context aContext) {
		context = aContext;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		observers = new ArrayList<DataSetObserver>();
		events = new Vector<EventDescription>();
		readEventsFromDB();
	}

	@Override
	public int getCount() {
		return events.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		if (position < events.size()) {
			return events.elementAt(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (position < events.size()) {
			return events.elementAt(position).getId();
		}
		return -1;
	}

	@Override
	public int getItemViewType(int position) {
		return (position < events.size() ? NORMAL_EVENT_TYPE : ADD_EVENT_TYPE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		EventListItemWrapper wrapper = null;
		if (convertView != null) {
			rowView = convertView;
			wrapper = (EventListItemWrapper) (rowView.getTag());
		} else {
			if (getItemViewType(position) == NORMAL_EVENT_TYPE) {
				rowView = inflater.inflate(R.layout.event_list_item, null);
				wrapper = new EventListItemWrapper(rowView);
				rowView.setTag(wrapper);
			} else { // ADD_EVENT_TYPE
				rowView = inflater.inflate(R.layout.add_item, null);
			}
		}
		if (getItemViewType(position) == NORMAL_EVENT_TYPE) {
			EventDescription event = (EventDescription) getItem(position);
			wrapper.getTitle().setText(event.getTitle());
			wrapper.getSubtitle().setText(event.getSubtitle());
		} else { // ADD_EVENT_TYPE
			TextView text = (TextView) rowView.findViewById(R.id.add_item_text);
			text.setText(context.getString(R.string.add_new_event) + " ");
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
		return events.isEmpty();
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

	public void readEventsFromDB() {
		if (db == null) {
			db = new GCDatabase(context);
		}

		DateFormat df = DateFormat.getDateInstance();

		ClearingEvent[] eventsArray = db.readEvents();
		int row = 0;
        if (eventsArray != null) {
            for (row = 0; row < eventsArray.length; ++row) {
                long id = eventsArray[row].getId();
                String name = eventsArray[row].getName();
                StringBuilder subtitleBuilder = new StringBuilder();
                subtitleBuilder.append(df.format(eventsArray[row].getStartDate()));
                subtitleBuilder.append(" -- ");
                subtitleBuilder.append(df.format(eventsArray[row].getFinishDate()));
                subtitleBuilder.append(' ');
                String subtitle = subtitleBuilder.toString();
                if (row < events.size()) {
                    events.get(row).Reset(id, name, subtitle);
                } else {
                    EventDescription desc = new EventDescription(id, name, subtitle);
                    events.add(desc);
                }
            }
        }
		while (row < events.size()) {
			events.remove(row);
		}
		notifyDataSetChanged();
	}

	public void closeDB() {
		if (db != null) {
			db.close();
		}
		db = null;
	}

	public GCDatabase getDatabase() {
		return db;
	}
}
