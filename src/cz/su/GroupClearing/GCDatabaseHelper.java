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
	public static final String TABLE_TRANSACTION_PARTICIPANTS = "gc_trans_participants";
	public static final String TABLE_RATES = "gc_rates";

	public enum TEColumns {
		id, name, note, start_date, finish_date, currency
	}

	public static final String[] TABLE_EVENTS_COLUMNS = {TEColumns.id.name(),
			TEColumns.name.name(), TEColumns.note.name(),
			TEColumns.start_date.name(), TEColumns.finish_date.name(),
			TEColumns.currency.name()};

	public enum TPColumns {
		id, event_id, name, note
	}
	public static final String[] TABLE_PERSONS_COLUMNS = {TPColumns.id.name(),
			TPColumns.event_id.name(), TPColumns.name.name(),
			TPColumns.note.name()};

	public enum TTColumns {
        id, event_id, name, date, currency,
            rate, amount, receiver_id,
            split_evenly, note
	}

	public static final String[] TABLE_TRANSACTIONS_COLUMNS = {
			TTColumns.id.name(), TTColumns.event_id.name(),
			TTColumns.name.name(), TTColumns.date.name(),
			TTColumns.currency.name(), TTColumns.rate.name(),
			TTColumns.amount.name(), TTColumns.receiver_id.name(),
			TTColumns.split_evenly.name(), TTColumns.note.name()};

	public enum TTPColumns {
		id, event_id, transaction_id, participant_id, value, marked
	}
	public static final String[] TABLE_TTP_COLUMNS = {TTPColumns.id.name(),
			TTPColumns.event_id.name(), TTPColumns.transaction_id.name(),
			TTPColumns.participant_id.name(), TTPColumns.value.name(),
			TTPColumns.marked.name()};

	public enum TRColumns {
		left_currency, right_currency, rate
	}
	public static final String[] TABLE_RATES_COLUMNS = {
			TRColumns.left_currency.name(), TRColumns.right_currency.name(),
			TRColumns.rate.name()};

	public GCDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" + TEColumns.id.name()
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TEColumns.name.name() + " TEXT, " + TEColumns.note.name()
				+ " TEXT, " + TEColumns.start_date.name() + " TEXT, "
				+ TEColumns.finish_date.name() + " TEXT, "
				+ TEColumns.currency.name() + " TEXT);");

		db.execSQL("CREATE TABLE " + TABLE_PERSONS + " (" + TPColumns.id.name()
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TPColumns.event_id.name() + " INTEGER, "
				+ TPColumns.name.name() + " TEXT, " + TPColumns.note.name()
				+ " TEXT);");

		db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " ("
				+ TTColumns.id.name() + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TTColumns.event_id.name() + " INTEGER, "
				+ TTColumns.name.name() + " TEXT, " + TTColumns.date.name()
				+ " TEXT, " + TTColumns.currency.name() + " TEXT, "
				+ TTColumns.rate.name() + " TEXT, " + TTColumns.amount.name()
				+ " TEXT, " + TTColumns.receiver_id.name() + " INTEGER, "
				+ TTColumns.split_evenly.name() + " INTEGER, "
				+ TTColumns.note.name() + " TEXT);");

		db.execSQL("CREATE TABLE " + TABLE_TRANSACTION_PARTICIPANTS + " ("
				+ TTPColumns.id.name() + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TTPColumns.event_id.name() + " INTEGER, "
				+ TTPColumns.transaction_id.name() + " INTEGER, "
				+ TTPColumns.participant_id.name() + " INTEGER, "
				+ TTPColumns.value.name() + " TEXT, "
				+ TTPColumns.marked.name() + " INTEGER, " + "UNIQUE("
				+ TTPColumns.event_id.name() + ", "
				+ TTPColumns.transaction_id.name() + ", "
				+ TTPColumns.participant_id.name() + ")" + ");");

		db.execSQL("CREATE TABLE " + TABLE_RATES + " ("
				+ TRColumns.left_currency.name() + " TEXT, "
				+ TRColumns.right_currency.name() + " TEXT, "
				+ TRColumns.rate.name() + " TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("GroupClearing",
				"Database scheme update, old data deleted");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_PARTICIPANTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATES);
		onCreate(db);
	}

}
