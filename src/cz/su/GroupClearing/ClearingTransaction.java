/**
 * 
 */
package cz.su.GroupClearing;

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author su
 * 
 */
public class ClearingTransaction {

	public class ParticipantInfo {
		long value;
		boolean marked;
        long id;

		public ParticipantInfo(long anId, long aValue, boolean aMark) {
			value = aValue;
			marked = aMark;
            id = anId;
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
	
        public long getId() {
            return id;
        }
    }

	private final long id;
	private final long eventId;
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
	private final GregorianCalendar calendarDate;
	private Currency currency;
	private final HashMap<Long, ParticipantInfo> participantsInfo;
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

    public long getBalance() {
        return negativeAmount - positiveAmount;
    }

    public boolean hasNonzeroValues() {
        return (positiveAmount != 0
                || negativeAmount != 0
                || amount != 0
                || receiverId >= 0);
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

    /**@brief Sets the split evenly flag.
     *
     * @param split New value of splitEvenly flag.
     *
     * Note, that when changing the type of transaction, the values of
     * participants and amount may change, this is because their
     * meaning in each kind of transaction is different.
     */
	public void setSplitEvenly(boolean split) {
		splitEvenly = split;
    }

	public long getId() {
		return id;
	}

	public long getEventId() {
		return eventId;
	}

	/**
	 * @brief Sets value of participant with given id in this transaction.
	 */
	public void setAndSaveParticipantValue(long participantId, long aValue, GCDatabase db) {
		Long participantIdObject = Long.valueOf(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
        if (info == null) {
            info = new ParticipantInfo(participantId, 0, false);
            participantsInfo.put(participantIdObject, info);
        }
        if (aValue != info.getValue()) {
            if (splitEvenly) {
                splitEvenly = false;
                db.updateTransactionSplitEvenly(this);
            }
            if (info.getValue() > 0) {
                positiveAmount -= info.getValue();
            } else {
                negativeAmount += info.getValue();
            }
            if (aValue > 0) {
                positiveAmount += aValue;
            } else {
                negativeAmount -= aValue;
            }
            if (positiveAmount < 0) {
                positiveAmount = 0;
            }
            if (negativeAmount < 0) {
                negativeAmount = 0;
            }
            info.setValue(aValue);
            info.setMarked(info.getValue() != 0);
            db.updateTransactionParticipantValue( eventId,
                id, participantId, info.getValue(), info.isMarked());
            if (amount != positiveAmount) {
                amount = positiveAmount;
                db.updateTransactionAmount(this);
            }
        }
	}

	/**
	 * @brief Sets mark of participant with given id.
	 * 
	 *        If participant with given id does not exist yet, it is inserted
	 *        with value 0 only in case aMark is true.
	 */
	public void setParticipantMark(long participantId, boolean aMark) {
		Long participantIdObject = Long.valueOf(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			info.setMarked(aMark);
		} else {
			if (aMark) {
				info = new ParticipantInfo(participantId, 0, true);
				participantsInfo.put(participantIdObject, info);
			}
		}
	}

	public long getParticipantValue(long participantId) {
		Long participantIdObject = Long.valueOf(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			return info.getValue();
		}
		return 0;
	}

	public boolean isParticipantMarked(long participantId) {
		Long participantIdObject = Long.valueOf(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			return info.isMarked();
		}
		return false;
	}

	public void addParticipant(long participantId, long aValue, boolean aMark) {
		ParticipantInfo info = new ParticipantInfo(participantId, aValue, aMark);
        if (aMark) {
            if (aValue > 0) {
                positiveAmount += aValue;
            }
            else {
                negativeAmount -= aValue;
            }
        }
        participantsInfo.put(Long.valueOf(participantId), info);
	}

    public long recomputeAndSaveChanges(GCDatabase db) {
        if (splitEvenly) {
            positiveAmount = 0;
            negativeAmount = 0;
            long negAmount = 0;
            long share = 0;
            long numberOfMarkedParticipants = 0;
            for (ParticipantInfo info : participantsInfo.values()) {
                if (info.isMarked()) {
                    ++ numberOfMarkedParticipants;
                }
            }
			if (numberOfMarkedParticipants > 0) {
				share = amount / numberOfMarkedParticipants;
                negAmount = share * numberOfMarkedParticipants;
            }
            for (ParticipantInfo info : participantsInfo.values()) {
                long value = 0;
                if (info.isMarked()) {
                    value = -share;
                    if (negAmount < amount) {
                        --value;
                        ++negAmount;
                    }
                }
                if (info.getId() == receiverId) {
                    value += amount;
                }
                if (value > 0) {
                    positiveAmount += value;
                } else {
                    negativeAmount -= value;
                }
                if (info.getValue() != value) {
                    info.setValue(value);
                    if (db != null) {
                        db.updateTransactionParticipantValue(eventId,
                                id, info.getId(), value, info.isMarked());
                    }
                }
            }
        } else {
            // Go through all participants and unmark those with 0 value.
           positiveAmount = 0;
           negativeAmount = 0;
                for (ParticipantInfo info : participantsInfo.values()) {
                    boolean oldMark = info.isMarked();
                    info.setMarked (info.getValue() != 0);
                    if (oldMark != info.isMarked()) {
                        if (db != null) {
                            db.updateTransactionParticipantValue(eventId,
                                    id, info.getId(), info.getValue(),
                                    info.isMarked());
                        }
                    }
                    if (info.getValue() > 0) {
                        positiveAmount += info.getValue();
                    }
                    else {
                        negativeAmount -= info.getValue();
                    }
                }
            if (amount != positiveAmount) {
                amount = positiveAmount;
                if (db != null) {
                    db.updateTransactionAmount(this);
                }
            }
        }
        return negativeAmount - positiveAmount;
    }
    
    public void resetValues(GCDatabase db) {
        for (ParticipantInfo info : participantsInfo.values()) {
            info.setValue(0);
            info.setMarked(splitEvenly);
        }
        positiveAmount = negativeAmount = amount = 0;
        receiverId = -1;
        if (db != null) {
            db.resetTransactionParticipantsValues(this, splitEvenly);
            db.updateTransactionAmount(this);
            db.updateTransactionReceiverId(this);
        }
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
