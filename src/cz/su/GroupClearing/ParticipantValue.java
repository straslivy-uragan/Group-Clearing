package cz.su.GroupClearing;

public class ParticipantValue {
    private long id;
    private long value;

    ParticipantValue(long id, long value) {
        this.id = id;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
