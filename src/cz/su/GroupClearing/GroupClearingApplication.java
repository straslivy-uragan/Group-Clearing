/**
 * 
 */
package cz.su.GroupClearing;

import java.text.NumberFormat;
import java.util.Currency;
import java.math.BigDecimal;

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

	private SharedPreferences preferences = null;
	private SharedPreferences.Editor preferencesEditor = null;

	public static final String SPLIT_CHANGE_WARNING_PREF = "split_change_warning";

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
	}

	public String formatCurrencyValue(long amount, Currency currency) {
		if (currencyFormatter == null) {
			currencyFormatter = NumberFormat.getInstance();
		}
		currencyFormatter.setGroupingUsed(false);
		currencyFormatter.setMaximumFractionDigits(currency
				.getDefaultFractionDigits());
		double value = amount
				/ Math.pow(10, currency.getDefaultFractionDigits());
		return currencyFormatter.format(value);
	}

	public String formatCurrencyValueWithSymbol(long amount, Currency currency) {
		if (currencyFormatterWithSymbol == null) {
			currencyFormatterWithSymbol = NumberFormat.getCurrencyInstance();
		}
		currencyFormatterWithSymbol.setCurrency(currency);
		double value = amount
				/ Math.pow(10, currency.getDefaultFractionDigits());
		return currencyFormatterWithSymbol.format(value);
	}

	public long parseCurrencyValue(String valueString, Currency currency)
			throws NumberFormatException {
            valueString = valueString.replace(',', '.');
            BigDecimal value = new BigDecimal(valueString);
            value = value.movePointRight(currency.getDefaultFractionDigits());
            return value.longValue();
	}

	public boolean getNoSplitChangeWarning() {
		return noSplitChangeWarning;
	}

	public void setNoSplitChangeWarning(boolean checked) {
		noSplitChangeWarning = checked;
		preferencesEditor.putBoolean(SPLIT_CHANGE_WARNING_PREF, checked);
		preferencesEditor.apply();
	}
}
