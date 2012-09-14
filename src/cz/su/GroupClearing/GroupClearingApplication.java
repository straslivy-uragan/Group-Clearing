/**
 * 
 */
package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/*
 * TODO: Do kolonek pro hodnoty transakce a platby účastníka transakce
 * umožnit zadat vzoreček (násobení konstantou, sčítání, možná závorky).
 * Mělo by stačit upravit GroupClearingApplication.parseCurrencyValue(String, Currency)
 * TODO: Umožnit zvolit měny jednotlivých transakcí s podporou
 * přepočtu, a to jednak s ručním nastavením kurzů, případně s jejich
 * stažením, ale pak by asi chtělo pamatovat si u každé transakce
 * kurz aktuální v době zadání, stačí k měně události.  
 */

/**
 * @author su
 * 
 */
public class GroupClearingApplication extends Application {
	private static GroupClearingApplication instance;
	private NumberFormat currencyFormatterWithSymbol = null;
	private NumberFormat currencyFormatter = null;

	private boolean noSplitChangeWarning = false;
	private boolean supportMultipleCurrencies = false;
	private boolean convertToEventCurrency = true;

	private SharedPreferences preferences = null;
	private SharedPreferences.Editor preferencesEditor = null;

	public static final String SPLIT_CHANGE_WARNING_PREF = "split_change_warning";
	public static final String SUPPORT_MULTIPLE_CURRENCIES = "support_multiple_currencies";
	public static final String CONVERT_TO_EVENT_CURRENCY = "convert_to_event_currency";

	public static GroupClearingApplication getInstance() {
		return instance;
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		instance = this;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferencesEditor = preferences.edit();
		noSplitChangeWarning = preferences.getBoolean(
				SPLIT_CHANGE_WARNING_PREF, false);
		supportMultipleCurrencies = preferences.getBoolean(
				SUPPORT_MULTIPLE_CURRENCIES, false);
		convertToEventCurrency = preferences.getBoolean(
				CONVERT_TO_EVENT_CURRENCY, true);
	}

	public String formatCurrencyValue(BigDecimal amount, Currency currency) {
		if (currencyFormatter == null) {
			currencyFormatter = NumberFormat.getInstance();
		}
		currencyFormatter.setGroupingUsed(false);
		currencyFormatter.setMaximumFractionDigits(currency
				.getDefaultFractionDigits());
		return currencyFormatter
				.format(amount.setScale(currency.getDefaultFractionDigits(),
						BigDecimal.ROUND_HALF_EVEN));
	}

	public String formatCurrencyValueWithSymbol(BigDecimal amount,
			Currency currency) {
		if (currencyFormatterWithSymbol == null) {
			currencyFormatterWithSymbol = NumberFormat.getCurrencyInstance();
		}
		currencyFormatterWithSymbol.setCurrency(currency);
		return currencyFormatterWithSymbol
				.format(amount.setScale(currency.getDefaultFractionDigits(),
						BigDecimal.ROUND_HALF_EVEN));
	}

	public BigDecimal parseCurrencyValue(String valueString, Currency currency)
			throws NumberFormatException {
		valueString = valueString.replace(',', '.');
		BigDecimal value = new BigDecimal(valueString);
		// value = value.movePointRight(currency.getDefaultFractionDigits());
		return value.setScale(currency.getDefaultFractionDigits(),
				BigDecimal.ROUND_HALF_EVEN);
	}

	public boolean getNoSplitChangeWarning() {
		return noSplitChangeWarning;
	}

	public void setNoSplitChangeWarning(boolean checked) {
		noSplitChangeWarning = checked;
		preferencesEditor.putBoolean(SPLIT_CHANGE_WARNING_PREF, checked);
		preferencesEditor.apply();
	}

	public boolean getSupportMultipleCurrencies() {
		return supportMultipleCurrencies;
	}

	public void setSupportMultipleCurrencies(boolean checked) {
		supportMultipleCurrencies = checked;
		preferencesEditor.putBoolean(SUPPORT_MULTIPLE_CURRENCIES, checked);
		preferencesEditor.apply();
	}

	public boolean getConvertToEventCurrency() {
		return convertToEventCurrency;
	}

	public void setConvertToEventCurrency(boolean checked) {
		convertToEventCurrency = checked;
		preferencesEditor.putBoolean(CONVERT_TO_EVENT_CURRENCY, checked);
		preferencesEditor.apply();
	}

}
