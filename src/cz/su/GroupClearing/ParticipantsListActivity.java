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

/** Activity with the list of participants of an event. The activity
 * maintains <code>ListView</code> populated by a
 * <code>ParticipantsListAdapter</code>. An item of this list contains
 * name of participant and its balance (either a single balance or
 * balances in different currencies depending on the state of
 * <code>convertToEventCurrency</code> preference, see
 * <code>GroupClearingApplication</code>). Above the list a button for
 * adding a new participant and a total balance (per currency balances
 * respectively) are placed. When
 * a participant item or button for creating new participant are
 * clicked, <code>EditParticipantDialog</code> is shown for input of
 * the name of the participant. When a participant is long-clicked,
 * context menu with possibility of deleting the participant appears.
 * Any creation/modification/deletion of participant is directly
 * consulted with the associated <code>GCDatabase</code> object.
 * 
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class ParticipantsListActivity extends FragmentActivity
		implements
			EditParticipantDialog.EditParticipantListener {

	/** Id of the event associated to this participants list.
     */
	private long myEventId = -1;
	/** Adapter populating the <code>ListView</code> with
     * participants.
     */
	private ParticipantsListAdapter participantsListAdapter = null;
	/** <code>TextView</code> for showing total balance (sum of
     * balances) over all participants. This <code>TextView</code> is
     * used only if <code>convertToEventCurrency</code> flag is true.
     * Otherwise <code>sumList</code> <code>LinearLayout</code> is
     * used for showing different balancies in different currencies.
     */
	private TextView sumValueText = null;
	/** <code>LinearLayout</code> which in the end contains list of
     * <code>TextView</code> objects showing the balances per
     * different currencies. This <code>LinearLayout</code> is used
     * only if <code>convertToEventCurrency</code> flag is false.
     * Otherwise <code>sumValueText</code> <code>TextView</code> is
     * used for showing one single balance.
     */
	private LinearLayout sumList = null;
	/** The application object for accessing global preferences and
     * other shared data.
     */
	private final GroupClearingApplication myApp = GroupClearingApplication
			.getInstance();

	/** Tag used in fragment manager associated with the dialog for
     * editing the name of participant.
     */
	public final static String EDIT_PARTICIPANT_DLG_TAG = "edit_participant_dialog";

	/** Tag determining the event id parameter of the activity.
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

    /** Called when an item of <code>ListView</code> of participants
     * is clicked on. Uses <code>FragmentManager</code> to invoke
     * modal dialog <code>EditParticipantDialog</code> for editing the
     * name of the corresponding participant. The participant object
     * is retrieved from <code>participantsListAdapter</code>. If a
     * special item for creating new participant was selected, then
     * this fact can be at any time recognized by comparing
     * <code>position</code> parameter to the number of participants
     * stored in <code>participantsListAdapter</code>.
     *
	 * @param position Position of item within <code>ListView</code>
     * which was clicked.
	 * @param id Id of item which has been clicked.
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

    /** Called from <code>EditParticipantDialog</code> when user
     * clicks on OK button. In this method either new participant with
     * given <code>name</code> is created if <code>position</code> is
     * at least the number of participants, or the participants name
     * is updated. Both actions are done through
     * <code>participantsListAdapter</code> methods.
	 * @param position Position of participant within the list.
	 * @param name New name of the participant.
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

	/** Called from <code>EditParticipantDialog</code> when user
     * cancels it. This method does nothing.
	 * @param position Position of participant within the list.
	 */
	@Override
	public void onNameEditorCancelled(final int position) {
	}

    /** Updates the <code>sumValueText</code> with the proper value
     * and color. The sum is retrieved from
     * <code>participantsListAdapter</code> using
     * <code>ParticipantsListAdapter.getParticipantsValuesSum()</code>
     * method. Color of the text is set to
     * <code>android.graphics.Color.GREEN</code> in case of positive
     * balance, to <code>android.graphics.Color.RED</code> in case of
     * negative balance and to
     * <code>android.R.color.primary_text_dark</code> in case of zero
     * balance. If <code>sumValueText</code> is <code>null</code>,
     * this function does nothing.
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

	/** Updates the balances in <code>sumList</code> to proper values
     * and colors. The sums per currencies are retrieved from
     * <code>participantsListAdapter</code> using
     * <code>ParticipantsListAdapter.getAllParticipantsValuesSums()</code> method.
     * If <code>sumList</code> is <code>null</code>, this function
     * does nothing.
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

	/** Refreshes the list of participants and the sums. Consists
     * mainly of calling
     * <code>ParticipantsListAdapter.readParticipantsFromDB</code>.
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

	/** Called when button for creating new participant was clicked.
     * It just calls <code>onPersonClicked</code> with position equal
     * to the number of participants.
	 * @param v View of <code>Button</code> which was clicked.
	 */
	public void onAddNewParticipantClicked(View v) {
		onPersonClicked(participantsListAdapter.getCount(), -1);
	}

}
