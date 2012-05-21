package cz.su.GroupClearing;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class ParticipantsListAdapter implements ListAdapter
{
   LayoutInflater inflater;
   Context context;
   ArrayList<DataSetObserver> observers;
   Vector<ClearingPerson> participants;
   long myEventId = -1;
   GCDatabase db = null;

   public static final int NORMAL_PARTICIPANT_TYPE = 0;

   public ParticipantsListAdapter(Context aContext, long eventId)
   {
      context = aContext;
      inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      observers = new ArrayList<DataSetObserver>();
      myEventId = eventId;
      readParticipantsFromDB();
   }

   @Override
   public int getCount()
   {
      return participants.size();
   }

   @Override
   public Object getItem(int position)
   {
      if (position < getCount())
      {
         return participants.get(position);
      }
      return null;
   }

   @Override
   public long getItemId(int position)
   {
      if (position < getCount())
      {
         return participants.get(position).getId();
      }
      return -1;
   }

   @Override
   public int getItemViewType(int position)
   {
      return NORMAL_PARTICIPANT_TYPE;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View rowView = null;
      ParticipantsListItemWrapper wrapper = null;
      if (convertView != null)
      {
         rowView = convertView;
         wrapper = (ParticipantsListItemWrapper) (rowView.getTag());
      }
      else
      {
         rowView = inflater.inflate(R.layout.participants_list_item, null);
         wrapper = new ParticipantsListItemWrapper(rowView);
         rowView.setTag(wrapper);
      }
      ClearingPerson person = participants.get(position);
      StringBuilder balanceBuilder = new StringBuilder();
      /*
       * balanceBuilder.append(myEvent.getDefaultCurrency());
       * balanceBuilder.append(' ');
       */
      balanceBuilder.append(person.getBalance());
      balanceBuilder.append(",- ");
      wrapper.getName().setText(person.getName());
      wrapper.getBalance().setText(balanceBuilder.toString());
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
      return participants.isEmpty();
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

   public void setNameOfParticipantAtPosition(int position, String name)
   {
      if (position < getCount())
      {
         ClearingPerson person = participants.get(position);
         person.setName(name);
         db.updateParticipantName(person);
         notifyDataSetChanged();
      }
   }

   public void createParticipantWithName(String name)
   {
      ClearingPerson aParticipant = db.createNewParticipant(myEventId, name);
      int index = 0;
      String realName = aParticipant.getName();
      while (index < participants.size()
            && realName.compareTo(participants.get(index).getName()) > 0)
      {
         ++ index;
      }
      participants.add(index, aParticipant);
      notifyDataSetChanged();
   }

   public void removeParticipantAtPosition(int position)
   {
      if (position < getCount())
      {
         db.deleteParticipantWithId(participants.get(position).getId());
         participants.remove(position);
         notifyDataSetChanged();
      }
   }

   public void readParticipantsFromDB()
   {
      if (db == null)
      {
         db = new GCDatabase(context);
      }
      participants = db.readParticipantsOfEvent(myEventId);
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

}
