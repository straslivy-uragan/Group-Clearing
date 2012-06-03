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
public class ClearingTransaction
{

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
    * This is a convenience value for checking whether payments are equal to the
    * total value of transaction.
    */
   private long negativeAmount;
   /**
    * Sum of positive amounts of participants.
    * 
    * This is a convenience value for checking whether payments are equal to the
    * total value of transaction.
    */
   private long positiveAmount;
   private String name;
   private GregorianCalendar calendarDate;
   private Currency currency;
   private HashMap<Long, Integer> participantsValues;
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
    *        because in general it is the person who is now receiving money from
    *        others as transaction needs not to be connected to an actual
    *        payment to outsiders.
    * 
    *        Note, that in case this value is -1, no explicit receiver is set.
    *        It does not necessarily mean that the transaction is unbalanced,
    *        because in participantsValues the values of participants can be
    *        both positive and negative. Which means that transaction can be
    *        general in this sense. In case of general transaction you should
    *        keep amount zero while the actual amounts are then positiveAmount
    *        and negativeAmount, the former with sum of positive values and the
    *        latter with the sum of negative values in participantsValues hash
    *        map.
    */
   private long receiverId = -1;

   public ClearingTransaction(long anId, long anEventId)
   {
      super();
      id = anId;
      eventId = anEventId;
      calendarDate = new GregorianCalendar();
      currency = Currency.getInstance(Locale.getDefault());
      amount = 0;
      positiveAmount = 0;
      negativeAmount = 0;
      participantsValues = new HashMap<Long, Integer>();
      note = "";
      receiverId = -1;
      splitEvenly = true;
   }

   public Date getDate()
   {
      return calendarDate.getTime();
   }

   public void setDate(int year, int month, int day)
   {
      calendarDate.set(Calendar.YEAR, year);
      calendarDate.set(Calendar.MONTH, month);
      calendarDate.set(Calendar.DAY_OF_MONTH, day);
   }

   public void setDate(Date aDate)
   {
      calendarDate.setTime(aDate);
   }

   public int getYear()
   {
      return calendarDate.get(Calendar.YEAR);
   }

   public int getMonth()
   {
      return calendarDate.get(Calendar.MONTH);
   }

   public int getDayOfMonth()
   {
      return calendarDate.get(Calendar.DAY_OF_MONTH);
   }

   public void setAmount(long newAmount)
   {
      amount = newAmount;
   }

   public long getAmount()
   {
      return amount;
   }

   public long getPositiveAmount()
   {
      return positiveAmount;
   }

   public long getNegativeAmount()
   {
      return negativeAmount;
   }

   public Currency getCurrency()
   {
      return currency;
   }

   public void setCurrency(Currency currency)
   {
      this.currency = currency;
   }

   public long getReceiverId()
   {
      return receiverId;
   }

   public void setReceiverId(long anId)
   {
      receiverId = anId;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getNote()
   {
      return note;
   }

   public void setNote(String note)
   {
      this.note = note;
   }

   public boolean getSplitEvenly()
   {
      return splitEvenly;
   }

   public void setSplitEvenly(boolean split)
   {
      splitEvenly = split;
   }

   public long getId()
   {
      return id;
   }

   public long getEventId()
   {
      return eventId;
   }

   public int numberOfParticipants()
   {
      return participantsValues.size();
   }

   public void putParticipantValue(long participantId, int aValue)
   {
      Long participantIdObject = new Long(participantId);
      Integer oldValueObject = participantsValues.get(participantIdObject);
      if (oldValueObject != null)
      {
         if (oldValueObject.intValue() > 0)
         {
            positiveAmount -= oldValueObject.intValue();
         }
         else
         {
            negativeAmount -= oldValueObject.intValue();
         }
      }
      if (aValue > 0)
      {
         positiveAmount += aValue;
      }
      else
      {
         negativeAmount += aValue;
      }
      participantsValues.put(participantIdObject, new Integer(aValue));
   }

   public void removeParticipantId(long participantId)
   {
      Integer valueObject = participantsValues.remove(new Long(participantId));
      if (valueObject != null)
      {
         if (valueObject.intValue() > 0)
         {
            positiveAmount -= valueObject.intValue();
         }
         else
         {
            negativeAmount -= valueObject.intValue();
         }
      }
   }

   public boolean containsParticipantId(long participantId)
   {
      return participantsValues.containsKey(new Long(participantId));
   }

   @Override
   public String toString()
   {
      return "ClearingTransaction [id=" + id + ", eventId=" + eventId
            + ", calendarDate=" + calendarDate + ", amount=" + amount
            + ", positiveAmount=" + positiveAmount + ", negativeAmount="
            + negativeAmount + ", currency=" + currency + ", participants="
            + participantsValues + ", note=" + note + "]";
   }
}
