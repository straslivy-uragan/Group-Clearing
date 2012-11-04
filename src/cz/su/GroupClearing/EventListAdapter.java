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

/** List adapter for list of events. This class serves as a source of
 * date for list of events.
 *
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class EventListAdapter implements ListAdapter {
	/** A description of event. This description only contains data which are
     * necessary for the list of events in <code>GroupClearingActivity</code>.
     *
     * @author Strašlivý Uragán
     * @version 1.0
     * @since 1.0
	 */
	public class EventDescription {
        /** Title of the list element. Contains name of the event.
         */
		String title;
        /** Precomputed subtitle of the list element. Consists of start
         * and finish dates.
         */
		String subtitle;
        /** Id of the represented event.
         */
		long id;

		/** Creates new object. 
         *
		 * @param id Id of event to be described.
		 * @param title Title of the list element.
		 * @param subtitle Subtitle of the list element. Consists of
         * start and finish dates.
		 */
		public EventDescription(long id, String title, String subtitle) {
			Reset(id, title, subtitle);
		}

		/** Resets the values with given ones.
         *
		 * @param id Id of event to be described.
		 * @param title Title of the list element.
		 * @param subtitle Subtitle of the list element. Consists of
         * start and finish dates.
		 */
		public void Reset(long id, String title, String subtitle) {
			this.title = title;
			this.subtitle = subtitle;
			this.id = id;
		}

		/** Returns the title of the list element.
		 * @return Title of the list element, i.e. the event's name.
		 */
		public String getTitle() {
			return title;
		}

		/** Returns subtitle of the list element.
		 * @return Subtitle of the list element. This consists of
         * start and finish dates.
		 */
		public String getSubtitle() {
			return subtitle;
		}

		/** Returns id of the event being described by this object.
		 * @return Id of the event being described by this object.
		 */
		public long getId() {
			return id;
		}
	}

	/** Constant identifying normal type of the list element.
	 */
	public static final int NORMAL_EVENT_TYPE = 0;
	/** Constant identifying the list element for adding new event.
	 */
	public static final int ADD_EVENT_TYPE = 1;

	/** Inflater used for inflating element views.
	 */
	LayoutInflater inflater;
	/** Context of the list to which this adapter should provide data.
	 */
	Context context;
	/** List of observers.
	 */
	ArrayList<DataSetObserver> observers;
	/** Events in the list.
	 */
	Vector<EventDescription> events;
	/** Database interface object.
	 */
	GCDatabase db = null;

	/** Creates new event list adapter with given context.
	 * @param aContext Context of the list this adapter should provide
     * data to.
	 */
	public EventListAdapter(Context aContext) {
		context = aContext;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		observers = new ArrayList<DataSetObserver>();
		events = new Vector<EventDescription>();
		readEventsFromDB();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return events.size() + 1;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		if (position < events.size()) {
			return events.elementAt(position);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		if (position < events.size()) {
			return events.elementAt(position).getId();
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		return (position < events.size() ? NORMAL_EVENT_TYPE : ADD_EVENT_TYPE);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
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

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		return 2;
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
		return events.isEmpty();
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

	/** Notifies observers, that data set changed.
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

	/** Reads the events from database and stores their description in
     * <code>events</code>. For every event title and subtitle are
     * constructed. Title is simply the name of event, while subtitle
     * is composed based on start and finish dates of the event. This
     * is also the reason, why description object is used instead of
     * storing <code>ClearingEvent</code> objects directly, to have
     * precomputed subtitle saves further computations.
	 */
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

	/** Closes database connection.
	 */
	public void closeDB() {
		if (db != null) {
			db.close();
		}
		db = null;
	}

	/** Returns database object.
	 * @return Database object <code>db</code>.
	 */
	public GCDatabase getDatabase() {
		return db;
	}
}
