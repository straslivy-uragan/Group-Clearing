package cz.su.GroupClearing;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class WarningDialogWithCheck extends DialogFragment {
    String message = null;
    CheckBox check = null;
    boolean okClicked = false;
    WarningDialogClickListener listener;

    public interface WarningDialogClickListener {
        public void onWarningConfirmed(boolean checked);
        public void onWarningCancelled(boolean checked);
    }

    public WarningDialogWithCheck(String aMessage) {
        message = aMessage;
    }

    @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            getDialog().setTitle(getString(R.string.gc_warning_title));
            View v = inflater.inflate(R.layout.warning_with_check,
                    container, false);
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

    public void onOkButtonClicked(View v) {
        dismiss();
        if (listener != null) {
            listener.onWarningConfirmed(check.isChecked());
        }
    }

    public void onCancelButtonClicked(View v) {
        dismiss();
        if (listener != null) {
            listener.onWarningCancelled(check.isChecked());
        }
    }

    @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (listener != null) {
                listener.onWarningCancelled(check.isChecked());
            }
        }
   
    public void setOnWarningListener(WarningDialogClickListener listener) {
        this.listener = listener;
    }
}
