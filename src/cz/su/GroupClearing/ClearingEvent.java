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
 * Class describing a clearing event. An event is basically a time
 * spanned event with start and (possible) finish, it contains usually
 * several payments which are split between several persons. 
 * This class contains basic properties of an event, while its
 * participants and transactions are stored separately. This
 * corresponds to the database scheme of storing events, participants
 * and transactions.
 *
 * Note, that this object does represent event information in memory,
 * but it does not directly communicate with database to initialize
 * itself or to save modified values there. You can use methods of
 * <code>GCDatabase</code> for that.
 *
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class ClearingEvent
{
    /** Id number of this event. */
   private long id;

   /** Start date of the event stored as an instance of
    * GregorianCalendar.
    */
   private GregorianCalendar startCalendar;

   /** Finish date of the event stored as an instance of
    * GregorianCalendar. 
    */
   private GregorianCalendar finishCalendar;
   /** Name of this event.
    */
   private String name;
   /** Note associated with this event.
    */
   private String note;
   /** Default currency of this event. Also main event currency.
    */
   private Currency defaultCurrency;

   /** Constructor, which creates new empty event object with given
    * id. After this event's id is set to <code>id</code> given in
    * parameter, <code>reset</code> is called to initialize the rest
    * of values.
    *
    * @param id Id of the event to be created.
    */
   public ClearingEvent(long id)
   {
       super();
       this.id = id;
       reset();
   }

   /**
    * Initializes the event object with default values. The
    * <code>id</code> value is left untouched, both dates are set to
    * actual date, name and note are set to be empty, default currency
    * is initialized based on default locale.
    */
   public void reset()
   {
       startCalendar = new GregorianCalendar();
       finishCalendar = new GregorianCalendar();
       name = "";
       note = "";
       defaultCurrency = Currency.getInstance(Locale.getDefault());
   }

   /**
    * Returns the name of this event.
    * @return The name of this event.
    */
   public String getName()
   {
       return name;
   }

   /**
    * Sets the new name of this event.
    * @param newName New name of this event.
    */
   public void setName(String newName)
   {
       name = newName;
   }

   /** Returns the note associated with this event.
    *
    * @return The note associated with this event.
    */
   public String getNote()
   {
      return note;
   }

   /** Sets the note associated with this event.
    * @param newNote New note associated with this event.
    */
   public void setNote(String newNote)
   {
       note = newNote;
   }

   /** Returns the default or main currency of this event.
    *
    * @return The default or main currency of this event.
    */
   public Currency getDefaultCurrency()
   {
      return defaultCurrency;
   }

   /** Sets new default or main currency of this event.
    * @param newDefaultCurrency New default or main currency of this
    * event.
    */
   public void setDefaultCurrency(Currency newDefaultCurrency)
   {
       defaultCurrency = newDefaultCurrency;
   }

   /** Returns id number of this event.
    * @return Id number of this event.
    */
   public long getId()
   {
      return id;
   }

   /** Sets new id number of this event.
    * @param newId New id number of this event.
    */
   public void setId(int newId)
   {
      id = newId;
   }

   /** Allows set new start date of this event. The new start date is
    * given as a triple <code>year</code>, <code>month</code>,
    * <code>day</code>.
    *
    * @param year New year part of the start date of this event.
    * @param month New month part of the start date of this event.
    * @param day New day part of the start date of this event.
    */
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

   /** Allows to set new start date of this event. This version admits
    * the new date given as a value of <code>Date</code> class.
    *
    * @param newStartDate New start date of this event.
    */
   public void setStartDate(Date newStartDate)
   {
      startCalendar.setTime(newStartDate);
   }
   
   /** Allows set new finish date of this event. The new finish date is
    * given as a triple <code>year</code>, <code>month</code>,
    * <code>day</code>.
    *
    * @param year New year part of the finish date of this event.
    * @param month New month part of the finish date of this event.
    * @param day New day part of the finish date of this event.
    */
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
   
   /** Allows to set new finish date of this event. This version admits
    * the new date given as a value of <code>Date</code> class.
    *
    * @param newFinishDate New finish date of this event.
    */
   public void setFinishDate(Date newFinishDate)
   {
      finishCalendar.setTime(newFinishDate);
   }

   /** Returns start date of this event.
    * @return <code>Date</code> object representing start date of this
    * event.
    */
   public Date getStartDate()
   {
      return startCalendar.getTime();
   }

   /** Returns finish date of this event.
    * @return <code>Date</code> object representing finish date of this
    * event.
    */
   public Date getFinishDate()
   {
      return finishCalendar.getTime();
   }

   /** Returns the year part of the start date of this event.
    * @return The year part of the start date of this event.
    */
   public int getStartYear()
   {
      return startCalendar.get(Calendar.YEAR);
   }

   /** Returns the month part of the start date of this event.
    *
    * @return The month part of the start date of this event.
    */
   public int getStartMonth()
   {
      return startCalendar.get(Calendar.MONTH);
   }

   /** Returns the day part of the start date of this event.
    *
    * @return The day part of the start date of this event.
    */
   public int getStartDayOfMonth()
   {
      return startCalendar.get(Calendar.DAY_OF_MONTH);
   }

   /** Returns the year part of the start date of this event.
    *
    * @return The year part of the start date of this event.
    */
   public int getFinishYear()
   {
      return finishCalendar.get(Calendar.YEAR);
   }

   /** Returns the month part of the start date of this event.
    *
    * @return The month part of the start date of this event.
    */
   public int getFinishMonth()
   {
      return finishCalendar.get(Calendar.MONTH);
   }

   /** Returns the day part of the start date of this event.
    *
    * @return The day part of the start date of this event.
    */
   public int getFinishDayOfMonth()
   {
      return finishCalendar.get(Calendar.DAY_OF_MONTH);
   }

   /** Returns string description of this event. This method returns
    * just the name of this event.
    *
    * @return The string description of this event, i.e. its name.
    */
   @Override
      public String toString()
      {
         return name;
      }

}
