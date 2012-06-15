/**
 * 
 */
package cz.su.GroupClearing;

import java.util.HashMap;
import java.util.Currency;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author su
 * 
 */
public class ClearingTransaction {

	public class ParticipantInfo {
		long value;
		boolean marked;

		public ParticipantInfo(long aValue, boolean aMark) {
			value = aValue;
			marked = aMark;
		}

		public long getValue() {
			return value;
		}

		public boolean isMarked() {
			return marked;
		}

		public void setMarked(boolean aMark) {
			marked = aMark;
		}

		public void setValue(long aValue) {
			value = aValue;
		}
	}

	private long id;
	private long eventId;
	/**
	 * Amount value is stored as long in “smaller” currency.
	 * 
	 * I.e. like in case of USD, amount represents number of cents, in case of
	 * CZK amount represents haléře and so on. This should be precise enough
	 * and it is simpler use than BigDecimal. Also it allows us to use INTEGER
	 * for representing this value in SQLite database which makes it possible to
	 * query the database for the arithmetic over these values (at least for
	 * addition or subtraction, not really the multiplication or division, but
	 * this is not necessary in case of monetary values).
	 */
	private long amount;
	/**
	 * Sum of payments.
	 * 
	 * This is a convenience value for checking whether payments are equal to
	 * the total value of transaction.
	 */
	private long negativeAmount;
	/**
	 * Sum of positive amounts of participants.
	 * 
	 * This is a convenience value for checking whether payments are equal to
	 * the total value of transaction.
	 */
	private long positiveAmount;
	private String name;
	private GregorianCalendar calendarDate;
	private Currency currency;
	private HashMap<Long, ParticipantInfo> participantsInfo;
	private String note;
	/**
	 * If the amount should be split evenly among the participants.
	 */
	private boolean splitEvenly = true;
	/**
	 * @brief Id of receiver of the money amount.
	 * 
	 *        This is usually a person who paid for something and now he/she
	 *        should get money from others. This person is called receiver
	 *        because in general it is the person who is now receiving money
	 *        from others as transaction needs not to be connected to an actual
	 *        payment to outsiders.
	 * 
	 *        Note, that in case this value is -1, no explicit receiver is set.
	 *        It does not necessarily mean that the transaction is unbalanced,
	 *        because in participantsInfo the values of participants can be both
	 *        positive and negative. Which means that transaction can be general
	 *        in this sense. In case of general transaction you should keep
	 *        amount zero while the actual amounts are then positiveAmount and
	 *        negativeAmount, the former with sum of positive values and the
	 *        latter with the sum of negative values in participantsInfo hash
	 *        map.
	 */
	private long receiverId = -1;
	private int numberOfMarkedParticipants = 0;

	public ClearingTransaction(long anId, long anEventId) {
		super();
		id = anId;
		eventId = anEventId;
		calendarDate = new GregorianCalendar();
		currency = Currency.getInstance(Locale.getDefault());
		amount = 0;
		positiveAmount = 0;
		negativeAmount = 0;
		participantsInfo = new HashMap<Long, ParticipantInfo>();
		note = "";
		receiverId = -1;
		splitEvenly = true;
	}

	public Date getDate() {
		return calendarDate.getTime();
	}

	public void setDate(int year, int month, int day) {
		calendarDate.set(Calendar.YEAR, year);
		calendarDate.set(Calendar.MONTH, month);
		calendarDate.set(Calendar.DAY_OF_MONTH, day);
	}

	public void setDate(Date aDate) {
		calendarDate.setTime(aDate);
	}

	public int getYear() {
		return calendarDate.get(Calendar.YEAR);
	}

	public int getMonth() {
		return calendarDate.get(Calendar.MONTH);
	}

	public int getDayOfMonth() {
		return calendarDate.get(Calendar.DAY_OF_MONTH);
	}

	public void setAmount(long newAmount) {
		amount = newAmount;
	}

	public long getAmount() {
		return amount;
	}

	public long getPositiveAmount() {
		return positiveAmount;
	}

	public long getNegativeAmount() {
		return negativeAmount;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public long getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(long anId) {
		receiverId = anId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public boolean getSplitEvenly() {
		return splitEvenly;
	}

	public void setSplitEvenly(boolean split) {
		splitEvenly = split;
	}

	public long getId() {
		return id;
	}

	public long getEventId() {
		return eventId;
	}

	public int getNumberOfMarkedParticipants() {
		return numberOfMarkedParticipants;
	}

	/**
	 * @brief Sets value of participant with given id in this transaction.
	 * 
	 *        Does not modify marks. If participant with given id is not yet in
	 *        the participantsInfo hash, then it is inserted with mark set to
	 *        false.
	 */
	public void setParticipantValue(long participantId, long aValue) {
		Long participantIdObject = new Long(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			if (info.getValue() > 0) {
				positiveAmount -= info.getValue();
			} else {
				negativeAmount -= info.getValue();
			}
			info.setValue(aValue);
		} else {
			info = new ParticipantInfo(aValue, false);
			participantsInfo.put(participantIdObject, info);
		}
		if (aValue > 0) {
			positiveAmount += aValue;
		} else {
			negativeAmount += aValue;
		}
	}

	/**
	 * @brief Sets mark of participant with given id.
	 * 
	 *        If participant with given id does not exist yet, it is inserted
	 *        with value 0 only in case aMark is true.
	 */
	public void setParticipantMark(long participantId, boolean aMark) {
		Long participantIdObject = new Long(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			int oldMark = (info.isMarked() ? 1 : 0);
			int newMark = (aMark ? 1 : 0);
			info.setMarked(aMark);
			numberOfMarkedParticipants += newMark - oldMark;
		} else {
			if (aMark) {
				info = new ParticipantInfo(0, true);
				++numberOfMarkedParticipants;
				participantsInfo.put(participantIdObject, info);
			}
		}
	}

	public long getParticipantValue(long participantId) {
		Long participantIdObject = new Long(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			return info.getValue();
		}
		return 0;
	}

	public boolean isParticipantMarked(long participantId) {
		Long participantIdObject = new Long(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			return info.isMarked();
		}
		return false;
	}

	public void addParticipant(long participantId, long aValue, boolean aMark) {
		ParticipantInfo info = new ParticipantInfo(aValue, aMark);
        participantsInfo.put(new Long(participantId), info);
	}
	
	@Override
	public String toString() {
		return "ClearingTransaction [id=" + id + ", eventId=" + eventId
				+ ", calendarDate=" + calendarDate + ", amount=" + amount
				+ ", positiveAmount=" + positiveAmount + ", negativeAmount="
				+ negativeAmount + ", currency=" + currency + ", participants="
				+ participantsInfo + ", note=" + note + "]";
	}
}
