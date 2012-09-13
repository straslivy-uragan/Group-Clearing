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

public class GroupClearingActivity extends FragmentActivity {
	// private Vector<Map<String,String>> listOfEventsForAdapter;
	private EventListAdapter eventListAdapter = null;
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

	public void showEventProperties(long id) {
		Intent intent = new Intent(this, EventViewActivity.class);
		intent.putExtra("cz.su.GroupClearing.EventId", id);
		startActivity(intent);
	}

	public void showSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void onEventClicked(final int position, long id) {
		showEventProperties(id);
	}

	public void deleteEvent(final int position, long id) {
		eventListAdapter.getDatabase().deleteEventWithId(id);
		refreshData();
	}

	public void refreshData() {
		eventListAdapter.readEventsFromDB();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (eventListAdapter != null) {
			eventListAdapter.closeDB();
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.event_properties_menu, menu);
	}

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
