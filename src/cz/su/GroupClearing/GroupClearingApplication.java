package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Strašlivý Uragán <straslivy.uragan@gmail.com>
 * @version 1.0
 * @since 1.0
 * 
 * This class serves as an interface to application related
 * settings and tools. This class has only one instance which
 * can be obtained using @see getInstance method.
 *
 * As of now, there are three preferences, which can be set by the
 * user in Group Clearing.
 *
 * <table class="PreferenceTable">
 * <thead>
 * <tr>
 * <th>Preference</th>
 * <th>Explanation</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>
 * <code>noSplitChangeWarning</code></td>
 * <td>By this preference
 * user can specify, whether he/she should be warned in case of
 * switching “Split evenly” checkbox in
 * <code>TransactionEditActivity</code>. If
 * <code>noSplitChangeWarning</code> is true and in cause of the above
 * mentioned checkbox switching values may change in the transaction,
 * warning dialog is presented to the user. Otherwise user is not
 * warned. There are two places, where this preference can be set. It
 * can be set using checkbox within the mentioned warning dialog, or
 * it can be set in <code>SettingsActivity</code>.</td></tr>
 * <tr>
 * <td><code>supportMultipleCurrencies</code></td>
 * <td>This preference value
 * specifies, whether multiple currencies should be supported or not.
 * If the support of multiple currencies is on, user can set main
 * event currency and a different currency and rate for each
 * transaction. In case the support of multiple currencies is off, the
 * event currency and all transaction currencies are set to default
 * currency for current locale at the time of creation. Note, that if
 * you switch currency off, all events and transactions which already
 * use multiple currencies are left untouched, however when event or
 * transaction is shown next time, user has no possibility to change
 * the currency, unless multiple currencies support is switched back
 * on.</td></tr>
 * <tr>
 * <td><code>convertToEventCurrency</code></td>
 * <td>This preference value
 * specifies whether values of transactions are always converted to
 * main event currency using specified rate before use. This
 * preference does not affect values in transaction as set or
 * presented in <code>TransactionEditActivity</code>. This preference affects
 * mainly two places (and similar others in the same way). In
 * <code>ParticipantsListActivity</code>, when values of participants
 * are computed
 * and <code>convertToEventCurrency</code> is set to true, then values of
 * participants in all transactions are summed together and converted
 * to main event transaction. If on the other hand
 * <code>convertToEventCurrency</code> is set to false, the values in different
 * currencies are treated separately and special sum value is
 * presented for each currency used in some transaction of the event.
 * Similarly this preference affects <code>SuggestClearanceActivity</code>, in case
 * <code>convertToEventCurrency</code> is set to true, clearance is suggested in
 * main event currency only where values in different currencies are
 * converted to the main event currency before they are used. If
 * <code>convertToEventCurrency</code> is set to false, then clearance in each
 * currency is computed and suggested separately.
 * </td></tr>
 * </tbody>
 * </table>
 *
 * @see TransactionEditActivity
 * @see ParticipantsListActivity
 * @see SuggestClearanceActivity
 */
public class GroupClearingApplication extends Application {
	/**
	 * The sole instance of this class.
	 */
	private static GroupClearingApplication instance;
	/**
     * Formatter for formatting currency value together with currency
     * symbol or shortcut.
	 */
	private NumberFormat currencyFormatterWithSymbol = null;
	/**
	 * Formatter for formatting currency value without currency symbol.
	 */
	private NumberFormat currencyFormatter = null;

	/**
     * Preference value specifying, whether on split radio change
     * warning should be shown.
	 * 
     * This preference value determines behaviour in
     * <code>TransactionEditActivity</code>. When split evenly
     * checkbox is switched and computed values in transaction are
     * about to change as a
     * result, then either warning dialog is shown to the user when
     * <code>noSpliChangeWarning</code> is true, or it is not shown otherwise.
     *
     * @see cz.su.GroupClearing.TransactionEditActivity
	 */
	private boolean noSplitChangeWarning = false;
	/**
	 * Preference value specifying, whether multiple currencies should be
	 * supported.
	 */
	private boolean supportMultipleCurrencies = false;
	/**
	 * Preference value specifying, whether all value should be converted to
	 * event currency.
	 * 
     * In case of multiple currencies suport this preference value
     * determines, whether values should be converted to main event
     * currency. This affects values presented to the user in list of
     * participants and when clearance is being suggested.
	 */
	private boolean convertToEventCurrency = true;

    /**
     * Object with application preferences.
     */
	private SharedPreferences preferences = null;
    /**
     * Editor for modifying application preferences.
     */
	private SharedPreferences.Editor preferencesEditor = null;

    /**
     * String tag used for storing application preference determining,
     * whether warning should be shown in case of split checkbox state
     * change.
     *
     * @see noSplitChangeWarning
     */
	public static final String SPLIT_CHANGE_WARNING_PREF = "split_change_warning";
    /**
     * String tag used for storing application preference determining,
     * whether multiple currencies should be supported.
     *
     * @see supportMultipleCurrencies
     */
    public static final String SUPPORT_MULTIPLE_CURRENCIES = "support_multiple_currencies";
	/**
     * String tag used for storing application preference determining,
     * whether values should be always converted to main event
     * currency.
     *
     * @see convertToEventCurrency
     */
    public static final String CONVERT_TO_EVENT_CURRENCY = "convert_to_event_currency";

