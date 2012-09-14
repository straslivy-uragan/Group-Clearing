package cz.su.GroupClearing;

import java.math.BigDecimal;

public class ParticipantValue {
	private long id;
	private BigDecimal value;

	ParticipantValue(long id, BigDecimal value) {
		this.id = id;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}
}
