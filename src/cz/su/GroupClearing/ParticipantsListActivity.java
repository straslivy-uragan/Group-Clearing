package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.SortedMap;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/** Activity with the list of participants of an event.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class ParticipantsListActivity extends FragmentActivity
		implements
			EditParticipantDialog.EditParticipantListener {

	/**
 * 
 */
	private long myEventId = -1;
	/**
 * 
 */
	private ParticipantsListAdapter participantsListAdapter = null;
	/**
 * 
 */
	private TextView sumValueText = null;
	/**
 * 
 */
	private LinearLayout sumList = null;
	/**
 * 
 */
	private final GroupClearingApplication myApp = GroupClearingApplication
			.getInstance();
	// private EditParticipantDialog editParticipantDialog = null;

	/**
 * 
 */
	public final static String EDIT_PARTICIPANT_DLG_TAG = "edit_participant_dialog";

	/**
	 * Tag determining the event id parameter of the activity.
	 */
	public static final String EVENT_ID_PARAM_TAG = "cz.su.GroupClearing.EventId";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (myApp.getConvertToEventCurrency()) {
			setContentView(R.layout.participants_list);
			sumValueText = (TextView) findViewById(R.id.pl_sum_value);
		} else {
			setContentView(R.layout.participants_list_multc);
			sumList = (LinearLayout) findViewById(R.id.pl_sum_list);
		}
		ListView lv = (ListView) findViewById(R.id.participants_list_view);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onPersonClicked(position, id);
			}
		});
		myEventId = getIntent().getLongExtra(EVENT_ID_PARAM_TAG, -1);
		participantsListAdapter = new ParticipantsListAdapter(this, myEventId);
		lv.setAdapter(participantsListAdapter);
		registerForContextMenu(lv);
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
		if (participantsListAdapter != null) {
			participantsListAdapter.closeDB();
		}
	}

	/**
	 * @param position
	 * @param id
	 */
	public void onPersonClicked(final int position, long id) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(
				EDIT_PARTICIPANT_DLG_TAG);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		EditParticipantDialog editParticipantDialog = new EditParticipantDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(EditParticipantDialog.POSITION_TAG, position);

		if (position < participantsListAdapter.getNumberOfParticipants()) {
			bundle.putString(
					EditParticipantDialog.NAME_TAG,
					((ClearingPerson) participantsListAdapter.getItem(position))
							.getName());
		}
		editParticipantDialog.setArguments(bundle);
		editParticipantDialog.show(ft, EDIT_PARTICIPANT_DLG_TAG);
	}

	/**
	 * @param position
	 * @param name
	 */
	@Override
	public void onNameEditorOK(final int position, String name) {
		if (position >= participantsListAdapter.getNumberOfParticipants()) {
			participantsListAdapter.createParticipantWithName(name);
		} else {
			participantsListAdapter.setNameOfParticipantAtPosition(position,
					name);
		}
	}

	/**
	 * @param position
	 */
	@Override
	public void onNameEditorCancelled(final int position) {
	}

	/**
 * 
 */
	private void setBalanceText() {
		if (sumValueText == null) {
			return;
		}
		BigDecimal value = participantsListAdapter.getParticipantsValuesSum();
		String text = myApp.formatCurrencyValueWithSymbol(value,
				participantsListAdapter.getEvent().getDefaultCurrency());
		sumValueText.setText(text + " ");
		if (value.signum() > 0) {
			sumValueText.setTextColor(android.graphics.Color.GREEN);
		} else if (value.signum() < 0) {
			sumValueText.setTextColor(android.graphics.Color.RED);
		} else {
			sumValueText.setTextColor(getResources().getColor(
					android.R.color.primary_text_dark));
		}
	}

	/**
 * 
 */
	private void setAllBalanceTexts() {
		if (sumList == null) {
			return;
		}
		SortedMap<String, BigDecimal> sums = participantsListAdapter
				.getAllParticipantsValuesSums();
		int index = 0;
		for (Map.Entry<String, BigDecimal> entry : sums.entrySet()) {
			TextView curText = null;
			if (index < sumList.getChildCount()) {
				curText = (TextView) sumList.getChildAt(index);
			} else {
				curText = new TextView(this);
				curText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				curText.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				params.gravity = Gravity.RIGHT;
				sumList.addView(curText, params);
			}
			String text = myApp.formatCurrencyValueWithSymbol(entry.getValue(),
					Currency.getInstance(entry.getKey()));
			curText.setText(text + " ");
			if (entry.getValue().signum() > 0) {
				curText.setTextColor(android.graphics.Color.GREEN);
			} else if (entry.getValue().signum() < 0) {
				curText.setTextColor(android.graphics.Color.RED);
			} else {
				curText.setTextColor(getResources().getColor(
						android.R.color.primary_text_dark));
			}
			++index;
		}
		if (index + 1 < sumList.getChildCount()) {
			sumList.removeViews(index + 1, sumList.getChildCount() - index - 1);

		}
	}

	/**
 * 
 */
	public void refreshData() {
		participantsListAdapter.readParticipantsFromDB();
		if (myApp.getConvertToEventCurrency()) {
			setBalanceText();
		} else {
			setAllBalanceTexts();
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
		inflater.inflate(R.menu.participants_list_menu, menu);
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
			case R.id.menu_add_participant : {
				onPersonClicked(participantsListAdapter.getCount(), -1);
				return true;
			}
			default :
				return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.participant_context_menu, menu);
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
			case R.id.menu_participant_delete : {
				participantsListAdapter
						.removeParticipantAtPosition(info.position);
			}
				return true;
			default :
				return super.onContextItemSelected(item);
		}
	}

	/**
	 * @param v
	 */
	public void onAddNewParticipantClicked(View v) {
		onPersonClicked(participantsListAdapter.getCount(), -1);
	}

}
