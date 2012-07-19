package cz.su.GroupClearing;

public class SimpleTransaction {
    private long receiverId;
    private long payerId;
    private long value;

    public SimpleTransaction(long receiverId, long payerId, long value) {
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

    public long getValue() {
        return value;
    }

}
