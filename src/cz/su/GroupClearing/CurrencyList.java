package cz.su.GroupClearing;

import java.util.Currency;

/**
 * Class representing list of currencies. The list is obtained from
 * resources, where it is as a pair of stringarrays,
 * <code>R.array.currency_names</code> with names of currencies and
 * <code>R.array.currency_codes</code> with currency codes. The list
 * contains currencies as specified in ISO 4217. The list is then used
 * to populate spinner for choosing currencies.
 *
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class CurrencyList {
    /** The single instance of currency list. The class has only one
     * instance stored in this variable.
     */
    private static CurrencyList instance = new CurrencyList();

    /** Array with currency names. */
    String[] currencyNames = null;
    /** Array with currency codes. The 3 characters long codes (such
     * as CZK for czech koruna. */
    String[] currencyCodes = null;

    /** Empty constructor does nothing. The fact, that this
     * constructor is empty ensures, that the only instance is the one
     * created within this class in <code>instance</code> variable.
     */
    private CurrencyList() {
    }

    /**
     * This method actually populates the member variables with data
     * got from application resources.
     */
    public void fillData() {
        GroupClearingApplication myApp = GroupClearingApplication.getInstance();
        currencyNames = myApp.getResources().getStringArray(R.array.currency_names);
        currencyCodes = myApp.getResources().getStringArray(R.array.currency_codes);
    }
    
	/** Returns the single instance of this class.
	 * @return The single instance of this class.
	 */
	public static CurrencyList getInstance() {
		return instance;
	}
	
	/** Returns array with currency names. If the array in
     * <code>currencyNames</code> is still empty, then
     * <code>fillData</code> is called to populate this array.
     *
	 * @return Array with currency names.
	 */
	String[] getCurrencyNames() {
        if (currencyNames == null) {
            fillData();
        }
		return currencyNames;
	}
		
	/** Returns array with currency codes. If the array in
     * <code>currencyCodes</code> is still empty, then
     * <code>fillData</code> is called to populate this array.
     *
	 * @return Array with currency codes.
	 */
    String[] getCurrencyCodes() {
        if (currencyCodes == null) {
            fillData();
        }
		return currencyCodes;
	}
	
	/** Returns currency code at given position.
	 * @param position Position within <code>currencyCodes</code> of code to return.
	 * @return Currency code at <code>currencyCodes[position]</code>.
	 */
	String getCode(int position) {
        if (currencyCodes == null) {
            fillData();
        }
		return currencyCodes[position];
	}
	
	/** Returns currency name at given position.
	 * @param position Position within <code>currencyNames</code> of
     * name to return.
	 * @return Currency name at <code>currencyNames[position]</code>.
	 */
	String getName(int position) {
        if (currencyNames == null) {
            fillData();
        }
		return currencyNames[position];
	}
	
	/** Returns position of currency with given code.
	 * @param code Code of currency, for which position should be
     * returned.
	 * @return Position withing <code>currencyCodes</code> of currency
     * with code given in <code>code</code>.
	 */
	int getPosition(String code) {
        // Assume that the array with codes is sorted
        // lexicographically and perform binary search.
        if (currencyCodes == null) {
            fillData();
        }
        int left = 0;
        int right = currencyCodes.length;
        while (left <= right) {
            int middle = (left + right) / 2;
            int comp = code.compareTo(currencyCodes[middle]);
            if (comp == 0) {
                return middle;
            } else if (comp < 0) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        return -1;
	}
	
    /** Returns <code>Currency</code> object corresponding to code at
     * given position.
	 * @param position Position of currency we want to return.
	 * @return <code>Currency</code> object created for <code>code</code> at given
     * <code>position</code>.
	 */
	Currency getCurrency(int position) {
        return Currency.getInstance(getCode(position));
	}
}
