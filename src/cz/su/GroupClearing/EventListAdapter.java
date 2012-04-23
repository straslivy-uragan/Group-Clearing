package cz.su.GroupClearing;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class EventListAdapter implements ListAdapter {
   Activity myActivity;
	LayoutInflater inflater;
    Context context;
    GroupClearingApplication myApplication;
    ArrayList<DataSetObserver> observers;

    public static final int NORMAL_EVENT_TYPE = 0;

   public class EventDescription {
      String title;
      String subtitle;
      int id;

      public EventDescription(String aTitle, String aSubtitle, int anId) {
         title = aTitle;
         subtitle = aSubtitle;
         id = anId;
      }
   }
    
    public EventListAdapter(Context aContext) {
    	myApplication = GroupClearingApplication.getInstance();
      myActivity = (Activity) aContext;
    	context = aContext;
      inflater = (LayoutInflater)context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
      observers = new ArrayList<DataSetObserver>();
   }

   @Override
      public int getCount() {
         return myApplication.getEvents().size();
      }

   @Override
      public Object getItem(int position) {
         if (position < myApplication.getEvents().size())
         {
            return myApplication.getEvents().elementAt(position);
         }
         return null;
      }

   @Override
      public long getItemId(int position) {
         if (position < myApplication.getEvents().size())
         {
            return myApplication.getEvents().elementAt(position).getId();
         }
         return 0;
      }

   @Override
      public int getItemViewType(int position) {
         return NORMAL_EVENT_TYPE;
      }

   @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         View rowView = null;
         EventListItemWrapper wrapper = null;
         if (convertView != null) {
            rowView = convertView;
            wrapper = (EventListItemWrapper)(rowView.getTag());
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
            ClearingEvent event = (ClearingEvent)getItem(position);
            wrapper.getTitle().setText(event.getName());
            wrapper.getSubtitle().setText(event.getSubtitle());
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
         return (myApplication.getEvents().size() > 0);
      }

   @Override
      public void registerDataSetObserver(DataSetObserver observer) {
	   	observers.add(observer);
      }

   @Override
      public void unregisterDataSetObserver(DataSetObserver observer) {
         observers.remove(observer);
      }

   public void notifyDataSetChanged(){
      for (DataSetObserver observer: observers) {
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


}
