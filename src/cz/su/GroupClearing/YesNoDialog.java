package cz.su.GroupClearing;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class YesNoDialog extends DialogFragment {
    String message = null;
    boolean okClicked = false;
    OnClickListener listener;

    Object myTag = null;

    public interface OnClickListener {
        public void onOkClicked(YesNoDialog dlg);
        public void onCancelled(YesNoDialog dlg);
    }

    public YesNoDialog(String aMessage) {
        message = aMessage;
    }

    @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            getDialog().setTitle(getString(R.string.gc_yesno_title));
            View v = inflater.inflate(R.layout.yes_no_dialog,
                    container, false);
            TextView msgText = (TextView)v.findViewById(R.id.yesno_text);
            if (message != null) {
                msgText.setText(message);
            }
            Button okButton =
                (Button)v.findViewById(R.id.yesno_ok);
            okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    onOkButtonClicked(v);
                    }
                    });
            Button cancelButton =
                (Button)v.findViewById(R.id.yesno_cancel);
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
            listener.onOkClicked(this);
        }
    }

    public void onCancelButtonClicked(View v) {
        dismiss();
        if (listener != null) {
            listener.onCancelled(this);
        }
    }

    @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (listener != null) {
                listener.onCancelled(this);
            }
        }
   
    public void setOnClickListener(YesNoDialog.OnClickListener listener) {
        this.listener = listener;
    }

    public void setDialogTag(Object aTag) {
        myTag = aTag;
    }

    public Object getDialogTag() {
        return myTag;
    }
}
