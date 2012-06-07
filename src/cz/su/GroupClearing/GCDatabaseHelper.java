package cz.su.GroupClearing;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GCDatabaseHelper extends SQLiteOpenHelper
{

   public static final int DATABASE_VERSION = 1;

   public static final String DATABASE_NAME = "cz.su.GroupClearing.db";

   public static final String TABLE_EVENTS = "gc_events";
   public static final String TABLE_PERSONS = "gc_persons";
   public static final String TABLE_TRANSACTIONS = "gc_transactions";
   public static final String TABLE_TRANSACTION_PARTICIPANTS = "gc_trans_participants";

   public static final String TE_ID = "id";
   public static final String TE_NAME = "name";
   public static final String TE_NOTE = "note";
   public static final String TE_START_DATE = "start_date";
   public static final String TE_FINISH_DATE = "finish_date";
   public static final String TE_CURRENCY = "currency";
   public static final String[] TABLE_EVENTS_COLUMNS = {
      TE_ID, TE_NAME, TE_NOTE, TE_START_DATE, TE_FINISH_DATE, TE_CURRENCY
   };

   public static final String TP_ID = "id";
   public static final String TP_EVENT_ID = "event_id";
   public static final String TP_NAME = "name";
   public static final String TP_NOTE = "note";
   public static final String[] TABLE_PERSONS_COLUMNS = {
      TP_ID, TP_EVENT_ID, TP_NAME, TP_NOTE
   };

   public static final String TT_ID = "id";
   public static final String TT_EVENT_ID = "event_id";
   public static final String TT_NAME = "name";
   public static final String TT_DATE = "date";
   public static final String TT_CURRENCY = "currency";
   public static final String TT_AMOUNT = "amount";
   public static final String TT_RECEIVER_ID = "receiver_id";
   public static final String TT_SPLIT_EVENLY = "split_evenly";
   public static final String TT_NOTE = "note";
   public static final String[] TABLE_TRANSACTIONS_COLUMNS = {
      TT_ID, TT_EVENT_ID, TT_NAME, TT_DATE, TT_CURRENCY,
      TT_AMOUNT, TT_RECEIVER_ID, TT_SPLIT_EVENLY, TT_NOTE
   };

   public static final String TTP_ID = "id";
   public static final String TTP_EVENT_ID = "event_id";
   public static final String TTP_TRANSACTION_ID = "transaction_id";
   public static final String TTP_PARTICIPANT_ID = "participant_id";
   public static final String TTP_VALUE = "value";

   public static final String[] TABLE_TTP_COLUMNS = {
      TTP_ID, TTP_EVENT_ID, TTP_TRANSACTION_ID, TTP_PARTICIPANT_ID, TTP_VALUE
   };

   public GCDatabaseHelper(Context context)
   {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }

   @Override
   public void onCreate(SQLiteDatabase db)
   {
      db.execSQL("CREATE TABLE " + TABLE_EVENTS + " ("
            + TE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TE_NAME + " TEXT, "
            + TE_NOTE + " TEXT, "
            + TE_START_DATE + " TEXT, "
            + TE_FINISH_DATE + " TEXT, "
            + TE_CURRENCY + " TEXT);");

      db.execSQL("CREATE TABLE " + TABLE_PERSONS + " (" 
            + TP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TP_EVENT_ID + " INTEGER, "
            + TP_NAME + " TEXT, "
            + TP_NOTE + " TEXT);");

      db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " ("
            + TT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TT_EVENT_ID + " INTEGER, "
            + TT_NAME + " TEXT, "
            + TT_DATE + " TEXT, "
            + TT_CURRENCY + " TEXT, "
            + TT_AMOUNT + " INTEGER, "
            + TT_RECEIVER_ID + " INTEGER, "
            + TT_SPLIT_EVENLY + " INTEGER, "
            + TT_NOTE + " TEXT);");

      db.execSQL("CREATE TABLE " + TABLE_TRANSACTION_PARTICIPANTS + " ("
            + TTP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TTP_EVENT_ID + " INTEGER, "
            + TTP_TRANSACTION_ID + " INTEGER, "
            + TTP_PARTICIPANT_ID + " INTEGER, "
            + TTP_VALUE + " INTEGER, "
            + "UNIQUE(" + TTP_EVENT_ID + ", " 
            + TTP_TRANSACTION_ID + ", " + TTP_PARTICIPANT_ID + ")"
            + ");");
   }

   @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {
         android.util.Log.w("GroupClearing",
               "Database scheme update, old data deleted");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
      //   db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_PARTICIPANTS);
         onCreate(db);
      }

}
