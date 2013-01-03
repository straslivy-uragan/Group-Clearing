package cz.su.GroupClearing;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/** Dialog showing a warning message. Dialog layout is described in
 * <code>warning_with_check.xml</code>. The layout consists of a
 * message <code>TextView</code>, a <code>CheckBox</code> allowing
 * user to determine if the warning should be shown next time in the
 * same situation, and an <code>OK</code> <code>Button</code>.
 * Activity using this dialog should implement
 * WarningDialogClickListener in order to listen to whether dialog was
 * confirmed by clicking the OK button, or cancelled using system back
 * button. There is one argument which can be passed to this dialog
 * and that is a message string to be shown. It should be passed using
 * <code>setArguments</code> method within a <code>Bundle</code>
 * object tagged using <code>MESSAGE_TAG</code> constant.
 *
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class WarningDialogWithCheck extends DialogFragment {
    /** CheckBox allowing user to specify whether the warning should
     * be shown next time in the same situation.
     */
    CheckBox check = null;
    /** Listener to the events of this dialog.
     */
    WarningDialogClickListener myListener;
    /** Tag for putting a message parameter into a <code>Bundle</code>
     * containing the arguments to this dialog.
     */
    public static final String MESSAGE_TAG = "message";

    /** Interface describing a listener to the events of this dialog.
     * @author Strašlivý Uragán
     * @version 1.0
     * @since 1.0
     */
    public interface WarningDialogClickListener {
        /** Called when user clicked the OK button.
         * @param checked Status of the checkbox.
         */
        public void onWarningConfirmed(boolean checked);
        /** Called when the dialog was cancelled.
         * @param checked Status of the checkbox.
         */
        public void onWarningCancelled(boolean checked);
    }

    /** Constructs an empty dialog.
     */
    public WarningDialogWithCheck() {
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
     */
    @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            getDialog().setTitle(getString(R.string.gc_warning_title));
            View v = inflater.inflate(R.layout.warning_with_check,
                    container, false);
            String message = getArguments().getString(MESSAGE_TAG);
            TextView msgText = (TextView)v.findViewById(R.id.warning_text);
            if (message != null) {
                msgText.setText(message);
            }
            check = (CheckBox)v.findViewById(R.id.do_not_show_check);
            Button okButton =
                (Button)v.findViewById(R.id.warning_with_check_ok);
            okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    onOkButtonClicked(v);
                    }
                    });
            Button cancelButton =
                (Button)v.findViewById(R.id.warning_with_check_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    onCancelButtonClicked(v);
                    }
                    });
            return v;
        }

    @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try {
                myListener = (WarningDialogClickListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement WarningDialogClickListener");
            }
        }
    /** Called when OK button was clicked. Calls appropriate function
     * of the listener.
     * @param v View of the button.
     */
    public void onOkButtonClicked(View v) {
        dismiss();
        if (myListener != null) {
            myListener.onWarningConfirmed(check.isChecked());
        }
    }

    /** Called when Cancel button was clicked. Calls appropriate function
     * of the listener.
     * @param v View of the button.
     */
    public void onCancelButtonClicked(View v) {
        dismiss();
        if (myListener != null) {
            myListener.onWarningCancelled(check.isChecked());
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
     */
    @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (myListener != null) {
                myListener.onWarningCancelled(check.isChecked());
            }
        }
  }
