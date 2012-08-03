/**
 * Definition of class with clearing event.
 */
package cz.su.GroupClearing;

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * @author su
 */
/**
 * Class describing a clearing event. An event is basically a time spanned event
 * with start and (possible) finish, it contains usually several payments which
 * are split between several persons. All these properties are part of data
 * stored in an object of this class.
 */
public class ClearingEvent
{
   private long id;

   private GregorianCalendar startCalendar;
   private GregorianCalendar finishCalendar;
   private String name;
   private String note;
   private Currency defaultCurrency;

   public ClearingEvent(long id)
   {
      super();
      this.id = id;
      reset();
   }

   public void reset()
   {
      startCalendar = new GregorianCalendar();
      finishCalendar = new GregorianCalendar();
      name = "";
      note = "";
      defaultCurrency = Currency.getInstance(Locale.getDefault());
   }

   public String getName()
   {
      return name;
   }

   public void setName(String newName)
   {
      if (name == null || !name.equals(newName))
      {
         name = newName;
      }
   }

   public String getNote()
   {
      return note;
   }

   public void setNote(String newNote)
   {
      if (note == null || !note.equals(newNote))
      {
         note = newNote;
      }
   }

   public Currency getDefaultCurrency()
   {
      return defaultCurrency;
   }

   public void setDefaultCurrency(Currency newDefaultCurrency)
   {
       defaultCurrency = newDefaultCurrency;
   }

   public long getId()
   {
      return id;
   }

   public void setId(int newId)
   {
      id = newId;
   }

   public void setStartDate(int year, int month, int day)
   {
      if (year != getStartYear())
         startCalendar.set(Calendar.YEAR, year);
      {
      }
      if (month != getStartMonth())
      {
         startCalendar.set(Calendar.MONTH, month);
      }
      if (day != getStartDayOfMonth())
      {
         startCalendar.set(Calendar.DAY_OF_MONTH, day);
      }
   }

   public void setStartDate(Date newStartDate)
   {
      startCalendar.setTime(newStartDate);
   }

   public void setFinishDate(int year, int month, int day)
   {
      if (year != getFinishYear())
      {
         finishCalendar.set(Calendar.YEAR, year);
      }
      if (month != getFinishMonth())
      {
         finishCalendar.set(Calendar.MONTH, month);
      }
      if (day != getFinishDayOfMonth())
      {
         finishCalendar.set(Calendar.DAY_OF_MONTH, day);
      }
   }

   public void setFinishDate(Date newFinishDate)
   {
      finishCalendar.setTime(newFinishDate);
   }

   public Date getStartDate()
   {
      return startCalendar.getTime();
   }

   public Date getFinishDate()
   {
      return finishCalendar.getTime();
   }

   /**
    * @return the startYear
    */
   public int getStartYear()
   {
      return startCalendar.get(Calendar.YEAR);
   }

   /**
    * @return the startMonthOfYear
    */
   public int getStartMonth()
   {
      return startCalendar.get(Calendar.MONTH);
   }

   /**
    * @return the startDayOfMonth
    */
   public int getStartDayOfMonth()
   {
      return startCalendar.get(Calendar.DAY_OF_MONTH);
   }

   /**
    * @return the finishYear
    */
   public int getFinishYear()
   {
      return finishCalendar.get(Calendar.YEAR);
   }

   /**
    * @return the finishMonthOfYear
    */
   public int getFinishMonth()
   {
      return finishCalendar.get(Calendar.MONTH);
   }

   /**
    * @return the finishDayOfMonth
    */
   public int getFinishDayOfMonth()
   {
      return finishCalendar.get(Calendar.DAY_OF_MONTH);
   }

   @Override
      public String toString()
      {
         return name;
      }

}
