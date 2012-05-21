package cz.su.GroupClearing;

import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import cz.su.GroupClearing.ParticipantsListAdapter;

public class ParticipantsListActivity extends FragmentActivity {

   private long myEventId = -1;
   private ParticipantsListAdapter participantsListAdapter = null;
   private EditParticipantDialog editParticipantDialog = null;

   public class EditParticipantDialog extends DialogFragment {
      String name = null;
      TextView nameTextView = null;
      int position = 0;

      @Override
         public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
         }

      @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
               Bundle savedInstanceState) {
            getDialog().setTitle(getString(R.string.app_name));
            View v = inflater.inflate(R.layout.participant_edit, container,
                  false);
            nameTextView = (TextView) v.findViewById(R.id.participant_name);
            if (name != null) {
               nameTextView.setText(name);
            }
            Button okButton = (Button) v.findViewById(R.id.participant_edit_ok);
            okButton.setOnClickListener(new View.OnClickListener() {
                  public void onClick(View v) {
                  okButtonClicked(v);
                  }
                  });
            return v;
         }

      public void okButtonClicked(View v) {
         name = nameTextView.getText().toString();
         dismiss();
         onNameEditorOK(position);
      }

      public void onCancel(DialogInterface dialog) {
         super.onCancel(dialog);
         nameTextView.setText(name);
         onNameEditorCancelled(position);
      }

      public String getName() {
         return name;
      }

      public void setName(String nameString) {
         name = nameString;
         if (nameTextView != null) {
            nameTextView.setText(name);
         }
      }

      public void setPosition(int newPosition) {
         position = newPosition;
      }
   }

   @Override
      public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.participants_list);
         ListView lv = (ListView) findViewById(R.id.participants_list_view);
         lv.setOnItemClickListener(new OnItemClickListener() {
               public void onItemClick(AdapterView<?> parent, View view,
                  int position, long id) {
               onPersonClicked(position, id);
               }
               });
         myEventId = getIntent().getIntExtra("cz.su.GroupClearing.EventId", -1);
         participantsListAdapter = new ParticipantsListAdapter(this, myEventId);
         lv.setAdapter(participantsListAdapter);
         registerForContextMenu(lv);
      }

   @Override
      protected void onResume() {
         super.onResume();
         refreshData();
      }

   @Override
   protected void onDestroy()
   {
      super.onDestroy();
      if (participantsListAdapter != null)
      {
         participantsListAdapter.closeDB();
      }
   }

   public void onPersonClicked(final int position, long id) {
      FragmentManager fm = getSupportFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
      Fragment prev = getSupportFragmentManager().findFragmentByTag(
            "edit_participant_dialog");
      if (prev != null) {
         ft.remove(prev);
      }
      ft.addToBackStack(null);
      if (editParticipantDialog == null) {
         editParticipantDialog = new EditParticipantDialog();
      }

      editParticipantDialog.show(ft, "edit_participant_dialog");
      editParticipantDialog.setPosition(position);
      if (position >= participantsListAdapter.getCount()) {
         editParticipantDialog.setName(getString(R.string.participant_name));
      } else {
         editParticipantDialog.setName(
               ((ClearingPerson)participantsListAdapter.getItem(position)).getName());
      }
   }

   public void onNameEditorOK(final int position) {
      if (position >= participantsListAdapter.getCount()) {
         participantsListAdapter.createParticipantWithName(
               editParticipantDialog.getName());
      } else {
         participantsListAdapter.setNameOfParticipantAtPosition(
               position, editParticipantDialog.getName());
      }
   }

   public void onNameEditorCancelled(final int position) {
   }

   public void refreshData() {
      participantsListAdapter.readParticipantsFromDB();
   }

   @Override
      public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.participants_list_menu, menu);
         return true;
      }

   @Override
      public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
            case R.id.menu_add_participant:
               {
                  onPersonClicked(participantsListAdapter.getCount(), 0);
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
         inflater.inflate(R.menu.participant_context_menu, menu);
      }

   @Override
      public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
            case R.id.menu_participant_delete:
               {
                  participantsListAdapter.removeParticipantAtPosition(info.position);
               }
               return true;
            default:
               return super.onContextItemSelected(item);
         }
      }

}
