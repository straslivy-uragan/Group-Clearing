package cz.su.GroupClearing;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

/** Activity which shows and allows to modify global application
 * preferences. The following settings can be modified here:
 * <code>noSplitChangeWarning</code>,
 * <code>supportMultipleCurrencies</code>, and
 * <code>convertToEventCurrency</code>. For the descriptions of these
 * preferences see the description of
 * <code>GroupClearingApplication</code>.
 * @see cz.su.GroupClearing.GroupClearingApplication
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class SettingsActivity extends Activity {
    /** Checkbox for setting <code>supportMultipleCurrencies</code>
     * preference.
     */
    CheckBox supportMultipleCurrencies = null;
    /** Checkbox for setting <code>convertToEventCurrency</code>
     * preference.
     */
    CheckBox convertToEventCurrency = null;
    /** Checkbox for setting <code>noSplitChangeWarning</code>
     * preference.
     */
    CheckBox splitWarningCheck = null;
    /** The global application object used for accessing and modifying
     * the preferences.
     */
    GroupClearingApplication myApp = GroupClearingApplication.getInstance();

    /* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
        supportMultipleCurrencies =
            (CheckBox)findViewById(R.id.mult_currencies_check);
        convertToEventCurrency =
            (CheckBox)findViewById(R.id.convert_check);
        splitWarningCheck = 
            (CheckBox)findViewById(R.id.split_warning_check);
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        supportMultipleCurrencies.setChecked(
                myApp.getSupportMultipleCurrencies());
        convertToEventCurrency.setVisibility(
                myApp.getSupportMultipleCurrencies()
                ? View.VISIBLE : View.GONE);
        convertToEventCurrency.setChecked(
                myApp.getConvertToEventCurrency());
        splitWarningCheck.setChecked(
                ! myApp.getNoSplitChangeWarning());
    }

    /** Called when the status of <code>CheckBox supportMultipleCurrencies</code>
     * changed.
     * @param v View of <code>CheckBox supportMultipleCurrencies</code>
     */
    public void onSupportMultipleCurrenciesChanged(View v) {
        myApp.setSupportMultipleCurrencies(
                supportMultipleCurrencies.isChecked());
        convertToEventCurrency.setVisibility(
                myApp.getSupportMultipleCurrencies()
                ? View.VISIBLE : View.GONE);
    }

    /** Called when the status of <code>CheckBox
     * convertToEventCurrency</code> changed.
     * @param v View of <code>CheckBox convertToEventCurrency</code>.
     */
    public void onConvertToEventCurrencyChanged(View v) {
        myApp.setConvertToEventCurrency(
                convertToEventCurrency.isChecked());
    }

    /** Called when the status of <code>CheckBox
     * splitWarningCheck</code> changed.
     * @param v View of <code>CheckBox splitWarningCheck</code>.
     */
    public void onSplitWarningChanged(View v) {
        myApp.setNoSplitChangeWarning(
                ! splitWarningCheck.isChecked());
    }
}
