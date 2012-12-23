package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** Class describing dialog for editing value of a participant within
 * a transaction. Used in <code>TransactionEditActivity</code>. This
 * editor dialog contains a label for showing name of participant and
 * a text box for input the value of the participant within the
 * transaction. The arguments (<code>name</code> and <code>id</code>
 * of the participant, <code>position</code> within list of
 * participants, the original <code>value</code>, the
 * <code>precomputed</code>
 * value suggestion, and the transaction currency - <code>transactionCurrency</code>)
 * are passed to this
 * object via <code>DialogFragment.setArguments(Bundle)</code>
 * function. If calling <code>Activity</code> wants to receive respond
 * when user clicks either on OK or on Cancel button, then it has to
 * implement
 * <code>EditParticipantValueDialog.EditParticipantValueListener</code>
 * interface. The value of the participant is passed to this dialog as
 * <code>String</code>.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class EditParticipantValueDialog extends DialogFragment {
     /** Name of the participant.
     */
    String name = "";
    /** Value of the participant. Stored as String.
     */
    BigDecimal value = BigDecimal.ZERO;
    /** Precomputed suggested value of the participant.
     */
    BigDecimal precomputed = BigDecimal.ZERO;
    /** <code>TextView</code> for showing the name of the participant.
     */
    TextView nameTextView = null;
    /** <code>EditText</code> for input of the value of the
     * participant.
     */
    EditText valueEdit = null;
    /** Id of the participant.
     */
    long participantId = -1;
    /** Position of the participant within the list of the
     * participants.
     */
    int position = -1;
    /** Listener object (activity) for this editor. */
    EditParticipantValueListener myListener = null;

    /** Application object for accessing global data and preferences.
     */
    GroupClearingApplication myApp = GroupClearingApplication.getInstance();

    /** Transaction currency.
     */
    Currency transactionCurrency = Currency.getInstance(Locale.getDefault());

    /** Tag for storing the name of the participant within the
     * <code>Bundle</code> map. This tag is used in a Bundle used for
     * passing the arguments via
     * <code>setArguments</code>/<code>getArguments</code> pair of
     * functions.
     */
    public static final String NAME_TAG = "name";
    /** Tag for storing the id of the participant within the
     * <code>Bundle</code> map. This tag is used in a Bundle used for
     * passing the arguments via
     * <code>setArguments</code>/<code>getArguments</code> pair of
     * functions.
     */
    public static final String ID_TAG = "id";
    /** Tag for storing the position of the participant within the
     * <code>Bundle</code> map. This tag is used in a Bundle used for
     * passing the arguments via
     * <code>setArguments</code>/<code>getArguments</code> pair of
     * functions.
     */
    public static final String POSITION_TAG = "position";
    /** Tag for storing the value of the participant within the
     * <code>Bundle</code> map. This tag is used in a Bundle used for
     * passing the arguments via
     * <code>setArguments</code>/<code>getArguments</code> pair of
     * functions.
     */
    public static final String VALUE_TAG = "value";
    /** Tag for storing the precomputed suggested value of
     * participant. This tag is used in a <code>Bundle</code> used for
     * passing the arguments via
     * <code>setArguments</code>/<code>getArguments</code> pair of
     * functions.
     */
    public static final String PRECOMPUTED_TAG  = "precomputed";
    /** Tag for storing the currency of the transaction. This tag is
     * used in a <code>Bundle</code> used for passing the arguments
     * via <code>setArguments</code>/<code>getArguments</code> pair of
     * functions. The currency is passed as String code.
     */
    public static final String CURRENCY_TAG = "currency";


    /** Interface describing the object which wants to receive respond
     * from this dialog. The listener is attached to this dialog in
     * <code>onAttach(Activity</code> method.
     * @author Strašlivý Uragán
     * @version 1.0
     * @since 1.0
     */
    public interface EditParticipantValueListener {
        /** Function called, when OK button was clicked.
         *
         * @param position Position parameter associated with the
         * dialog.
         * @param participantId Id of the participant.
         * @param value Current value of the participant.
         */
        public void onValueEditorOK(int position, long participantId, BigDecimal value);
        /** Function called when Cancel button was clicked or the
         * dialog was otherwise cancelled.
         * @param position Position parameter associated with the
         * dialog.
         * @param participantId Id of the participant.
         */
        public void onValueEditorCancelled(int position, long participantId);
    }

    /** Construction of an empty dialog.
    */
    public EditParticipantValueDialog() {
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
            View v = inflater.inflate(R.layout.participant_value_edit,
                    container, false);
            nameTextView = (TextView) v.findViewById(R.id.pev_name);
            Bundle arguments = getArguments();
            position = arguments.getInt(POSITION_TAG, -1);
            name = arguments.getString(NAME_TAG);
            if (name == null) {
                name = "";
            }
            participantId = arguments.getLong(ID_TAG, -1);
            String valueString = arguments.getString(VALUE_TAG);
            if (valueString != null) {
                value = new BigDecimal(valueString);
            }
            value = new BigDecimal(valueString);
            String precomputedString = arguments.getString(PRECOMPUTED_TAG);
            if (precomputedString != null) {
                precomputed = new BigDecimal(precomputedString);
            }
            String currencyString = arguments.getString(CURRENCY_TAG);
            if (currencyString != null)
            {
                transactionCurrency = Currency.getInstance(currencyString);
            }
            nameTextView.setText(name);
            valueEdit = (EditText) v.findViewById(R.id.pev_value);
            valueEdit.setText(myApp.formatCurrencyValue(value,
                        transactionCurrency));
            Button okButton = (Button) v.findViewById(R.id.pev_ok);
            okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    onOkButtonClicked(v);
                    }
                    });
            Button cancelButton = (Button) v.findViewById(R.id.pev_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    onCancelButtonClicked(v);
                    }
                    });
            Button computeButton = (Button) v.findViewById(R.id.pev_compute);
            computeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    onComputeButtonClicked(v);
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
			myListener = (EditParticipantValueListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement EditParticipantListener");
		}
	}
   
    /** Reacts on the click of OK button.
     * @param v The View of the clicked button.
     */
    public void onOkButtonClicked(View v) {
        dismiss();
        try {
            value = myApp.parseCurrencyValue(
                    valueEdit.getText().toString(),
                    transactionCurrency);
            myListener.onValueEditorOK(position, participantId, value);
        } catch (NumberFormatException e) {
            myListener.onValueEditorCancelled(position, participantId);
        }
    }

    /** Reacts on the click of Cancel button.
     * @param v The view of the clicked button.
     */
    public void onCancelButtonClicked(View v) {
        dismiss();
        myListener.onValueEditorCancelled(position, participantId);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
     */
    @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            myListener.onValueEditorCancelled(position, participantId);
        }

    /** Reacts on the click of Compute button. Sets the string in 
     * <code>valueEdit</code> to the precomputed
     * suggestion stored in <code>precomputed</code> variable.
     * @param v The view of the clicked button.
     */
    public void onComputeButtonClicked(View v) {
        valueEdit.setText(myApp.formatCurrencyValue(
                    precomputed, transactionCurrency));
    }
}
