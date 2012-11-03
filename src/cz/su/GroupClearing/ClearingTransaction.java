/**
 * 
 */
package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/** This class represents a single transaction within a clearing
 * event. A transaction can be of two types, either the values are
 * split evenly and a person who receives many is given (altough not
 * neccessarily), or it is a general transaction which assigns a
 * positive (i.e. to be received) or negative (i.e. to be paid) value
 * to every participant. The type of the transaction is
 * <code>splitEvenly</code> field.
 *
 * Every transaction has assigned participants, in this object the
 * participants are stored in <code>participantsInfo</code>, which is
 * a <code>HashMap</code> hashed using ids of participants (as a long
 * value). Each participant is then described using
 * <code>ParticipantInfo</code> object. In this object only neccessary
 * values about each participant are stored, i.e. the id of
 * participant specifying person id. Value of the participant within
 * this transaction, the meaning of this value is a bit different
 * dependind <code>splitEvenly</code> state. Each person can moreover
 * be marked, a marked participant really participates in transaction,
 * unmarked participant is not taken into account and is given 0
 * value. This means, that a transaction can accomodate all persons
 * participating in the corresponding event, but only those who are
 * marked really participate in this transaction, too. This is easier
 * for managing visible marking or unmarking participants in
 * <code>TransactionEditActivity</code>. 
 *
 * In case of <code>splitEvenly</code> being true, the value of each
 * marked participant is equal and corresponds to the value to be paid
 * by the participant (i.e. it is negative), in this case a
 * transaction can store an id of person which should receive the
 * money in <code>receiverId</code>, note that this person is not
 * neccessarily marked in which case it does not pay anything, but
 * only receives the total. Otherwise to compute the real value of
 * receiving person, we have to subtract its value from the total
 * value of transaction.
 *
 * In case of <code>splitEvenly</code> being false, the marked
 * participants are those with nonzero value (either positive or
 * negative). In this case this value represents what a person should
 * pay or receive and the values can differ from person to person.
 *
 * Due to very different natures of these transaction types and their
 * storage, if this type changes, all values are reseted.
 *
 * Each transaction can have its own currency, in case this currency
 * is different from the currency of the event, a rate can be assigned
 * for converting this currency to the event currency. Whether this
 * rate is then used or not depends on whether
 * <code>convertToEventCurrency</code> application preference value is
 * true or false.
 *
 * Each transaction has also its total value stored in
 * <code>amount</code> field. This amount is either equal to sum of
 * positive values in case of general transaction (i.e. with
 * <code>splitEvenly</code> set to false), or it is a special value to
 * be split among participants in case of <code>splitEvenly</code>
 * being true.
 *
 * Other than values and participants, each transaction has its
 * <code>id</code> number, date, note and other auxiliary data. See
 * particular comments for details.
 *
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class ClearingTransaction {

    /** This class represents neccessary information about participant
     * of a transaction. The stored information consists of person's
     * id, value, and mark status. For more detailed information on
     * these fields see the outer <code>ClearingTransaction</code>
     * class description.
     *
     * @author Strašlivý Uragán
     * @version 1.0
     * @since 1.0
     */
	public class ParticipantInfo {
        /** Id number of the person being described.
         */
        long id;
        /** Value of participant in the transaction.
         */
		BigDecimal value;
        /** Whether participants is marked in the transaction or not.
         * Basically, this value determines whether this participants
         * is accounted in the transaction or not.
         */
		boolean marked;
        
		/** Constructor for construction the object with given values.
         *
		 * @param anId Id of the person to be described.
		 * @param aValue Value of the participant in the transaction.
		 * @param aMark Whether this participant is marked in the
         * transaction or not.
		 */
		public ParticipantInfo(long anId, BigDecimal aValue, boolean aMark) {
			value = aValue;
			marked = aMark;
            id = anId;
		}

		/** Returns value of this participant in the transaction.
		 * @return Value of this participant in the transaction.
		 */
		public BigDecimal getValue() {
			return value;
		}

		/** Returns mark of this participant.
		 * @return The state of the <code>mark</code> status of this
         * participant.
		 */
		public boolean isMarked() {
			return marked;
		}

		/** Sets mark of this participant.
		 * @param aMark New state of the <code>mark</code> status of
         * this participant.
		 */
		public void setMarked(boolean aMark) {
			marked = aMark;
		}

		/** Sets value of this participant in the transaction.
		 * @param aValue New value of the participant in the
         * transaction.
		 */
		public void setValue(BigDecimal aValue) {
			value = aValue;
		}
	
        /** Returns <code>id</code> of this participant.
         * @return Person <code>id</code> of this participant.
         */
        public long getId() {
            return id;
        }
    }

	/**
	 * Id value of this transaction.
	 */
	private final long id;
	/**
	 * Id of the event of this transaction.
	 */
	private final long eventId;
	/**
	 * Total amount of this transaction. In case of split evenly
     * transaction, this is a special value which is split among
     * participants. In case of a general transaction
     * <code>amount</code> is equal to <code>positiveAmount</code>
     * (i.e. it is a sum of positive values of participants).
     */
	private BigDecimal amount;
	/**
     * Absolute value of sum of negative values of participants in
     * this transaction (payments). Note in particular, that as an
     * absolute value, this amount is always nonnegative. This is a
     * convenience value for checking whether payments are equal to
     * the total value of transaction.
	 */
	private BigDecimal negativeAmount;
	/**
	 * Sum of positive amounts of participants.
	 * 
	 * This is a convenience value for checking whether payments are equal to
	 * the total value of transaction.
	 */
	private BigDecimal positiveAmount;
    /** Name of this transaction. */
	private String name;
    /** Date of the transaction. */
	private final GregorianCalendar calendarDate;
    /** Currency of this transaction. */
	private Currency currency;
    /** Hash table storing participants of the transaction. The hash
     * value is the id of each participant.
     */
	private final HashMap<Long, ParticipantInfo> participantsInfo;
    /** Note associated with this transaction. */
	private String note;
    /**
     * Rate with respect to default currency of event.
     *
     * If default currency of event is A and currency of this
     * transaction is B, then 1B = rate A. E.g. amount of transaction
     * in A is obtained by multiplying rate * amount. Multiplication
     * by rate should not be used when A is the same currency as B
     * This weak restriction is put in place just to avoid cases, when
     * both currencies are same but somehow the rate is not 1. If rate
     * is 1, then it works correctly as <code>rate</code> is
     * stored as <code>BigDecimal</code> which is precise enough.
     */
    private BigDecimal rate;
	/**
	 * If the amount should be split evenly among the participants.
     * For detail see the class description.
	 */
	private boolean splitEvenly = true;
	/**
	 * Id of receiver of the money amount. This value plays any role
     * only in case of split evenly type transaction.
	 * 
	 * This is usually a person who paid for something and now he/she
	 * should get money from others. This person is called receiver
	 * because in general it is the person who is now receiving money
	 * from others as transaction needs not to be connected to an actual
	 * payment to outsiders.
	 *
     * In case no receiver is assigned, the value of
     * <code>receiverId</code> is -1. In case of general transaction
     * this value is meaningless.
     */
	private long receiverId = -1;

	/** Constructs a transaction with only id and event id values.
     * The rest of the values is set to defaults.
     *
	 * @param anId Id of transaction being constructed.
	 * @param anEventId Id of event to which this transaction belongs.
	 */
	public ClearingTransaction(long anId, long anEventId) {
		super();
		id = anId;
		eventId = anEventId;
		calendarDate = new GregorianCalendar();
		currency = Currency.getInstance(Locale.getDefault());
		amount = BigDecimal.ZERO;
		positiveAmount = BigDecimal.ZERO;
		negativeAmount = BigDecimal.ZERO;
        rate = BigDecimal.ONE;
		participantsInfo = new HashMap<Long, ParticipantInfo>();
		note = "";
		receiverId = -1;
		splitEvenly = true;
	}

	/** Returns the date of this transaction.
	 * @return Date of this transaction as a <code>Date</code> object.
	 */
	public Date getDate() {
		return calendarDate.getTime();
	}

	/** Sets the date of this transaction. This version allows to set
     * date of this transaction using triple of
     * <code>year</code>, <code>month</code>, and <code>day</code> of
     * month values.
     *
	 * @param year Year part of the date of the transaction.
	 * @param month Month part of the date of the transaction.
	 * @param day Day of month part of the date of the transaction.
	 */
	public void setDate(int year, int month, int day) {
		calendarDate.set(Calendar.YEAR, year);
		calendarDate.set(Calendar.MONTH, month);
		calendarDate.set(Calendar.DAY_OF_MONTH, day);
	}

	/** Sets the date of this transaction. This version allows to set
     * date of this transaction using a <code>Date</code> object.
     *
	 * @param aDate New date of the transaction.
	 */
	public void setDate(Date aDate) {
		calendarDate.setTime(aDate);
	}

	/** Returns the year part of the date of this transaction.
	 * @return Year part of the date of this transaction.
	 */
	public int getYear() {
		return calendarDate.get(Calendar.YEAR);
	}

	/** Returns the month part of the date of this transaction.
	 * @return Month part of the date of this transaction.
	 */
	public int getMonth() {
		return calendarDate.get(Calendar.MONTH);
	}

	/** Returns the day of month part of the date of this transaction.
	 * @return Day of month part of the date of this transaction.
	 */
	public int getDayOfMonth() {
		return calendarDate.get(Calendar.DAY_OF_MONTH);
	}

    /** Sets new amount of the transaction. Note, that it makes to set
     * the amount of transaction only in case of split evenly
     * transaction. In case of a general transaction, the amount of
     * transaction is computed as a sum of positive values and this
     * method does nothing. This method does not call
     * <code>recomputeAndSaveChanges</code> method
     * for recomputing values, it should be called explicitely after
     * amount has changed.
     *
	 * @param newAmount New amount of this transaction.
	 */
	public void setAmount(BigDecimal newAmount) {
        if (splitEvenly) {
            amount = newAmount;
        }
	}

    /** Returns the amount of this transaction. In any case this
     * returns the value of <code>amount</code> member variable. In
     * case of general transaction this should be equal to
     * <code>positiveAmount</code> if
     * <code>recomputeAndSaveChanges</code> was called properly.
     *
	 * @return The value of <code>amount</code> member variable.
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/** Returns the positive amount of this transaction. Positive
     * amount is the sum of positive values in the transaction. In any
     * type of transactin it should in fact be equal to
     * <code>amount</code> after <code>recomputeAndSaveChanges</code>.
     *
	 * @return The value of <code>positiveAmount</code> member
     * variable.
	 */
	public BigDecimal getPositiveAmount() {
		return positiveAmount;
	}

    /** Returns the negative amount of this transaction. Negative
     * amount is the absolute value of sum of negative values in the
     * transaction.
     *
	 * @return The value of <code>negativeAmount</code> member
     * variable.
	 */
	public BigDecimal getNegativeAmount() {
		return negativeAmount;
	}

    /** Return difference of <code>negativeAmount</code> and
     * <code>positiveAmount</code>. I.e. this method returns 0 if
     * payed value is the same as the value received in total,
     * otherwise it returns the difference. Note, that
     * <code>negativeAmount</code> is an absolute value of sum of
     * negative values of participants and as such is nonnegative. In
     * particular, if
     * <code>positiveAmount</code>&gt;<code>negativeAmount</code>,
     * then the balance is negative. This corresponds to the
     * intuition, that balance here is the value, which would have to
     * be added to the transaction to make it balanced.
     *
     * @return Result of
     * (<code>negativeAmount</code>-<code>positiveAmount</code>).
     */
    public BigDecimal getBalance() {
        return negativeAmount.subtract(positiveAmount);
    }

    /** Checks, whether one of the important values in this
     * transaction has nonzero value. In particular, this method
     * checks <code>positiveAmount</code>,
     * <code>negativeAmount</code>, <code>amount</code>,
     * <code>receiverId</code> for zero and returns true, if one of
     * the above mentioned values is not zero. This is used in case of
     * switching the type of the transaction to determine, whether
     * user should be warned about possible change of values.
     *
     * @return <code>true</code>, if one of
     * <code>positiveAmount</code>, <code>negativeAmount</code>,
     * <code>amount</code>, or <code>receiverId</code> is not zero,
     * <code>false</code> otherwise.
     */
    public boolean hasNonzeroValues() {
        return (positiveAmount.signum() != 0
                || negativeAmount.signum() != 0
                || amount.signum() != 0
                || receiverId >= 0);
    }

	/** Returns the currency of this transaction.
	 * @return Currency of this transaction.
	 */
	public Currency getCurrency() {
		return currency;
	}

	/** Sets currency of this transaction.
	 * @param currency New currency of this transaction.
	 */
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	/** Returns receivers id. In case of split evenly transaction this
     * the <code>receiverId</code> contains the id of receiver of the
     * money. In case of a general transaction, this value has no
     * impact. For details see the description of this class and
     * description of <code>receiverId</code> member variable.
     *
	 * @return Value of <code>receiverId</code>.
	 */
	public long getReceiverId() {
		return receiverId;
	}

    /** Sets id of receiver in this transaction. In case of split
     * evenly transaction this the <code>receiverId</code> contains
     * the id of receiver of the money. In case of a general
     * transaction, this value has no impact. For details see the
     * description of this class and description of
     * <code>receiverId</code> member variable. Note, that this method
     * does not call <code>recomputeAndSaveChanges</code>, it should
     * be called explicitely after this change.
     *
	 * @param anId New value of <code>receiverId</code>.
	 */
	public void setReceiverId(long anId) {
		receiverId = anId;
	}

	/** Returns name of this transaction.
	 * @return Value of <code>name</code> member variable.
	 */
	public String getName() {
		return name;
	}

	/** Sets the name of this transaction.
	 * @param name New value of <code>name</code> member variable.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** Returns note associated with this transaction.
	 * @return Value of <code>note</code> member variable.
	 */
	public String getNote() {
		return note;
	}

	/** Sets note associated with this transaction.
	 * @param note New value of <code>note</code> member variable.
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/** Returns the value of <code>splitEvenly</code> flag.
     *
	 * @return Value of <code>splitEvenly</code> member variable.
	 */
	public boolean getSplitEvenly() {
		return splitEvenly;
	}

    /** Sets the split evenly flag. Note, that when changing the type
     * of transaction, the values of participants and amount may
     * change, this is because their meaning in each kind of
     * transaction is different. For details see the class description
     * and description of <code>splitEvenly</code> member variable.
     *
     * @param split New value of <code>splitEvenly</code> flag.
     */
	public void setSplitEvenly(boolean split) {
		splitEvenly = split;
    }

	/** Returns id of this transaction.
	 * @return Value of the <code>id</code> member variable.
	 */
	public long getId() {
		return id;
	}

	/** Returns event id of this transaction. I.e. this method returns
     * the id of event this transaction belongs to.
	 * @return Value of the <code>eventId</code> member variable.
	 */
	public long getEventId() {
		return eventId;
	}

	/**
	 * Sets value of participant with given id in this transaction.
     *
	 * @param participantId Id of participant whose value is about to
     * change.
	 * @param aValue New value of participant in this transaction.
	 */
	public void setParticipantValue(long participantId, BigDecimal aValue) {
		Long participantIdObject = Long.valueOf(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
        if (info != null && aValue.compareTo(info.getValue()) == 0) {
            // No change.
            return;
        }
        if (info == null) {
            info = new ParticipantInfo(participantId, aValue,
                    aValue.signum() != 0);
            participantsInfo.put(participantIdObject, info);
        } else {
            if (info.getValue().signum() > 0) {
                positiveAmount = positiveAmount.subtract(info.getValue());
            } else {
                negativeAmount = negativeAmount.add(info.getValue());
            }
            info.setValue(aValue);
            info.setMarked(aValue.signum() != 0);
        }
        if (splitEvenly) {
            splitEvenly = false;
        }
        if (aValue.signum() > 0) {
            positiveAmount = positiveAmount.add(aValue);
        } else {
            negativeAmount = negativeAmount.subtract(aValue);
        }
        if (positiveAmount.signum() < 0) {
            positiveAmount = BigDecimal.ZERO;
        }
        if (negativeAmount.signum() < 0) {
            negativeAmount = BigDecimal.ZERO;
        }
        if (amount.compareTo(positiveAmount) != 0) {
            amount = positiveAmount;
        }
	}

	/**
     * Sets mark of participant with given id. If participant with
     * given id does not exist yet, it is inserted with value 0 only
     * in case aMark is true.
	 * @param participantId Id of participant whose mark should be
     * set.
	 * @param aMark New status of participant's <code>mark</code> flag.
	 */
	public void setParticipantMark(long participantId, boolean aMark) {
		Long participantIdObject = Long.valueOf(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			info.setMarked(aMark);
		} else {
			if (aMark) {
				info = new ParticipantInfo(participantId, BigDecimal.ZERO, true);
				participantsInfo.put(participantIdObject, info);
			}
		}
	}

	/** Returns the value of participant 
	 * @param participantId
	 * @return
	 */
	public BigDecimal getParticipantValue(long participantId) {
		Long participantIdObject = Long.valueOf(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			return info.getValue();
		}
		return BigDecimal.ZERO;
	}

    /** Returns the status of participant's mark flag.
     * @param participantId Id of participant whose mark status should returned.
     * @return The status of <code>mark</code> flag of participant
     * with <code>id</code> equal to <code>participantId</code>.
     */
	public boolean isParticipantMarked(long participantId) {
		Long participantIdObject = Long.valueOf(participantId);
		ParticipantInfo info = participantsInfo.get(participantIdObject);
		if (info != null) {
			return info.isMarked();
		}
		return false;
	}

	/** Adds new participant to this transaction with given value.
	 * @param participantId Id of participant to be added.
	 * @param aValue Value of participant in this transaction.
     * @param aMark Mark status of given participant in this
     * transaction. Value is updated only if this parameter is
     * <code>true</code>.
	 */
	public void addParticipant(long participantId, BigDecimal aValue, boolean aMark) {
		ParticipantInfo info = new ParticipantInfo(participantId, aValue, aMark);
        if (aMark) {
            if (aValue.signum() > 0) {
                positiveAmount = positiveAmount.add(aValue);
            }
            else {
                negativeAmount = negativeAmount.subtract(aValue);
            }
        }
        participantsInfo.put(Long.valueOf(participantId), info);
	}

    /** Recomputes the values to be consistent and stores changes to
     * given database. To decrease the number of unneccessary database
     * updates, this method takes as a parameter database object and
     * uses its method to store changes into it. Only modified values
     * are updated. What is done by this function depends on the type
     * of transaction.
     *
     * In case of <code>splitEvenly</code> being <code>true</code>,
     * i.e. in case of split evenly type transaction, the values are
     * recomputed as follows. The amount of transaction is divided by
     * the number of marked participants. This fraction is then
     * rounded down to the number of significant fraction digits for
     * the transaction currency (using
     * <code>java.util.Currency.getDefaultFractionDigits()</code>),
     * this is actualy used already in the division as the limit of
     * significant fraction values. If some value remains, it is split
     * among several participants. So that the sum of participant
     * values is equal to the transaction amount. Simple division
     * would not work in cases as 1/3, because for these values we
     * would need infinite number of fraction values. Then each marked
     * participant is assigned its value, each unmarked participant is
     * assigned value of zero. In case of participant being also
     * receiver, the transaction <code>amount</code> is added to its
     * value. This is done even if the receiver itself is not marked.
     * In any case, if any participant gets different value from the
     * previous one, it is saved in the database. No database update
     * is performed if the participant has the same value as before.
     * Also <code>amount</code> is not updated, it is assumed, that it
     * has been set before calling this function. The values
     * <code>negativeAmount</code> and <code>positiveAmount</code> are
     * modified properly, too.
     *
     * In case of <code>splitEvenly</code> being <code>false</code>,
     * i.e. in case of a general transaction, the values of
     * participants are unchanged, what is possibly changed are their
     * marks and the <code>amount</code> of transaction. The status of
     * <code>mark</code> flag of each participant is set to
     * <code>true</code> if and only if its value is not zero in the
     * transaction. Value of <code>positiveAmount</code> is computed
     * as sum of positive values of participants. Value of
     * <code>negativeAmount</code> is computed as an absolute value of
     * sum of negative values of participants. Value of
     * <code>amount</code> is set to the value of
     * <code>positiveAmount</code>.
     *
     * As the output this method returns unbalance of the transaction
     * after recomputation, which is the same value as
     * <code>getBalance</code> method would return, i.e.
     * <code>negativeAmount</code>-<code>positiveAmount</code>
     *
     * @param db Database into which changes to values should be
     * saved. If this parameter is null, then the recomputation
     * proceeds, but changes are not saved into a database.
     * @return The difference of <code>negativeAmount</code> and
     * <code>positiveAmount</code> as <code>getBalance()</code>
     * method.
     */
    public BigDecimal recomputeAndSaveChanges(GCDatabase db) {
        if (splitEvenly) {
            positiveAmount = BigDecimal.ZERO;
            negativeAmount = BigDecimal.ZERO;
            BigDecimal negAmount = BigDecimal.ZERO;
            BigDecimal share = BigDecimal.ZERO;
            BigDecimal shiftedOne = 
                BigDecimal.ONE.movePointLeft(currency.getDefaultFractionDigits());
            long numberOfMarkedParticipants = 0;
            for (ParticipantInfo info : participantsInfo.values()) {
                if (info.isMarked()) {
                    ++ numberOfMarkedParticipants;
                }
            }
			if (numberOfMarkedParticipants > 0) {
                BigDecimal markedNumber = new BigDecimal(numberOfMarkedParticipants);
				share = amount.divide(markedNumber,
                        currency.getDefaultFractionDigits(), BigDecimal.ROUND_DOWN);
                negAmount = share.multiply(markedNumber);
            }
            for (ParticipantInfo info : participantsInfo.values()) {
                BigDecimal value = BigDecimal.ZERO;
                if (info.isMarked()) {
                    value = share.negate();
                    if (negAmount.compareTo(amount) < 0) {
                        value = value.subtract(shiftedOne);
                        negAmount = negAmount.add(shiftedOne);
                    }
                }
                if (info.getId() == receiverId) {
                    value = value.add(amount);
                }
                if (value.signum() > 0) {
                    positiveAmount = positiveAmount.add(value);
                } else {
                    negativeAmount = negativeAmount.subtract(value);
                }
                if (info.getValue().compareTo(value) != 0) {
                    info.setValue(value);
                    if (db != null) {
                        db.updateTransactionParticipantValue(this, info.getId());
                    }
                }
            }
        } else {
            // Go through all participants and unmark those with 0 value.
           positiveAmount = BigDecimal.ZERO;
           negativeAmount = BigDecimal.ZERO;
                for (ParticipantInfo info : participantsInfo.values()) {
                    boolean oldMark = info.isMarked();
                    info.setMarked (info.getValue().signum() != 0);
                    if (oldMark != info.isMarked()) {
                        if (db != null) {
                            db.updateTransactionParticipantValue(this, info.getId());
                        }
                    }
                    if (info.getValue().signum() > 0) {
                        positiveAmount = positiveAmount.add(info.getValue());
                    }
                    else {
                        negativeAmount = negativeAmount.subtract(info.getValue());
                    }
                }
            if (amount.compareTo(positiveAmount) != 0) {
                amount = positiveAmount;
                if (db != null) {
                    db.updateTransactionAmount(this);
                }
            }
        }
        return negativeAmount.subtract(positiveAmount);
    }
  
    /** Sets the rate of the transaction currency to the event
     * currency.
     *
     * @param rate New rate value.
     */
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    /** Returns the rate of the transaction currency to the event
     * currency.
     * @return The rate of the transaction currency to the event
     * currency.
     */
    public BigDecimal getRate() {
        return rate;
    }

    /** Resets the values when transaction type is changed. The values
     * of participants, their marks, and all three transaction amounts
     * are set to defaults. Other values are left unchanged, in
     * particular the <code>splitEvenly</code> flag.
     */
    public void resetValues() {
        for (ParticipantInfo info : participantsInfo.values()) {
            info.setValue(BigDecimal.ZERO);
            info.setMarked(splitEvenly);
        }
        positiveAmount = negativeAmount = amount = BigDecimal.ZERO;
        receiverId = -1;
    }

    /** Returns the string representation of this object.
     *
     * @return String representation of this object.
     */
	@Override
	public String toString() {
		return "ClearingTransaction [id=" + id + ", eventId=" + eventId
				+ ", calendarDate=" + calendarDate + ", amount=" + amount
				+ ", positiveAmount=" + positiveAmount + ", negativeAmount="
				+ negativeAmount + ", currency=" + currency + ", participants="
				+ participantsInfo + ", note=" + note + "]";
	}
}
