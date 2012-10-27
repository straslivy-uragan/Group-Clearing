package cz.su.GroupClearing;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {
    CheckBox supportMultipleCurrencies = null;
    CheckBox convertToEventCurrency = null;
    CheckBox splitWarningCheck = null;
    GroupClearingApplication myApp = GroupClearingApplication.getInstance();

/** Called when the activity is first created. */
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

    public void onSupportMultipleCurrenciesChanged(View v) {
        myApp.setSupportMultipleCurrencies(
                supportMultipleCurrencies.isChecked());
        convertToEventCurrency.setVisibility(
                myApp.getSupportMultipleCurrencies()
                ? View.VISIBLE : View.GONE);
    }

    public void onConvertToEventCurrencyChanged(View v) {
        myApp.setConvertToEventCurrency(
                convertToEventCurrency.isChecked());
    }

    public void onSplitWarningChanged(View v) {
        myApp.setNoSplitChangeWarning(
                ! splitWarningCheck.isChecked());
    }
}
