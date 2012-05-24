package cz.su.GroupClearing;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GCDatabase
{
   private SQLiteDatabase db;

   public GCDatabase(Context context)
   {
      db = (new GCDatabaseHelper(context)).getWritableDatabase();
   }

   public void close()
   {
      db.close();
      db = null;
   }

   public ClearingEvent createNewEvent()
   {
      ContentValues values = new ContentValues(5);
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      values.put(GCDatabaseHelper.TE_NAME, "");
      values.put(GCDatabaseHelper.TE_NOTE, "");
      values.put(GCDatabaseHelper.TE_START_DATE,
            df.format(new Date()));
      values.put(GCDatabaseHelper.TE_FINISH_DATE,
            df.format(new Date()));
      values.put(GCDatabaseHelper.TE_CURRENCY,
            Currency.getInstance(Locale.getDefault()).toString());
      long id = db.insert(GCDatabaseHelper.TABLE_EVENTS, null, values);
      if (id > 0)
      {
         return readEventWithId(id);
      }
      return null;
   }

   public ClearingEvent readEventWithId(long id)
   {
      String whereClause = String.format("%s=%d", GCDatabaseHelper.TE_ID, id);
      Cursor eventCursor = db.query(GCDatabaseHelper.TABLE_EVENTS,
            GCDatabaseHelper.TABLE_EVENTS_COLUMNS, whereClause, null,
            null, null, null);
      eventCursor.moveToFirst();
      if (eventCursor.isAfterLast())
      {
         return null;
      }
      SimpleDateFormat dateParser = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
      ClearingEvent event = new ClearingEvent(id);
      event.setName(eventCursor.getString(1));
      event.setNote(eventCursor.getString(2));
      Date startDate = dateParser.parse(eventCursor.getString(3),
            new ParsePosition(0));
      event.setStartDate(startDate);
      Date finishDate = dateParser.parse(eventCursor.getString(4),
            new ParsePosition(0));
      event.setFinishDate(finishDate);
      event.setDefaultCurrency(Currency.getInstance(eventCursor.getString(5)));
      return event;
   }

   public void deleteEventWithId(long id)
   {
      db.beginTransaction();
      try {
         // Delete transactions in the event.
         String whereClause = String.format("%s=%d",
               GCDatabaseHelper.TTP_EVENT_ID, id);
         db.delete(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
               whereClause, null);
         whereClause = String.format("%s=%d",
               GCDatabaseHelper.TT_EVENT_ID, id);
         db.delete(GCDatabaseHelper.TABLE_TRANSACTIONS, whereClause, null);
         // Delete participants of the event from gc_persons table.
         whereClause = String.format("%s=%d",
               GCDatabaseHelper.TP_EVENT_ID,
               id);
         db.delete(GCDatabaseHelper.TABLE_PERSONS, whereClause, null);

         // Delete the event itself
         whereClause = String.format("%s=%d",
               GCDatabaseHelper.TE_ID, id);
         db.delete(GCDatabaseHelper.TABLE_EVENTS, whereClause.toString(), null);
         db.setTransactionSuccessful();
      }
      finally {
         db.endTransaction();
      }
   }

   /**
    * @brief Saves given event to the database.
    * 
    *        This method assumes, that the corresponding event already exists
    *        and it saves only the main data, i.e. not the transactions and
    *        participants as those are stored in different tables and managed
    *        separately.
    * 
    * @param anEvent
    *           Event to be saved.
    */
   public void updateEvent(ClearingEvent anEvent)
   {
      ContentValues values = new ContentValues(5);
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      values.put(GCDatabaseHelper.TE_NAME, anEvent.getName());
      values.put(GCDatabaseHelper.TE_NOTE, anEvent.getNote());
      values.put(GCDatabaseHelper.TE_START_DATE,
            df.format(anEvent.getStartDate()));
      values.put(GCDatabaseHelper.TE_FINISH_DATE,
            df.format(anEvent.getFinishDate()));
      values.put(GCDatabaseHelper.TE_CURRENCY,
            anEvent.getDefaultCurrency().toString());
      StringBuilder whereClause = new StringBuilder();
      whereClause.append(GCDatabaseHelper.TE_ID);
      whereClause.append('=');
      whereClause.append(anEvent.getId());
      db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause.toString(),
               null);

   }

   public void updateEventName(ClearingEvent anEvent)
   {
      ContentValues values = new ContentValues(1);
      values.put(GCDatabaseHelper.TE_NAME, anEvent.getName());
      StringBuilder whereClause = new StringBuilder();
      whereClause.append(GCDatabaseHelper.TE_ID);
      whereClause.append('=');
      whereClause.append(anEvent.getId());
      db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause.toString(),
               null);
   }

   public void updateEventNote(ClearingEvent anEvent)
   {
      ContentValues values = new ContentValues(1);
      values.put(GCDatabaseHelper.TE_NOTE, anEvent.getNote());
      StringBuilder whereClause = new StringBuilder();
      whereClause.append(GCDatabaseHelper.TE_ID);
      whereClause.append('=');
      whereClause.append(anEvent.getId());
      db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause.toString(),
               null);
   }

   public void updateEventStartDate(ClearingEvent anEvent)
   {
      ContentValues values = new ContentValues(1);
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      values.put(GCDatabaseHelper.TE_START_DATE,
            df.format(anEvent.getStartDate()));
      StringBuilder whereClause = new StringBuilder();
      whereClause.append(GCDatabaseHelper.TE_ID);
      whereClause.append('=');
      whereClause.append(anEvent.getId());
      db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause.toString(),
            null);
   }

   public void updateEventFinishDate(ClearingEvent anEvent)
   {
      ContentValues values = new ContentValues(1);
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      values.put(GCDatabaseHelper.TE_FINISH_DATE,
            df.format(anEvent.getFinishDate()));
      StringBuilder whereClause = new StringBuilder();
      whereClause.append(GCDatabaseHelper.TE_ID);
      whereClause.append('=');
      whereClause.append(anEvent.getId());
      db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause.toString(),
            null);
   }

   public void updateEventCurrency(ClearingEvent anEvent)
   {
      ContentValues values = new ContentValues(1);
      values.put(GCDatabaseHelper.TE_CURRENCY,
            anEvent.getDefaultCurrency().toString());
      StringBuilder whereClause = new StringBuilder();
      whereClause.append(GCDatabaseHelper.TE_ID);
      whereClause.append('=');
      whereClause.append(anEvent.getId());
      db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause.toString(),
            null);
   }

   public ClearingPerson readPersonWithId(long id)
   {
      String whereClause = String.format("%s=%d", GCDatabaseHelper.TP_ID, id);
      Cursor personCursor = db.query(GCDatabaseHelper.TABLE_PERSONS,
            GCDatabaseHelper.TABLE_PERSONS_COLUMNS, whereClause, null,
            null, null, null);
      personCursor.moveToFirst();
      if (personCursor.isAfterLast())
      {
         return null;
      }
      ClearingPerson person = new ClearingPerson(id, personCursor.getString(2),
            computeBalanceOfPerson(personCursor.getInt(1), id),
            personCursor.getString(3));
      return person;
   }

   public int computeBalanceOfPerson(long eventId, long personId)
   {
      return 0;
   }

   public Vector<ClearingPerson> readParticipantsOfEvent(long eventId)
   {
      String whereClause = String.format("%s=%d",
            GCDatabaseHelper.TP_EVENT_ID, eventId);
      Cursor result = db.query(GCDatabaseHelper.TABLE_PERSONS,
            GCDatabaseHelper.TABLE_PERSONS_COLUMNS,
            whereClause, null,
            null, null, GCDatabaseHelper.TP_NAME);
      Vector<ClearingPerson> participants = new Vector<ClearingPerson>();
      result.moveToFirst();
      while (!result.isAfterLast())
      {
         ClearingPerson person = new ClearingPerson(result.getInt(0),
               result.getString(2), 
               computeBalanceOfPerson(eventId, result.getInt(0)),
               result.getString(3));
         participants.add(person);
         result.moveToNext();
      }
      return participants;
   }

   public void updateParticipantName(ClearingPerson participant)
   {
      ContentValues values = new ContentValues(1);
      values.put(GCDatabaseHelper.TP_NAME, participant.getName());
      String whereClause = String.format("%s=%d", GCDatabaseHelper.TP_ID,
            participant.getId());
      db.update(GCDatabaseHelper.TABLE_PERSONS, values, whereClause,
            null);
   }

   public void deleteParticipantWithId(long id)
   {
      String whereClause = String.format("%s=%d", GCDatabaseHelper.TP_ID, id);
      db.delete(GCDatabaseHelper.TABLE_PERSONS, whereClause, null);
   }

   public SQLiteDatabase getDB()
   {
      return db;
   }

   public ClearingPerson createNewParticipant(long eventId, String name)
   {
      ContentValues values = new ContentValues(4);
      values.put(GCDatabaseHelper.TP_EVENT_ID, eventId);
      values.put(GCDatabaseHelper.TP_NAME, name);
      values.put(GCDatabaseHelper.TP_NOTE, "");
      long id = db.insert(GCDatabaseHelper.TABLE_PERSONS, null, values);
      if (id > 0)
      {
         return readPersonWithId(id);
      }
      return null;
   }
   
   public Vector<ClearingTransaction> readTransactionsOfEvent(long eventId)
   {
      String whereClause = String.format("%s=%d",
            GCDatabaseHelper.TT_EVENT_ID, eventId);
      Cursor transactionsCursor = db.query(GCDatabaseHelper.TABLE_TRANSACTIONS,
            GCDatabaseHelper.TABLE_TRANSACTIONS_COLUMNS,
            whereClause, null,
            null, null, GCDatabaseHelper.TT_ID);
      whereClause = String.format("%s=%d",
            GCDatabaseHelper.TTP_EVENT_ID, eventId);
      Cursor participantsCursor = db.query(
            GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
            GCDatabaseHelper.TABLE_TTP_COLUMNS,
            whereClause, null,
            null, null, GCDatabaseHelper.TTP_TRANSACTION_ID);
      // TODO: Implement
      // Go through both cursors simultaneously.
      transactionsCursor.moveToFirst();
      participantsCursor.moveToFirst();
      while (!transactionsCursor.isAfterLast())
      {
         transactionsCursor.moveToNext();
      }
      return null;
   }

   public ClearingTransaction readTransactionWithId(long id)
   {
      return null;
   }

   public void deleteTransactionWithId(long id)
   {
      String whereClause = String.format("%s=%d",
            GCDatabaseHelper.TTP_TRANSACTION_ID, id);
      db.delete(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
            whereClause, null);
      whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID, id);
      db.delete(GCDatabaseHelper.TABLE_TRANSACTIONS, whereClause, null);
   }

   public ClearingTransaction createNewTransaction(long eventId)
   {
      // TODO: Implement
      return null;
   }
}
