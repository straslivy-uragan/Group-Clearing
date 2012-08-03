package cz.su.GroupClearing;

import java.util.Currency;

public class CurrencyList {

    private static CurrencyList instance = new CurrencyList();

    String[] currencyNames = null;
    String[] currencyCodes = null;

    private CurrencyList() {
    }

    public void fillData() {
        GroupClearingApplication myApp = GroupClearingApplication.getInstance();
        currencyNames = myApp.getResources().getStringArray(R.array.currency_names);
        currencyCodes = myApp.getResources().getStringArray(R.array.currency_codes);
    }
    
	public static CurrencyList getInstance() {
		return instance;
	}
	
	String[] getCurrencyNames() {
        if (currencyNames == null) {
            fillData();
        }
		return currencyNames;
	}
	
	String[] getCurrencyCodes() {
        if (currencyCodes == null) {
            fillData();
        }
		return currencyCodes;
	}
	
	String getCode(int position) {
        if (currencyCodes == null) {
            fillData();
        }
		return currencyCodes[position];
	}
	
	String getName(int position) {
        if (currencyNames == null) {
            fillData();
        }
		return currencyNames[position];
	}
	
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
	
	Currency getCurrency(int position) {
        return Currency.getInstance(getCode(position));
	}
}
