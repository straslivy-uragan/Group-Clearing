package cz.su.GroupClearing;

import java.math.BigDecimal;

/** Encodes pair of <code>id</code> and <code>value</code> of a
 * participant.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class ParticipantValue {
    /** The <code>id</code> of the participant. */
	private long id;
    /** Ghe <code>value</code> of the participant. */
	private BigDecimal value;

    /** Initializes new pair with given <code>id</code> and
     * <code>value</code>.
     *
     * @param id Id of participant.
     * @param value Value of participant.
     */
	ParticipantValue(long id, BigDecimal value) {
		this.id = id;
		this.value = value;
	}

    /** Returns the <code>id</code> of participant.
     * @return The <code>id</code> of participant.
     */
	public long getId() {
		return id;
	}

    /** Returns the <code>value</code> of participant. 
     * @return The <code>value</code> of participant.
     */
	public BigDecimal getValue() {
		return value;
	}

    /** Sets new <code>value</code> of participant.
     *
     * @param value New <code>value</code> of participant.
     */
	public void setValue(BigDecimal value) {
		this.value = value;
	}
}
