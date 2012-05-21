package cz.su.GroupClearing;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class EventListAdapter implements ListAdapter
{
   public class EventDescription
   {
      String title;
      String subtitle;
      int id;

      public EventDescription(int anId, String aTitle, String aSubtitle)
      {
         Reset(anId, aTitle, aSubtitle);
      }

      public void Reset(int anId, String aTitle, String aSubtitle)
      {
         title = aTitle;
         subtitle = aSubtitle;
         id = anId;
      }

      public String getTitle()
      {
         return title;
      }

      public String getSubtitle()
      {
         return subtitle;
      }

      public int getId()
      {
         return id;
      }
   }

   public static final int NORMAL_EVENT_TYPE = 0;
   public static final String EVENT_DESCRIPTION_QUERY = "select "
         + GCDatabaseHelper.TE_ID + ", " + GCDatabaseHelper.TE_NAME + ", "
         + GCDatabaseHelper.TE_START_DATE + ", "
         + GCDatabaseHelper.TE_FINISH_DATE + " from "
         + GCDatabaseHelper.TABLE_EVENTS;

   LayoutInflater inflater;
   Context context;
   ArrayList<DataSetObserver> observers;
   Vector<EventDescription> events;
   GCDatabase db = null;

   public EventListAdapter(Context aContext)
   {
      context = aContext;
      inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      observers = new ArrayList<DataSetObserver>();
      events = new Vector<EventDescription>();
      readEventsFromDB();
   }

   @Override
   public int getCount()
   {
      return events.size();
   }

   @Override
   public Object getItem(int position)
   {
      if (position < events.size())
      {
         return events.elementAt(position);
      }
      return null;
   }

   @Override
   public long getItemId(int position)
   {
      if (position < events.size())
      {
         return events.elementAt(position).getId();
      }
      return 0;
   }

   @Override
   public int getItemViewType(int position)
   {
      return NORMAL_EVENT_TYPE;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View rowView = null;
      EventListItemWrapper wrapper = null;
      if (convertView != null)
      {
         rowView = convertView;
         wrapper = (EventListItemWrapper) (rowView.getTag());
      }
      else
      {
         if (getItemViewType(position) == NORMAL_EVENT_TYPE)
         {
            rowView = inflater.inflate(R.layout.event_list_item, null);
            wrapper = new EventListItemWrapper(rowView);
            rowView.setTag(wrapper);
         }
      }
      if (getItemViewType(position) == NORMAL_EVENT_TYPE)
      {
         EventDescription event = (EventDescription) getItem(position);
         wrapper.getTitle().setText(event.getTitle());
         wrapper.getSubtitle().setText(event.getSubtitle());
      }
      return rowView;
   }

   @Override
   public int getViewTypeCount()
   {
      return 1;
   }

   @Override
   public boolean hasStableIds()
   {
      return true;
   }

   @Override
   public boolean isEmpty()
   {
      return events.isEmpty();
   }

   @Override
   public void registerDataSetObserver(DataSetObserver observer)
   {
      observers.add(observer);
   }

   @Override
   public void unregisterDataSetObserver(DataSetObserver observer)
   {
      observers.remove(observer);
   }

   public void notifyDataSetChanged()
   {
      for (DataSetObserver observer : observers)
      {
         observer.onChanged();
      }
   }

   @Override
   public boolean areAllItemsEnabled()
   {
      return true;
   }

   @Override
   public boolean isEnabled(int position)
   {
      return true;
   }

   public void readEventsFromDB()
   {
      if (db == null)
      {
         db = new GCDatabase(context);
      }
      Cursor result = db.getDB().rawQuery(EVENT_DESCRIPTION_QUERY, null);
      int row = 0;
      result.moveToFirst();
      SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      DateFormat df = DateFormat.getDateInstance();
      while (!result.isAfterLast())
      {
         int id = result.getInt(0);
         String name = result.getString(1);
         StringBuilder subtitleBuilder = new StringBuilder();
         Date startDate = dateParser.parse(result.getString(2),
               new ParsePosition(0));
         subtitleBuilder.append(df.format(startDate));
         subtitleBuilder.append(" -- ");
         Date finishDate = dateParser.parse(result.getString(3),
               new ParsePosition(0));
         subtitleBuilder.append(df.format(finishDate));
         String subtitle = subtitleBuilder.toString();
         if (row < events.size())
         {
            events.get(row).Reset(id, name, subtitle);
         }
         else
         {
            EventDescription desc = new EventDescription(id, name, subtitle);
            events.add(desc);
         }
         ++row;
         result.moveToNext();
      }
      result.close();
      while (row < events.size())
      {
         events.remove(row);
      }
      notifyDataSetChanged();
   }

   public void closeDB()
   {
      if (db != null)
      {
         db.close();
      }
      db = null;
   }

   public GCDatabase getDatabase() 
   {
      return db;
   }
}
