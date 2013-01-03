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

/**
 * A simple dialog presenting the user with a message and two buttons yes and no
 * (or OK and Cancel). The layout of this dialog is described in
 * <code>yes_no_dialog.xml</code>. Activity using this dialog should implement
 * YesNoListener in order to listen to the response of the user. There is one
 * argument which can be passed to this dialog and that is a message string to
 * be shown. It should be passed using <code>setArguments</code> method within a
 * <code>Bundle</code> object tagged using <code>MESSAGE_TAG</code> constant. It
 * is also possible to associate a any object as a tag with a dialog object.
 * 
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class YesNoDialog extends DialogFragment {
	/**
	 * Tag for putting message string into a <code>Bundle</code> with arguments.
	 */
	public static final String MESSAGE_TAG = "message";
	/**
	 * Listener to the events of this dialog.
	 */
	YesNoListener myListener;
	/**
	 * Tag associated with this dialog object.
	 */
	Object myTag = null;

	/**
	 * Interface describing a listener to the events of this dialog.
	 * 
	 * @author Strašlivý Uragán
	 * @version 1.0
	 * @since 1.0
	 */
	public interface YesNoListener {
		/**
		 * Called when user clicked the OK/Yes button.
		 * 
		 * @param dlg
		 *            The calling dialog.
		 */
		public void onYesNoOkClicked(YesNoDialog dlg);
		/**
		 * Called when user clicked the Cancel/No button.
		 * 
		 * @param dlg
		 *            The calling dialog.
		 */
		public void onYesNoCancelClicked(YesNoDialog dlg);
		/**
		 * Called when the dialog was cancelled.
		 * 
		 * @param dlg
		 *            The calling dialog.
		 */
		public void onYesNoCancelled(YesNoDialog dlg);
	}

	/**
	 * Constructs an empty dialog.
	 */
	public YesNoDialog() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle
	 * )
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(getString(R.string.gc_yesno_title));
		View v = inflater.inflate(R.layout.yes_no_dialog, container, false);
		TextView msgText = (TextView) v.findViewById(R.id.yesno_text);
		String message = getArguments().getString(MESSAGE_TAG);
		if (message != null) {
			msgText.setText(message);
		}
		Button okButton = (Button) v.findViewById(R.id.yesno_ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOkButtonClicked(v);
			}
		});
		Button cancelButton = (Button) v.findViewById(R.id.yesno_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCancelButtonClicked(v);
			}
		});
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			myListener = (YesNoListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement YesNoListener");
		}
	}

	/**
	 * Called when OK button was clicked. Calls appropriate function of the
	 * listener.
	 * 
	 * @param v
	 *            View of the button.
	 */
	public void onOkButtonClicked(View v) {
		dismiss();
		if (myListener != null) {
			myListener.onYesNoOkClicked(this);
		}
	}

	/**
	 * Called when Cancel button was clicked. Calls appropriate function of the
	 * listener.
	 * 
	 * @param v
	 *            View of the button.
	 */
	public void onCancelButtonClicked(View v) {
		dismiss();
		if (myListener != null) {
			myListener.onYesNoCancelClicked(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.DialogFragment#onCancel(android.content.
	 * DialogInterface)
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (myListener != null) {
			myListener.onYesNoCancelled(this);
		}
	}

	/**
	 * Sets the tag associated with this dialog object.
	 * 
	 * @param aTag
	 *            New tag associated with this dialog object.
	 */
	public void setDialogTag(Object aTag) {
		myTag = aTag;
	}

	/**
	 * Returns the tag associated with this dialog object.
	 * 
	 * @return The tag associated with this dialog object.
	 */
	public Object getDialogTag() {
		return myTag;
	}
}
