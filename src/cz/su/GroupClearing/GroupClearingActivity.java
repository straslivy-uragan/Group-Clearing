package cz.su.GroupClearing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import cz.su.GroupClearing.GroupClearingApplication;
import cz.su.GroupClearing.EventViewActivity;
import cz.su.GroupClearing.GroupClearingException;

public class GroupClearingActivity extends FragmentActivity {
   //private Vector<Map<String,String>> listOfEventsForAdapter;
   private EventListAdapter eventListAdapter = null;
   private GroupClearingApplication myApplication = null;
   private InfoDialog infoDialog = null;

   public class InfoDialog extends DialogFragment {
      @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
               Bundle savedInstanceState) {
            getDialog().setTitle(getString(R.string.app_name));
            View v = inflater.inflate(R.layout.about_dialog, container, false);
            return v;
         }
   }

   // private static final int DIALOG_INFO_ID = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   listOfEventsForAdapter = new Vector<Map<String,String>>();
        myApplication = GroupClearingApplication.getInstance();
        
        
       // refreshData();
        
        //ListView lv = getListView();
        setContentView(R.layout.main);
        ListView lv = (ListView)findViewById(R.id.eventListView);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
             onEventClicked(position, id);
            }
          });
        eventListAdapter = new EventListAdapter(this);
        lv.setAdapter(eventListAdapter);
        registerForContextMenu(lv);
         try {
            myApplication.readEvents();
         }
         catch (GroupClearingException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.alert_title);
            builder.setMessage(e.getMessage());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                  //       showEventProperties(position);
                  }
                  });
            AlertDialog alert = builder.create();
            alert.show();
         }
    }
   
    public void showEventProperties(int position) {
       myApplication.prepareActiveEvent(position);
       Intent intent = new Intent(this, EventViewActivity.class);
       startActivity(intent);
    }

    public void onEventClicked(final int position, long id)
    {
       try {
          myApplication.saveModifiedEvents();
          showEventProperties(position);
       }
       catch (GroupClearingException e) {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setMessage(e.getMessage());
          builder.setTitle(R.string.alert_title);
          builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                showEventProperties(position);
                }
                });
          AlertDialog alert = builder.create();
          alert.show();
       }
    }

    public void deleteEvent (final int position, long id)
    {
       myApplication.deleteEventWithId((int)id);
       refreshData();
    }
    
    public void refreshData()
    {
       eventListAdapter.notifyDataSetChanged();
    }

   @Override
      protected void onStart() {
         super.onStart();
      }


    @Override
    protected void onResume()
    {
       super.onResume();
       refreshData();
    }

    @Override
    protected void onPause()
    {
       super.onPause();
       try {
          myApplication.saveModifiedEvents();
       }
       catch (GroupClearingException e) {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setMessage(e.getMessage());
          builder.setTitle(R.string.alert_title);
          builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
                });
          AlertDialog alert = builder.create();
          alert.show();
       }
}

@Override
       public boolean onCreateOptionsMenu(Menu menu) {
          MenuInflater inflater = getMenuInflater();
          inflater.inflate(R.menu.group_clearing_menu, menu);
          return true;
       }

@Override
       public boolean onOptionsItemSelected(MenuItem item) {
          // Handle item selection
          switch (item.getItemId()) {
             case R.id.menu_about:
                {
                   FragmentManager fm = getSupportFragmentManager();
                   FragmentTransaction ft = fm.beginTransaction();
                   Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                   if (prev != null) {
                      ft.remove(prev);
                   }
                   ft.addToBackStack(null);
                   if (infoDialog == null)
                   {
                      infoDialog = new InfoDialog();
                   }
                   infoDialog.show(ft, "dialog");
                   //showDialog(DIALOG_INFO_ID);
                   return true;
                }
             case R.id.menu_new_event:
             {
            	 showEventProperties(myApplication.getEvents().size());
                return true;
             }
             default:
                return super.onOptionsItemSelected(item);
          }
       }

@Override
public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
   super.onCreateContextMenu(menu, v, menuInfo);
   MenuInflater inflater = getMenuInflater();
   inflater.inflate(R.menu.event_properties_menu, menu);
}

@Override
public boolean onContextItemSelected(MenuItem item) {
   AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
   switch (item.getItemId()) {
      case R.id.menu_event_delete:
         {
            deleteEvent(info.position, info.id);
         }
         return true;
      default:
         return super.onContextItemSelected(item);
   }
}

  /*  @Override
       protected Dialog onCreateDialog(int id) {
          switch (id)
          {
             case DIALOG_INFO_ID:
             default:
                Dialog infoDialog = new Dialog(this);
                infoDialog.setContentView(R.layout.about_dialog);
                Resources myResources=getResources();
                infoDialog.setTitle(
                      myResources.getString(R.string.program_name));
                return infoDialog;
          }
       }*/
    }
