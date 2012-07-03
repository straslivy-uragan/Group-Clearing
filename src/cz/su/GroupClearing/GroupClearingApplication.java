/**
 * 
 */
package cz.su.GroupClearing;

import java.text.NumberFormat;
import java.util.Currency;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/*
 * TODO: Návrh vyrovnání 
 * TODO: Umožnit změnu měny události 
 * TODO: Umožnit zvolit měny jednotlivých transakcí s podporou
 * přepočtu, a to jednak s ručním nastavením kurzů, případně s jejich
 * stažením, ale pal by asi chtělo pamatovat si u každé transakce
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
			throws GCSyntaxException {
		// We cannot use format, because there might be both , and . used as
		// fraction digits separators.
		long value = 0;
		int index = 0;
		long sign = 1;
		while (index < valueString.length()
				&& Character.isSpace(valueString.charAt(index))) {
			++index;
		}
		if (index >= valueString.length()) {
			return 0;
		}
		if (valueString.charAt(index) == '-') {
			sign = -1;
			++index;
		}
		while (index < valueString.length()
				&& Character.isDigit(valueString.charAt(index))) {
			value = value * 10 + (valueString.charAt(index) - '0');
			++index;
		}
		if (index < valueString.length() && valueString.charAt(index) != '.'
				&& valueString.charAt(index) != ',') {
			throw new GCSyntaxException();
		}
		++index;
		int fractionIndex = 0;
		while (fractionIndex < currency.getDefaultFractionDigits()
				&& index + fractionIndex < valueString.length()
				&& Character.isDigit(valueString.charAt(index + fractionIndex))) {
			value = value * 10
					+ (valueString.charAt(index + fractionIndex) - '0');
			++fractionIndex;
		}
		int restIndex = index + fractionIndex;
		while (restIndex < valueString.length()) {
			if (!Character.isDigit(valueString.charAt(index + fractionIndex))) {
				throw new GCSyntaxException();
			}
			++restIndex;
		}
		while (fractionIndex < currency.getDefaultFractionDigits()) {
			value *= 10;
			++fractionIndex;
		}
		return sign * value;
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
