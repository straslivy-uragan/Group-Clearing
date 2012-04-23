package cz.su.GroupClearing;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class ParticipantsListAdapter implements ListAdapter {
	LayoutInflater inflater;
    Context context;
    GroupClearingApplication myApplication;
    ClearingEvent myEvent;
    ArrayList<DataSetObserver> observers;

    public static final int NORMAL_PARTICIPANT_TYPE = 0;

    public ParticipantsListAdapter(Context aContext) {
    	myApplication = GroupClearingApplication.getInstance();
      myEvent = myApplication.getActiveEvent();
    	context = aContext;
      inflater = (LayoutInflater)context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
      observers = new ArrayList<DataSetObserver>();
   }

   @Override
      public int getCount() {
         return myEvent.getNumberOfParticipants();
      }

   @Override
      public Object getItem(int position) {
         return myEvent.getParticipant(position);
      }

   @Override
      public long getItemId(int position) {
         if (position < myEvent.getNumberOfParticipants())
         {
            return myEvent.getParticipant(position).getId();
         }
         return 0;
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
            wrapper = (ParticipantsListItemWrapper)(rowView.getTag());
         }
         else
         {
            rowView = inflater.inflate(R.layout.participants_list_item, null);
            wrapper = new ParticipantsListItemWrapper(rowView);
            rowView.setTag(wrapper);
         }
         ClearingPerson person = myEvent.getParticipant(position);
         StringBuilder balanceBuilder = new StringBuilder();
         /*balanceBuilder.append(myEvent.getDefaultCurrency());
           balanceBuilder.append(' ');*/
         balanceBuilder.append(person.getBalance());
         balanceBuilder.append(",- ");
         wrapper.getName().setText(person.getName());
         wrapper.getBalance().setText(balanceBuilder.toString());
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
         return myEvent.getNumberOfParticipants() > 0;
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
