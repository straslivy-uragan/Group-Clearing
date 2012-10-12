package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.Currency;

public class SimpleTransaction {
    private final long receiverId;
    private final long payerId;
    private final BigDecimal value;
    private final Currency currency;

    public SimpleTransaction(long receiverId, long payerId, BigDecimal value, Currency currency) {
        this.receiverId = receiverId;
        this.payerId = payerId;
        this.value = value;
        this.currency = currency;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public long getPayerId() {
        return payerId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }
}
