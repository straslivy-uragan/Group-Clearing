package cz.su.GroupClearing;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** The helper class for opening, creating and upgrading the database.
 *
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class GCDatabaseHelper extends SQLiteOpenHelper {

	/** Version of the database. When this value changes, in the next
     * invokation of the program, database is upgraded.
	 */
	public static final int DATABASE_VERSION = 1;

	/** Name of the database in SQLite.
	 */
	public static final String DATABASE_NAME = "cz.su.GroupClearing.db";

	/** Name of the database table with events.
	 */
	public static final String TABLE_EVENTS = "gc_events";
	/** Name of the database table with persons.
	 */
	public static final String TABLE_PERSONS = "gc_persons";
	/** Name of the database table with transactions.
	 */
	public static final String TABLE_TRANSACTIONS = "gc_transactions";
	/** Name of the database table with participants of transactions.
	 */
	public static final String TABLE_TRANSACTION_PARTICIPANTS = "gc_trans_participants";
	/** Name of the database with default currency rates.
	 * 
	 */
	public static final String TABLE_RATES = "gc_rates";

    /** Enum containing the indices of columns in
     * <code>TABLE_EVENTS</code> database table.
	 * @author Strašlivý Uragán
     * @version 1.0
	 * @since 1.0
	 */
	public enum TEColumns {
		/** Column with the id of the event.
		 */
		id,
        /** Column with the name of the event.
		 */
		name,
        /** Column with the note associated with the event.
		 */
		note,
        /** Column with the start date of the event.
		 */
		start_date,
        /** Column with the finish date of the event.
		 */
		finish_date,
        /** Column with the default currency of the event.
		 */
		currency
	}

    /** String array with the names of columns in
     * <code>TABLE_EVENTS</code> database table.
     * This array is indexed by constants from <code>TEColumns</code>
     * enum and in fact the names of the columns are the same as the
     * names of the corresponding constants and are acquired using
     * their <code>name()</code> function.
	 */
	public static final String[] TABLE_EVENTS_COLUMNS = {TEColumns.id.name(),
			TEColumns.name.name(), TEColumns.note.name(),
			TEColumns.start_date.name(), TEColumns.finish_date.name(),
			TEColumns.currency.name()};

    /** Enum containing the indices of columns in
     * <code>TABLE_PERSONS</code> database table.
	 * @author Strašlivý Uragán
     * @version 1.0
	 * @since 1.0
	 */
	public enum TPColumns {
		/** Column with the id of the person.
		 */
		id,
        /** Column with the id of event person belongs to.
		 */
		event_id,
        /** Column with the name of the person.
		 */
		name,
        /** Column with the note associated with the person.
		 */
		note
	}

    /** String array with the names of columns in
     * <code>TABLE_PERSONS</code> database table.
     * This array is indexed by constants from <code>TPColumns</code>
     * enum and in fact the names of the columns are the same as the
     * names of the corresponding constants and are acquired using
     * their <code>name()</code> function.
	 */
	public static final String[] TABLE_PERSONS_COLUMNS = {TPColumns.id.name(),
			TPColumns.event_id.name(), TPColumns.name.name(),
			TPColumns.note.name()};

	 /** Enum containing the indices of columns in
     * <code>TABLE_TRANSACTIONS</code> database table.
	 * @author Strašlivý Uragán
     * @version 1.0
	 * @since 1.0
	 */
	public enum TTColumns {
		/** Column with the id of the transaction.
		 */
		id,
        /** Column with the id of the event the transaction belongs
         * to.
		 */
		event_id,
        /** Column with the name of the transaction.
		 */
		name,
        /** Column with the date of the transaction.
		 */
		date,
        /** Column with the currency of the transaction.
		 */
		currency,
        /** Column with the conversion rate of the currency of the transaction to
         * the default currency of the event.
		 */
		rate,
        /** Column with the amount or value of the transaction.
		 */
		amount,
        /** Column with the id of the receiver in the transaction.
		 */
		receiver_id,
        /** Column with status of split evenly flag of the
         * transaction.
		 */
		split_evenly,
        /** Column with the note associated with the transaction.
		 */
		note
	}

	/** String array with the names of columns in
     * <code>TABLE_TRANSACTIONS</code> database table.
     * This array is indexed by constants from <code>TTColumns</code>
     * enum and in fact the names of the columns are the same as the
     * names of the corresponding constants and are acquired using
     * their <code>name()</code> function.
	 */public static final String[] TABLE_TRANSACTIONS_COLUMNS = {
			TTColumns.id.name(), TTColumns.event_id.name(),
			TTColumns.name.name(), TTColumns.date.name(),
			TTColumns.currency.name(), TTColumns.rate.name(),
			TTColumns.amount.name(), TTColumns.receiver_id.name(),
			TTColumns.split_evenly.name(), TTColumns.note.name()};

     /** Enum containing the indices of columns in
     * <code>TABLE_TRANSACTION_PARTICIPANTS</code> database table.
	 * @author Strašlivý Uragán
     * @version 1.0
	 * @since 1.0
	 */
	public enum TTPColumns {
		/** Column with the row id.
		 */
		id,
        /** Column with the id of the event. Note, that this value is
         * here only for convenience as it could be obtained from
         * table with transactions or persons.
		 */
		event_id, 
        /** Column with the id of the transaction.
		 */
		transaction_id,
        /** Column with the id of person whose value is stored in the same
         * row.
		 */
		participant_id,
        /** Column with the value of the person within the
         * transaction.
		 */
		value,
        /** Column with the status of the flag determining whether
         * the person is marked in the transaction.
		 */
		marked
	}

    /** String array with the names of columns in
     * <code>TABLE_TRANSACTION_PARTICIPANTS</code> database table.
     * This array is indexed by constants from <code>TTPColumns</code>
     * enum and in fact the names of the columns are the same as the
     * names of the corresponding constants and are acquired using
     * their <code>name()</code> function.
	 */
	public static final String[] TABLE_TTP_COLUMNS = {TTPColumns.id.name(),
			TTPColumns.event_id.name(), TTPColumns.transaction_id.name(),
			TTPColumns.participant_id.name(), TTPColumns.value.name(),
			TTPColumns.marked.name()};

	/** Enum containing the indices of columns in
     * <code>TABLE_RATES</code> database table.
	 * @author Strašlivý Uragán
     * @version 1.0
	 * @since 1.0
	 */
    public enum TRColumns {
		/** Name of the left currency of the rate.
		 */
		left_currency,
        /** Name of the right currency of the rate.
		 */
		right_currency,
        /** The rate between the left and the right currency. 1 unit
         * of the left currency is rate units of the right currency.
         * Rate is stored as string and it represents
         * <code>BigDecimal</code> value of the rate.
		 */
		rate
	}
	/** String array with the names of columns in
     * <code>TABLE_RATES</code> database table.
     * This array is indexed by constants from <code>TRColumns</code>
     * enum and in fact the names of the columns are the same as the
     * names of the corresponding constants and are acquired using
     * their <code>name()</code> function.
	 */
    public static final String[] TABLE_RATES_COLUMNS = {
			TRColumns.left_currency.name(), TRColumns.right_currency.name(),
			TRColumns.rate.name()};

	/** Constructs new object of <code>GCDatabaseHelper class</code>.
	 * @param context Context in which this helper class was created.
	 */
	public GCDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

    /** This method creates the database. The database is created by calling
     * <code>execSQL</code> method with <code>CREATE TABLE</code>
     * commands on db provided in the parameter.
     *
     * @param db Database in which the tables should be created.
     *
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
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

	/** This method is called when database should be updated.
     * The version numbers <code>oldVersion</code> and
     * <code>newVersion</code> determine from which version of
     * database schem to which the database should be upgraded. At the
     * time, however, these parameters are ignored. The database is
     * dropped and newly recreated.
     *
     * @param db Database in which the tables should be created.
     * @param oldVersion old version of the database.
     * @param newVersion new version of the database.
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
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
