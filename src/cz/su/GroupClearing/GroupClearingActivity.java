package cz.su.GroupClearing;

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

/**
 * The main view activity. The activity with the list of events, these can be
 * deleted after a long click (enters context menu), or modified after a click
 * (enters event editor, i.e. <code>EventViewActivity</code>). A special row in
 * the list is used for adding new events, new event also can be created from
 * menu. From menu one can access info dialog and settings (
 * <code>SettingsActivity</code>), too.
 * 
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class GroupClearingActivity extends FragmentActivity {
	/**
	 * Adapter providing data for the event list. )
	 */
	private EventListAdapter eventListAdapter = null;
	/**
	 * Dialog with information about program.
	 */
	private InfoDialog infoDialog = null;

	/**
	 * Fragment class describing dialog with information about program.
	 * 
	 * @author Strašlivý Uragán
	 * @version 1.0
	 * @since 1.0
	 */
	public static class InfoDialog extends DialogFragment {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater
		 * , android.view.ViewGroup, android.os.Bundle)
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getDialog().setTitle(getString(R.string.app_name));
			View v = inflater.inflate(R.layout.about_dialog, container, false);
			return v;
		}
	}

	/** Called when the activity is first created. */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ListView lv = (ListView) findViewById(R.id.eventListView);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onEventClicked(position, id);
			}
		});
		eventListAdapter = new EventListAdapter(this);
		lv.setAdapter(eventListAdapter);
		registerForContextMenu(lv);
	}

	/**
	 * Opens <code>EventViewActivity</code> for viewing and modifying event with
	 * given <code>id</code>. The <code>id</code> is passed to
     * <code>EventViewActivity</code> through <code>Intent</code>
     * extra. Note, that if -1 is passed to
     * <code>EventViewActivity</code> as <code>id</code>, then new
     * event is created.
	 * 
	 * @param id
	 *            Id of event to be opened in <code>EventViewActivity</code>.
	 */
	public void showEventProperties(long id) {
		Intent intent = new Intent(this, EventViewActivity.class);
		intent.putExtra(EventViewActivity.EVENT_ID_PARAM_TAG, id);
		startActivity(intent);
	}

	/**
	 * Opens <code>SettingsActivity</code> with settings.
	 */
	public void showSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	/**
	 * Event handler reacts on click on event item. It calls
	 * <code>showEventProperties</code> with <code>id</code> as a parameter.
     * The <code>position</code> parameter is ignored.
	 * 
	 * @param position Position of the item with event within list of
     * events.
	 * @param id Id of the item with event, it is assumed to be equal
     * to event id or -1 if new event should be created.
	 */
	public void onEventClicked(final int position, long id) {
		showEventProperties(id);
	}

	/** Deletes event at given position within list of events and
     * given id. The <code>position</code> parameter is ignored and
     * <code>GCDatabase.deleteEventWithId(id)</code> is used to delete
     * the event.
	 * @param position Position of the item with event within list of
     * events.
	 * @param id Id of the item with event, it is assumed to be equal
     * to event id and especially should not be -1.
	 */
	public void deleteEvent(final int position, long id) {
		eventListAdapter.getDatabase().deleteEventWithId(id);
		refreshData();
	}

	/** Refreshes data in the list of events by querying database.
     * <code>EvenListAdapter.readEventsFromDB()</code> is used to
     * refresh data.
	 * 
	 */
	public void refreshData() {
		eventListAdapter.readEventsFromDB();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		refreshData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (eventListAdapter != null) {
			eventListAdapter.closeDB();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.group_clearing_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_about : {
				FragmentManager fm = getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				Fragment prev = getSupportFragmentManager().findFragmentByTag(
						"dialog");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);
				if (infoDialog == null) {
					infoDialog = new InfoDialog();
				}
				infoDialog.show(ft, "dialog");
				// showDialog(DIALOG_INFO_ID);
				return true;
			}
			case R.id.menu_new_event : {
				showEventProperties(-1);
				return true;
			}
			case R.id.menu_settings : {
				showSettings();
				return true;
			}
			default :
				return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.event_properties_menu, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.menu_event_delete : {
				deleteEvent(info.position, info.id);
			}
				return true;
			default :
				return super.onContextItemSelected(item);
		}
	}
}
