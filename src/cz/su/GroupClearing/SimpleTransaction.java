package cz.su.GroupClearing;

import java.math.BigDecimal;

public class SimpleTransaction {
    private long receiverId;
    private long payerId;
    private BigDecimal value;

    public SimpleTransaction(long receiverId, long payerId, BigDecimal value) {
        this.receiverId = receiverId;
        this.payerId = payerId;
        this.value = value;
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

}