    /**
     * Returns the sole instance of this class.
     *
     * @return The sole instance of this class.
     */
	public static GroupClearingApplication getInstance() {
		return instance;
	}

    /**
     * Called, when application object is being created.
     */
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

    /**
     * This method formats given value in given currency without
     * currency symbol used.
     *
     * This method takes two parameters, amount and currency. The
     * output is a string with properly formatted value in given
     * currency. In this version currency symbol or name is not part
     * of the output string.
     *
     * @param amount Value to be formatted.
     * @param currency Currency of the value to be formatted.
     *
     * @return String with formatted value, sign or name of currency
     * is not included.
     */
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

    /**
     * This method formats given value in given currency with
     * properly formatted currency symbol or name.
     *
     * This method takes two parameters, amount and currency. The
     * output is a string with properly formatted value in given
     * currency. In this version currency symbol or name is formatted
     * as a part of the output string.
     *
     * @param amount Value to be formatted.
     * @param currency Currency of the value to be formatted.
     *
     * @return String with formatted value, sign or name of currency
     * is part of the string.
     */
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

    /**
     * This method parses value stored in given string as a value in 
     * given currency.
     *
     * This method takes two parameters, string containing value and
     * currency. It then parses string value with respect to given
     * currency. Due to badly used fraction digit in android, we had
     * to add possibility to use both '.' and ',' as fraction symbols,
     * due to this we could not simply used <code>BigDecimal</code> constructor.
     * Actually, we use this constructor, but before that we replace
     * occurrences of ',' with '.'.
     *
     * @param valueString String with value to be parsed.
     * @param currency Currency of the value. This parameter is used
     * to properly set scale of the parsed value.
     *
     * @return Parsed value as <code>BigDecimal</code>.
     * @throws NumberFormatException In case <code>BigDecimal</code> constructor
     * fails to parse the value.
     */
	public BigDecimal parseCurrencyValue(String valueString, Currency currency)
			throws NumberFormatException {
		valueString = valueString.replace(',', '.');
		BigDecimal value = new BigDecimal(valueString);
		return value.setScale(currency.getDefaultFractionDigits(),
				BigDecimal.ROUND_HALF_EVEN);
	}

    /**
     * Returns value of <code>noSplitChangeWarning</code> preference value.
     *
     * This preference value determines behaviour in
     * <code>TransactionEditActivity</code>. When split evenly checkbox is switched
     * and computed values in transaction are about to change as a
     * result, then either warning dialog is shown to the user when
     * <code>noSpliChangeWarning</code> is true, or it is not shown otherwise.
     *
     * @see noSplitChangeWarning
     * @see TransactionEditActivity
     *
     * @return Value of <code>noSplitChangeWarning</code> preference value.
     */
	public boolean getNoSplitChangeWarning() {
		return noSplitChangeWarning;
	}

    /**
     * Sets the value of <code>noSplitChangeWarning</code> preference value.
     * @param checked New value of <code>noSplitChangeWarning</code> preference
     * value.
     *
     * @see noSplitChangeWarning
     * @see TransactionEditActivity
     */
	public void setNoSplitChangeWarning(boolean checked) {
		noSplitChangeWarning = checked;
		preferencesEditor.putBoolean(SPLIT_CHANGE_WARNING_PREF, checked);
		preferencesEditor.apply();
	}

    /**
     * Returns value of preference specifying, whether multiple
     * currencies should be supported.
     *
     * @see supportMultipleCurrencies
     *
     * @return Value of <code>supportMultipleCurrencies</code> preference value.
     */
	public boolean getSupportMultipleCurrencies() {
		return supportMultipleCurrencies;
	}

    /**
     * Sets the value of <code>supportMultipleCurrencies</code> preference value.
     *
     * Sets the value of preference value specifying, whether multiple
     * currencies should be supported.
     * @param checked New value of support multiple currencies
     * preference value.
     *
     * @see supportMultipleCurrencies
     */
	public void setSupportMultipleCurrencies(boolean checked) {
		supportMultipleCurrencies = checked;
		preferencesEditor.putBoolean(SUPPORT_MULTIPLE_CURRENCIES, checked);
		preferencesEditor.apply();
	}

    /**
     * Returns value of <code>convertToEventCurrency</code> preference value.
     *
     * Returns the value of preference specifying, whether values
     * should be converted to main event currency before they are
     * presented to the user in participants list or before they are
     * used in further computation such as clearance suggestion.
     *
     * @see convertToEventCurrency
     *
     * @return Value of <code>convertToEventCurrency</code> preference.
     */
	public boolean getConvertToEventCurrency() {
		return convertToEventCurrency;
	}

    /**
     * Sets value of <code>convertToEventCurrency</code> preference value.
     *
     * Sets new value of <code>convertToEventcurrency</code> preference specifying,
     * whether values should be converted to main event currency
     * before they are presented to the user in participants list or
     * before they are used in further computation such as clearance
     * suggestion.
     * @param checked New value of <code>convertToEventCurrency</code> preference
     * value.
     *
     * @see convertToEventCurrency
     */
	public void setConvertToEventCurrency(boolean checked) {
		convertToEventCurrency = checked;
		preferencesEditor.putBoolean(CONVERT_TO_EVENT_CURRENCY, checked);
		preferencesEditor.apply();
	}

}
