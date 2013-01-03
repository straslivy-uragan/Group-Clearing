package cz.su.GroupClearing;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/** Class describing dialog for modifying name of participant. It is
 * usually invoked from <code>ParticipantsListActivity</code> this
 * editor contains an edit box for inserting and changing the
 * participants name. The arguments (original <code>name</code> and
 * <code>position</code>) are passed to this object via
 * <code>DialogFragment.setArguments(Bundle)</code> function. If
 * calling <code>Activity</code> wants to receive respond when user
 * clicks either on OK or on Cancel button, then it has to implement
 * <code>EditParticipantDialog.EditParticipantListener</code>
 * interface.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class EditParticipantDialog extends DialogFragment {
    /** Current name of the participant. */
	String name = null;
    /** <code>TextView</code> containing the <code>name</code>. */
	TextView nameTextView = null;
    /** Position of the participant within the list. */
	int position = 0;
    /** Listener object (activity) for this editor. */
	EditParticipantListener myListener = null;

    /** Tag for storing the name of the participant within the
     * <code>Bundle</code> map. This tag is used in a Bundle used for
     * passing the arguments via
     * <code>setArguments</code>/<code>getArguments</code> pair of
     * functions.
     */
	public static final String NAME_TAG = "name";
	/** Tag for storing the position of the participant within the
     * <code>Bundle</code> map. This tag is used in a Bundle used for
     * passing the arguments vie
     * <code>setArguments</code>/<code>getArguments</code> pair of
     * functions.
     */
	public static final String POSITION_TAG = "position";

	/** Interface describing the object which wants to receive
     * respond from this dialog. The listener is attached to this
     * dialog in <code>onAttach(Activity)</code> method.
     * @author Strašlivý Uragán
     * @version 1.0
     * @since 1.0
	 */
	public interface EditParticipantListener {
		/** Function called, when OK button was clicked.
         *
		 * @param position Position parameter associated with the dialog.
		 * @param name Current name of participant (content of <code>nameTextView</code>).
		 */
		public void onNameEditorOK(int position, String name);
		/** Function called, when Cancel button was clicked or the
         * dialog was otherwise cancelled.
		 * @param position Position parameter associated with the dialog.
		 */
		public void onNameEditorCancelled(int position);
	}

	/** Constructor creating an empty dialog.
     */
	public EditParticipantDialog() {
		name = null;
		position = -1;
		myListener = null;
		nameTextView = null;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(getString(R.string.app_name));
		View v = inflater.inflate(R.layout.participant_edit, container, false);
        Bundle arguments = getArguments();
        position = arguments.getInt(POSITION_TAG, -1);
        name = arguments.getString(NAME_TAG);
        if (name == null)
        {
        	name = "";
        }
		nameTextView = (TextView) v.findViewById(R.id.participant_name);
        nameTextView.setText(name);
		Button okButton = (Button) v.findViewById(R.id.participant_edit_ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOkButtonClicked(v);
			}
		});
		Button cancelButton = (Button) v
				.findViewById(R.id.participant_edit_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return v;
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			myListener = (EditParticipantListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement EditParticipantListener");
		}
	}
    /** Handler of OK button click event. If <code>myListener</code>
     * was set in <code>onAttach(Activity)</code> function, then
     * <code>myListener.onNameEditorOk(position,name)</code> is
     * called. In any case <code>dismiss()</code> is called to dismiss
     * the dialog. 
     * @param v <code>View</code> of button which was
     * clicked.
	 */
	public void onOkButtonClicked(View v) {
		name = nameTextView.getText().toString();
		dismiss();
        if (myListener != null)
        {
            myListener.onNameEditorOK(position, name);
        }
	}

	/** Handler of cancel event. If <code>myListener</code>
     * was set in <code>onAttach(Activity)</code> function, then
     * <code>myListener.onNameEditorCancel(position,name)</code> is
     * called. In any case <code>dismiss()</code> is called to dismiss
     * the dialog. 
	 * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
        if (myListener != null)
        {
            myListener.onNameEditorCancelled(position);
        }
	}
}
