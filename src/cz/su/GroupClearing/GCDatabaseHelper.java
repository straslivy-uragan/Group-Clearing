package cz.su.GroupClearing;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GCDatabaseHelper extends SQLiteOpenHelper {

   public static final int DATABASE_VERSION = 1;

	public static final String DATABASE_NAME = "cz.su.GroupClearing.db";
   
   public static final String TABLE_EVENTS = "gc_events";
   public static final String TABLE_PERSONS = "gc_persons";
   public static final String TABLE_TRANSACTIONS = "gc_transactions";
   public static final String TABLE_EVENT_PARTICIPANTS = "gc_event_participants";
   public static final String TABLE_TRANSACTION_PARTICIPANTS =
      "gc_transaction_participants";
   
   public static final String TE_ID = "id";
   public static final String TE_NAME = "name";
	public static final String TE_NOTE = "note";
   public static final String TE_START_DATE = "start_date";
   public static final String TE_FINISH_DATE = "finish_date";
   public static final String TE_CURRENCY = "currency";

   public static final String TP_ID = "id";
   public static final String TP_NAME = "name";
   public static final String TP_NOTE = "note";

   public static final String TT_ID = "id";
   public static final String TT_EVENT = "event_id";
   public static final String TT_NAME = "name";
   public static final String TT_DATE = "date";
   public static final String TT_CURRENCY = "currency";
   public static final String TT_RECEIVER = "receiver";
   public static final String TT_NOTE = "note";

   public static final String TEP_EVENT = "event_id";
   public static final String TEP_PARTICIPANT = "person_id";
   public static final String TEP_BALANCE = "balance";

   public static final String TTP_TRANSACTION = "transaction_id";
   public static final String TTP_PARTICIPANT = "person_id";
   public static final String TTP_SHARE_AMOUNT = "share_amount";

	public GCDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
      // db.execSQL("PRAGMA foreign_keys=ON;");
      db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
            TE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TE_NAME + " TEXT, " +
            TE_NOTE + " TEXT, " +
            TE_START_DATE + " TEXT, " +
            TE_FINISH_DATE + " TEXT, " +
            TE_CURRENCY + " TEXT);");

      db.execSQL("CREATE TABLE " + TABLE_PERSONS + " (" +
            TP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TP_NAME + " TEXT, " +
            TP_NOTE + " TEXT);");

      db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
            TT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TT_EVENT + " INTEGER, " +
            TT_NAME + " TEXT, " +
            TT_DATE + " TEXT, " +
            TT_CURRENCY + " TEXT, " +
            TT_RECEIVER + " INTEGER, " +
            TT_NOTE + " TEXT);");
      /* "FOREIGN KEY(" + TT_EVENT + ") REFERENCES " +
         TABLE_EVENTS + "(" + TE_ID + ") ON DELETE CASCADE" +
         ");");*/

      db.execSQL("CREATE TABLE " + TABLE_EVENT_PARTICIPANTS + " (" +
            TEP_EVENT + " INTEGER, " +
            TEP_PARTICIPANT + " INTEGER, " +
            TEP_BALANCE + " REAL);");
      /* "FOREIGN KEY(" + TEP_EVENT + ") REFERENCES " +
         TABLE_EVENTS + "(" + TE_ID + ") ON DELETE CASCADE, " +
         "FOREIGN KEY(" + TEP_PARTICIPANT + ") REFERENCES " +
         TABLE_PERSONS + "(" + TP_ID + ") DEFERRABLE INITIALLY DEFERRED" +
         ");");*/

      db.execSQL("CREATE TABLE " + TABLE_TRANSACTION_PARTICIPANTS + " (" +
            TTP_TRANSACTION + " INTEGER, " +
            TTP_PARTICIPANT + " INTEGER, " +
            TTP_SHARE_AMOUNT + " REAL);");
      /*"FOREIGN KEY(" + TTP_TRANSACTION + ") REFERENCES " +
        TABLE_TRANSACTIONS + "(" + TT_ID + ") ON DELETE CASCADE, " +
        "FOREIGN KEY(" + TTP_PARTICIPANT + ") REFERENCES " +
        TABLE_PERSONS + "(" + TP_ID + ") ON DELETE CASCADE" +
        ");");*/
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      android.util.Log.w("GroupClearing",
            "Database scheme update, old data deleted");
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONS);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_PARTICIPANTS);
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_PARTICIPANTS);
      onCreate(db);
	}

}
