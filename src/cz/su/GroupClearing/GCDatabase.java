package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Class representing the interface to database of Group Clearing application.
 * Object of this class maintains a connection to the SQLite database and it
 * contains various methods for accessing and updating data within the database.
 * Note, that this object does not serve as a helper object for creating and/or
 * opening the database. <code>GCDatabaseHelper</code> class serves this
 * purpose.
 * 
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 * @see cz.su.GroupClearing.GCDatabaseHelper
 */
public class GCDatabase {
	/** Object representing the SQLite database. */
	private SQLiteDatabase db;
	/** Object for formatting and parsing dates. */
	private final SimpleDateFormat dateFormat;

	/**
	 * Enum containing possible values of <code>computeBalance</code> parameter
	 * of <code>readParticipantsOfEvent(long,
	 * ComputeBalance)</code> method. This method reads participants of a given
	 * event, depending on this value the method either computes balance for
	 * each person or not, if it does compute balance, then it is possible to
	 * distinguish, whether balance should be computed in event currency (and
	 * values in other currencies should be converted to it), or if balances
	 * should be computed separately in every currency. Note, that
	 * <code>ClearingPerson</code> object can store either a single valued
	 * balance (using pair of methods <code>setBalance(BigDecimal)</code>/
	 * <code>getBalance()</code>, or it can store balances in separate
	 * currencies (using pair of methods
	 * <code>setAllBalances(SortedMap&lt;String,
	 * BigDecimal&gt;)</code>/<code>getAllBalances()</code>.
	 * 
	 * @author Strašlivý Uragán
	 * @version 1.0
	 * @since 1.0
	 * @see cz.su.GroupClearing.GCDatabase#readParticipantsOfEvent(long,ComputeBalance)
	 * @see cz.su.GroupClearing.ClearingPerson#setBalance(BigDecimal)
	 * @see cz.su.GroupClearing.ClearingPerson#setAllBalances(SortedMap<String,
	 *      BigDecimal>)
	 */
	public enum ComputeBalance {
		/**
		 * Balances of participants should not be computed at all.
		 */
		DO_NOT_COMPUTE,
		/**
		 * Only one cumulative balance should be computed with value in event
		 * currency. Other values are converted to event currency before adding
		 * them to the total balance.
		 */
		COMPUTE_CUMULATIVE,
		/**
		 * Compute separate balances for all different currencies used in
		 * transactions within the event.
		 */
		COMPUTE_ALL
	};

	/**
	 * Creates the database with given context. Context is used to open the
	 * database using <code>GCDatabaseHelper</code> object.
	 * 
	 * @param context
	 */
	public GCDatabase(Context context) {
		db = (new GCDatabaseHelper(context)).getWritableDatabase();
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * Closes the database.
	 */
	public void close() {
		db.close();
		db = null;
	}

	/**
	 * Creates new event, stores it in the database and returns it in a
	 * <code>ClearingEvent</code> object. The event is first created and put
	 * into the database, then it is retrieved back using
	 * <code>readEventWithId</code> and the read object is returned. This way it
	 * is ensured, that the event was successfully stored in the database and
	 * that the object with appropriate event id is returned.
	 * 
	 * @return The newly created <code>ClearingEvent</code> object with a newly
	 *         initialized event.
	 */
	public ClearingEvent createNewEvent() {
		ContentValues values = new ContentValues(5);
		values.put(GCDatabaseHelper.TEColumns.name.name(), "");
		values.put(GCDatabaseHelper.TEColumns.note.name(), "");
		values.put(GCDatabaseHelper.TEColumns.start_date.name(),
				dateFormat.format(new Date()));
		values.put(GCDatabaseHelper.TEColumns.finish_date.name(),
				dateFormat.format(new Date()));
		values.put(GCDatabaseHelper.TEColumns.currency.name(), Currency
				.getInstance(Locale.getDefault()).toString());
		long id = db.insert(GCDatabaseHelper.TABLE_EVENTS, null, values);
		if (id > 0) {
			return readEventWithId(id);
		}
		return null;
	}

	/**
	 * Translates given database row into an event. The row is supposed to
	 * contain all columns in order given at the time of creation of the table
	 * within <code>GCDatabaseHelper</code>.
	 * 
	 * @param row
	 *            Database row containing the event data.
	 * @return <code>ClearingEvent</code> object initialized with data from
	 *         <code>row</code> parameter. If <code>row</code> does not contain
	 *         any data, <code>null</code> is returned.
	 */
	public ClearingEvent eventFromRow(Cursor row) {
		if (row.isAfterLast()) {
			return null;
		}
		SimpleDateFormat dateParser = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		ClearingEvent event = new ClearingEvent(
				row.getLong(GCDatabaseHelper.TEColumns.id.ordinal()));
		event.setName(row.getString(GCDatabaseHelper.TEColumns.name.ordinal()));
		event.setNote(row.getString(GCDatabaseHelper.TEColumns.note.ordinal()));
		Date startDate = dateParser.parse(
				row.getString(GCDatabaseHelper.TEColumns.start_date.ordinal()),
				new ParsePosition(0));
		event.setStartDate(startDate);
		Date finishDate = dateParser
				.parse(row.getString(GCDatabaseHelper.TEColumns.finish_date
						.ordinal()), new ParsePosition(0));
		event.setFinishDate(finishDate);
		event.setDefaultCurrency(Currency.getInstance(row
				.getString(GCDatabaseHelper.TEColumns.currency.ordinal())));
		return event;
	}

	/**
	 * Reads event with given id from the database.
	 * 
	 * @param id
	 *            Id of event to be read from the database.
	 * @return Event with id given in <code>id</code> parameter as it was stored
	 *         within the database. If no event with given id is stored in the
	 *         database, <code>null</code> is returned.
	 */
	public ClearingEvent readEventWithId(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TEColumns.id.name(), id);
		Cursor eventCursor = db.query(GCDatabaseHelper.TABLE_EVENTS,
				GCDatabaseHelper.TABLE_EVENTS_COLUMNS, whereClause, null, null,
				null, null);
		eventCursor.moveToFirst();
		return eventFromRow(eventCursor);
	}

	/**
	 * Reads all events from the database and returns them in an array.
	 * 
	 * @return Array of <code>ClearingEvent</code> objects containing the events
	 *         stored in the database. If no event is stored in the database,
	 *         <code>null</code> is returned.
	 */
	public ClearingEvent[] readEvents() {
		Cursor eventCursor = db.query(GCDatabaseHelper.TABLE_EVENTS,
				GCDatabaseHelper.TABLE_EVENTS_COLUMNS, null, null, null, null,
				null);
		eventCursor.moveToFirst();
		if (eventCursor.isAfterLast()) {
			return null;
		}
		ClearingEvent[] events = new ClearingEvent[eventCursor.getCount()];
		int rowIndex = 0;
		while (!eventCursor.isAfterLast()) {
			events[rowIndex++] = eventFromRow(eventCursor);
			eventCursor.moveToNext();
		}
		return events;
	}

	/**
	 * Deletes event with given id from the database. This method deletes the
	 * transactions of the event, its participants and finally the event itself
	 * from the database. Note, that this deletion is irreversible.
	 * 
	 * @param id
	 *            <code>id</code> of the event to be deleted.
	 */
	public void deleteEventWithId(long id) {
		db.beginTransaction();
		try {
			// Delete transactions in the event.
			String whereClause = String.format("%s=%d",
					GCDatabaseHelper.TTPColumns.event_id.name(), id);
			db.delete(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
					whereClause, null);
			whereClause = String.format("%s=%d",
					GCDatabaseHelper.TTColumns.event_id.name(), id);
			db.delete(GCDatabaseHelper.TABLE_TRANSACTIONS, whereClause, null);
			// Delete participants of the event from gc_persons table.
			whereClause = String.format("%s=%d",
					GCDatabaseHelper.TPColumns.event_id.name(), id);
			db.delete(GCDatabaseHelper.TABLE_PERSONS, whereClause, null);

			// Delete the event itself
			whereClause = String.format("%s=%d",
					GCDatabaseHelper.TEColumns.id.name(), id);
			db.delete(GCDatabaseHelper.TABLE_EVENTS, whereClause.toString(),
					null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Updates given event in the database.
	 * 
	 * This method assumes, that the corresponding event already exists and it
	 * updates only the main event data stored in <code>ClearingEvent</code>
	 * object. In particular the transactions and participants are not updated
	 * as those are stored in different tables and managed separately.
	 * 
	 * @param anEvent
	 *            Event to be updated.
	 */
	public void updateEvent(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(5);
		values.put(GCDatabaseHelper.TEColumns.name.name(), anEvent.getName());
		values.put(GCDatabaseHelper.TEColumns.note.name(), anEvent.getNote());
		values.put(GCDatabaseHelper.TEColumns.start_date.name(),
				dateFormat.format(anEvent.getStartDate()));
		values.put(GCDatabaseHelper.TEColumns.finish_date.name(),
				dateFormat.format(anEvent.getFinishDate()));
		values.put(GCDatabaseHelper.TEColumns.currency.name(), anEvent
				.getDefaultCurrency().toString());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TEColumns.id.name(), anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause, null);
	}

	/**
	 * Updates the name of given event in the database.
	 * 
	 * @param anEvent
	 *            Event, whose name should be updated.
	 */
	public void updateEventName(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TEColumns.name.name(), anEvent.getName());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TEColumns.id.name(), anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause, null);
	}

	/**
	 * Updates the note associated with event in the database.
	 * 
	 * @param anEvent
	 *            Event, whose note should be updated.
	 */
	public void updateEventNote(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TEColumns.note.name(), anEvent.getNote());
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(GCDatabaseHelper.TEColumns.id.name());
		whereClause.append('=');
		whereClause.append(anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values,
				whereClause.toString(), null);
	}

	/**
	 * Updates the starting date of given event in the database.
	 * 
	 * @param anEvent
	 *            Event, whose starting date should be updated.
	 */
	public void updateEventStartDate(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TEColumns.start_date.name(),
				dateFormat.format(anEvent.getStartDate()));
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(GCDatabaseHelper.TEColumns.id.name());
		whereClause.append('=');
		whereClause.append(anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values,
				whereClause.toString(), null);
	}

	/**
	 * Updates the finish date of given event in the database.
	 * 
	 * @param anEvent
	 *            Event, whose finish date should be updated.
	 */
	public void updateEventFinishDate(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TEColumns.finish_date.name(),
				dateFormat.format(anEvent.getFinishDate()));
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(GCDatabaseHelper.TEColumns.id.name());
		whereClause.append('=');
		whereClause.append(anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values,
				whereClause.toString(), null);
	}

	/**
	 * Updates the default currency of given event in the database.
	 * 
	 * @param anEvent
	 *            Event, whose default currency should be updated.
	 */
	public void updateEventCurrency(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TEColumns.currency.name(), anEvent
				.getDefaultCurrency().toString());
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(GCDatabaseHelper.TEColumns.id.name());
		whereClause.append('=');
		whereClause.append(anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values,
				whereClause.toString(), null);
	}

	/**
	 * Reads the person with given id from database.
	 * 
	 * @param id
	 *            Id or person to be retrieved from database.
	 * @param computeBalance
	 *            Determines, whether and how balance should be computed for the
	 *            person.
	 * @return <code>ClearingPerson</code> object representing person with
	 *         <code>id</code>, or <code>null</code>, if this person does not
	 *         exist in database.
	 */
	public ClearingPerson readPersonWithId(long id,
			ComputeBalance computeBalance) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.id.name(), id);
		Cursor personCursor = db.query(GCDatabaseHelper.TABLE_PERSONS,
				GCDatabaseHelper.TABLE_PERSONS_COLUMNS, whereClause, null,
				null, null, null);
		personCursor.moveToFirst();
		if (personCursor.isAfterLast()) {
			return null;
		}
		ClearingPerson person = new ClearingPerson(id,
				personCursor.getString(GCDatabaseHelper.TPColumns.name
						.ordinal()), BigDecimal.ZERO,
				personCursor.getString(GCDatabaseHelper.TPColumns.note
						.ordinal()));
		switch (computeBalance) {
			case DO_NOT_COMPUTE :
				break;
			case COMPUTE_CUMULATIVE :
				person.setBalance(computeBalanceOfPerson(personCursor
						.getInt(GCDatabaseHelper.TPColumns.event_id.ordinal()),
						id));
				break;
			case COMPUTE_ALL :
				person.setAllBalances(computeAllBalancesOfPerson(personCursor
						.getInt(GCDatabaseHelper.TPColumns.event_id.ordinal()),
						id));
				break;
		}
		return person;
	}

	/**
	 * Computes balance of given person within given event.
	 * 
	 * @param eventId
	 *            Id of event specifying transactions to consider in the
	 *            computation.
	 * @param personId
	 *            Id of person for which balance should be computed.
	 * @return Balance, i.e. sum of values of person with id equal to
	 *         <code>personId</code> over transactions within event with id
	 *         <code>eventId</code>.
	 */
	public BigDecimal computeBalanceOfPerson(long eventId, long personId) {
		// Get values in all transactions
		/*
		 * "SELECT value, currency, rate FROM TABLE_TRANSACTION_PARTICIPANTS
		 * INNER JOIN TABLE_TRANSACTIONS ON
		 * TABLE_TRANSACTION_PARTICIPANTS.event_id=TABLE_TRANSACTIONS.event_id
		 * AND
		 * TABLE_TRANSACTION_PARTICIPANTS.transaction_id=TABLE_TRANSACTIONS.id
		 * WHERE TABLE_TRANSACTION_PARTICIPANTS.event_id=eventId AND
		 * TABLE_TRANSACTION_PARTICIPANTS.participant_id=personId AND value<>0
		 * AND marked<>0"
		 */
		Currency eventCurrency = getCurrencyOfEvent(eventId);
		String eventCurrencyName = eventCurrency.toString();
		String query = String
				.format("SELECT %s, %s, %s FROM %s INNER JOIN %s ON %s.%s=%s.%s AND %s.%s=%s.%s WHERE %s.%s=%d AND %s.%s=%d AND %s<>0 AND %s<>0",
						GCDatabaseHelper.TTPColumns.value.name(),
						GCDatabaseHelper.TTColumns.currency.name(),
						GCDatabaseHelper.TTColumns.rate.name(),
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TABLE_TRANSACTIONS,
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TTPColumns.event_id.name(),
						GCDatabaseHelper.TABLE_TRANSACTIONS,
						GCDatabaseHelper.TTColumns.event_id.name(),
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TTPColumns.transaction_id.name(),
						GCDatabaseHelper.TABLE_TRANSACTIONS,
						GCDatabaseHelper.TTColumns.id.name(),
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TTPColumns.event_id.name(), eventId,
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TTPColumns.participant_id.name(),
						personId, GCDatabaseHelper.TTPColumns.value.name(),
						GCDatabaseHelper.TTPColumns.marked.name());
		Cursor valuesCursor = db.rawQuery(query, null);
		valuesCursor.moveToFirst();
		BigDecimal amount = BigDecimal.ZERO;
		while (!valuesCursor.isAfterLast()) {
			BigDecimal value = new BigDecimal(valuesCursor.getString(0));
			String currencyName = valuesCursor.getString(1);
			BigDecimal rate = new BigDecimal(valuesCursor.getString(2));
			if (eventCurrencyName.compareTo(currencyName) == 0) {
				amount = amount.add(value);
			} else {
				BigDecimal mult = value.multiply(rate);
				amount = amount.add(mult);
			}
			valuesCursor.moveToNext();
		}
		return amount;
	}

	/**
	 * Computes all per-currency balances of given person. For each currency
	 * used by some transaction in event with id equal to <code>eventId</code>,
	 * balance of person with id equal to <code>personId</code> is computed in
	 * this currency and then mapped to the name of the currency in the
	 * <code>SortedMap</code> being returned.
	 * 
	 * @param eventId
	 *            Id of event specifying transactions to consider in the
	 *            computation.
	 * @param personId
	 *            Id of person for which balances should be computed.
	 * @return <code>SortedMap</code> mapping name of currency with
	 *         <code>BigDecimal</code> value of person in this currency.
	 */
	public SortedMap<String, BigDecimal> computeAllBalancesOfPerson(
			long eventId, long personId) {
		String query = String
				.format("SELECT %s, %s FROM %s INNER JOIN %s ON %s.%s=%s.%s AND %s.%s=%s.%s WHERE %s.%s=%d AND %s.%s=%d AND %s<>0 AND %s<>0",
						GCDatabaseHelper.TTPColumns.value.name(),
						GCDatabaseHelper.TTColumns.currency.name(),
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TABLE_TRANSACTIONS,
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TTPColumns.event_id.name(),
						GCDatabaseHelper.TABLE_TRANSACTIONS,
						GCDatabaseHelper.TTColumns.event_id.name(),
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TTPColumns.transaction_id.name(),
						GCDatabaseHelper.TABLE_TRANSACTIONS,
						GCDatabaseHelper.TTColumns.id.name(),
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TTPColumns.event_id.name(), eventId,
						GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						GCDatabaseHelper.TTPColumns.participant_id.name(),
						personId, GCDatabaseHelper.TTPColumns.value.name(),
						GCDatabaseHelper.TTPColumns.marked.name());
		Cursor valuesCursor = db.rawQuery(query, null);
		TreeMap<String, BigDecimal> amounts = new TreeMap<String, BigDecimal>();
		valuesCursor.moveToFirst();
		while (!valuesCursor.isAfterLast()) {
			BigDecimal value = new BigDecimal(valuesCursor.getString(0));
			String currencyName = valuesCursor.getString(1);
			BigDecimal oldValue = amounts.get(currencyName);
			if (oldValue == null) {
				amounts.put(currencyName, value);
			} else {
				amounts.put(currencyName, oldValue.add(value));
			}
			valuesCursor.moveToNext();
		}
		return amounts;
	}

	/**
	 * Reads all participants of given event.
	 * 
	 * @param eventId
	 *            Id of event, whose participants should be retrieved from
	 *            database.
	 * @param computeBalance
	 *            Determines, whether and how balances of participants should be
	 *            computed.
	 * @return Vector of all participants of event with id equal to
	 *         <code>eventId</code>.
	 */
	public Vector<ClearingPerson> readParticipantsOfEvent(long eventId,
			ComputeBalance computeBalance) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.event_id.name(), eventId);
		Cursor result = db.query(GCDatabaseHelper.TABLE_PERSONS,
				GCDatabaseHelper.TABLE_PERSONS_COLUMNS, whereClause, null,
				null, null, GCDatabaseHelper.TPColumns.name.name());
		Vector<ClearingPerson> participants = new Vector<ClearingPerson>();
		result.moveToFirst();
		while (!result.isAfterLast()) {
			ClearingPerson person = new ClearingPerson(
					result.getInt(GCDatabaseHelper.TPColumns.id.ordinal()));
			person.setName(result.getString(GCDatabaseHelper.TPColumns.name
					.ordinal()));
			person.setNote(result.getString(GCDatabaseHelper.TPColumns.note
					.ordinal()));
			switch (computeBalance) {
				case DO_NOT_COMPUTE :
					break;
				case COMPUTE_CUMULATIVE :
					person.setBalance(computeBalanceOfPerson(eventId, result
							.getInt(GCDatabaseHelper.TPColumns.id.ordinal())));
					break;
				case COMPUTE_ALL :
					person.setAllBalances(computeAllBalancesOfPerson(eventId,
							result.getInt(GCDatabaseHelper.TPColumns.id
									.ordinal())));
					break;
			}
			participants.add(person);
			result.moveToNext();
		}
		return participants;
	}

	/**
	 * Updates name of participant in the database.
	 * 
	 * @param participant
	 *            Id of participant whose name should be updated.
	 */
	public void updateParticipantName(ClearingPerson participant) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TPColumns.name.name(),
				participant.getName());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.id.name(), participant.getId());
		db.update(GCDatabaseHelper.TABLE_PERSONS, values, whereClause, null);
	}

	/**
	 * Deletes participant from the database. Note, that only participant's
	 * record in <code>TABLE_PERSONS</code> is deleted. Transactions are not
	 * affected, and thus it may happen, that then within a transaction a person
	 * is specified with id, which has been deleted. This situation is taken
	 * care of when transaction is retrieved or modified in the database.
	 * 
	 * @param id
	 *            Id of participant to be deleted.
	 */
	public void deleteParticipantWithId(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.id.name(), id);
		db.delete(GCDatabaseHelper.TABLE_PERSONS, whereClause, null);
	}

	/**
	 * Returns the database object.
	 * 
	 * @return The <code>SQLiteDatabase</code> interface object.
	 */
	public SQLiteDatabase getDB() {
		return db;
	}

	/**
	 * Creates new participant within given event with given name.
	 * 
	 * @param eventId
	 *            Id of event in which participant should be created.
	 * @param name
	 *            Name of participant which should be created.
	 * @return Newly created participant as a <code>ClearingPerson</code>
	 *         object.
	 */
	public ClearingPerson createNewParticipant(long eventId, String name) {
		ContentValues values = new ContentValues(4);
		values.put(GCDatabaseHelper.TPColumns.event_id.name(), eventId);
		values.put(GCDatabaseHelper.TPColumns.name.name(), name);
		values.put(GCDatabaseHelper.TPColumns.note.name(), "");
		long id = db.insert(GCDatabaseHelper.TABLE_PERSONS, null, values);
		if (id > 0) {
			return readPersonWithId(id, ComputeBalance.DO_NOT_COMPUTE);
		}
		return null;
	}

	/**
	 * Translates given database row into a transaction. The row is supposed to
	 * contain all columns in order given at the time of creation of the table
	 * within <code>GCDatabaseHelper</code>. It is assumed, that
     * <code>participants</code>
     * points to the row of
     * <code>TABLE_TRANSACTION_PARTICIPANTS</code>, the
     * participants are added while this row contains
     * participant for the transaction stored in
     * <code>row</code>. After finishing this method, this cursor
     * either points to the row behind the last one, or it contains
     * first participant from different transaction.
	 * 
	 * @param row
	 *            Database row containing the transaction data.
     * @param participants
     *              Cursor with list of participants.
     * @return <code>ClearingTransaction</code> object initialized with data
	 *         from <code>row</code> parameter. If <code>row</code> does not
	 *         contain any data, <code>null</code> is returned.
	 */
	public ClearingTransaction transactionFromRow(Cursor row, Cursor participants) {
        if (row == null || row.isAfterLast()) {
            return null;
        }
	
        long transactionId = row.getLong(GCDatabaseHelper.TTColumns.id.ordinal());
        ClearingTransaction aTransaction = new ClearingTransaction(
				transactionId,
				row.getLong(GCDatabaseHelper.TTColumns.event_id.ordinal()));
		aTransaction.setName(row.getString(GCDatabaseHelper.TTColumns.name
				.ordinal()));
		SimpleDateFormat dateParser = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		aTransaction.setDate(dateParser.parse(
				row.getString(GCDatabaseHelper.TTColumns.date.ordinal()),
				new ParsePosition(0)));
		aTransaction.setCurrency(Currency.getInstance(row
				.getString(GCDatabaseHelper.TTColumns.currency.ordinal())));
		aTransaction.setRate(new BigDecimal(row
				.getString(GCDatabaseHelper.TTColumns.rate.ordinal())));
		aTransaction.setAmount(new BigDecimal(row
				.getLong(GCDatabaseHelper.TTColumns.amount.ordinal())));
		aTransaction.setReceiverId(row
				.getLong(GCDatabaseHelper.TTColumns.receiver_id.ordinal()));
		aTransaction
				.setSplitEvenly(row
						.getInt(GCDatabaseHelper.TTColumns.split_evenly
								.ordinal()) != 0);
		aTransaction.setNote(row.getString(GCDatabaseHelper.TTColumns.note
				.ordinal()));

        // Participants
        if (participants != null) {
            while (!participants.isAfterLast()
                    && participants
                    .getLong(GCDatabaseHelper.TTPColumns.transaction_id
                        .ordinal()) == transactionId) {
                aTransaction
                    .addParticipant(
                            participants
                            .getInt(GCDatabaseHelper.TTPColumns.participant_id
                                .ordinal()),
                            new BigDecimal(
                                participants
                                .getString(GCDatabaseHelper.TTPColumns.value
                                    .ordinal())),
                            participants
                            .getInt(GCDatabaseHelper.TTPColumns.marked
                                .ordinal()) != 0);
                participants.moveToNext();
            }
        }
        return aTransaction;
    }

	/**
	 * Reads all transactions of given event into a <code>Vector</code>.
	 * 
	 * @param eventId
	 *            Id of event whose transactions should be retrieved from the
	 *            database.
	 * @return <code>Vector</code> of transactions of event with id equal to
	 *         <code>eventId</code>.
	 */
	public Vector<ClearingTransaction> readTransactionsOfEvent(long eventId) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.event_id.name(), eventId);
		Cursor transactionsCursor = db.query(
				GCDatabaseHelper.TABLE_TRANSACTIONS,
				GCDatabaseHelper.TABLE_TRANSACTIONS_COLUMNS, whereClause, null,
				null, null, GCDatabaseHelper.TTColumns.id.name());
		whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTPColumns.event_id.name(), eventId);
		Cursor participantsCursor = db.query(
				GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
				GCDatabaseHelper.TABLE_TTP_COLUMNS, whereClause, null, null,
				null, GCDatabaseHelper.TTPColumns.transaction_id.name());
		// Go through both cursors simultaneously.
		Vector<ClearingTransaction> transactions = new Vector<ClearingTransaction>(
				transactionsCursor.getCount());
		transactionsCursor.moveToFirst();
		participantsCursor.moveToFirst();
		while (!transactionsCursor.isAfterLast()) {
			ClearingTransaction aTransaction = transactionFromRow(transactionsCursor, participantsCursor);
			transactionsCursor.moveToNext();
			transactions.add(aTransaction);
		}
		return transactions;
	}

	/** Reads transaction with given id from database. Note, that
     * <code>id</code> of transaction is enough since it is unique for
     * and two transaction from different events have never the same
     * id.
	 * @param id Id of transaction to be read.
	 * @return Transaction with given id or null, if this transaction
     * is not present in the database.
	 */
	public ClearingTransaction readTransactionWithId(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), id);
		Cursor transactionCursor = db.query(
				GCDatabaseHelper.TABLE_TRANSACTIONS,
				GCDatabaseHelper.TABLE_TRANSACTIONS_COLUMNS, whereClause, null,
				null, null, null);
		transactionCursor.moveToFirst();
		if (transactionCursor.isAfterLast()) {
			return null;
		}
		whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTPColumns.transaction_id.name(), id);
		Cursor participantsCursor = db.query(
				GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
				GCDatabaseHelper.TABLE_TTP_COLUMNS, whereClause, null, null,
				null, null);
        participantsCursor.moveToFirst();
		ClearingTransaction aTransaction = transactionFromRow(transactionCursor, participantsCursor);
		return aTransaction;
	}

	/** Deletes the transactions from database. Involves also deleting
     * corresponding records from <code>TABLE_TRANSACTION_PARTICIPANTS</code>.
     *
	 * @param id Id if transaction to be deleted.
	 */
	public void deleteTransactionWithId(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTPColumns.transaction_id.name(), id);
		db.delete(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS, whereClause,
				null);
		whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), id);
		db.delete(GCDatabaseHelper.TABLE_TRANSACTIONS, whereClause, null);
	}

	/** Returns default currency of event with given id.
	 * @param id Id of event
	 * @return Default currency of event with id equal to
     * <code>id</code>.
	 */
	public Currency getCurrencyOfEvent(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TEColumns.id.name(), id);
		String[] currencyColumn = {GCDatabaseHelper.TEColumns.currency.name()};
		Cursor currencyCursor = db.query(GCDatabaseHelper.TABLE_EVENTS,
				currencyColumn, whereClause, null, null, null, null);
		currencyCursor.moveToFirst();
		if (currencyCursor.isAfterLast()) {
			return null;
		}
		return Currency.getInstance(currencyCursor.getString(0));
	}

	/** Creates new empty transaction within event with given id and
     * stores it in database.
	 * @param eventId Id of event
	 * @return New empty transaction which has been created and stored
     * in the database. The transaction belongs to event with id equal
     * to <code>eventId</code>.
	 * @throws GCEventDoesNotExistException If event with id equal to
     * <code>eventId</code> does not exist.
	 */
	public ClearingTransaction createNewTransaction(long eventId)
			throws GCEventDoesNotExistException {
		// Get default currency of the event
		Currency transactionCurrency = getCurrencyOfEvent(eventId);
		if (transactionCurrency == null) {
			throw new GCEventDoesNotExistException("Cannot create transaction.");
		}

		ContentValues values = new ContentValues(8);
		values.put(GCDatabaseHelper.TTColumns.event_id.name(), eventId);
		values.put(GCDatabaseHelper.TTColumns.name.name(), "");
		values.put(GCDatabaseHelper.TTColumns.date.name(),
				dateFormat.format(new Date()));
		values.put(GCDatabaseHelper.TTColumns.currency.name(),
				transactionCurrency.toString());
		values.put(GCDatabaseHelper.TTColumns.rate.name(), "1");
		values.put(GCDatabaseHelper.TTColumns.amount.name(), "0");
		values.put(GCDatabaseHelper.TTColumns.receiver_id.name(), -1);
		values.put(GCDatabaseHelper.TTColumns.split_evenly.name(), true);
		values.put(GCDatabaseHelper.TTColumns.note.name(), "");
		long id = db.insert(GCDatabaseHelper.TABLE_TRANSACTIONS, null, values);
		if (id > 0) {
			/* Add all participants with value 0 to TTP */
			String whereClause = String.format("%s=%d",
					GCDatabaseHelper.TPColumns.event_id.name(), eventId);
			String[] participantIdColumn = {GCDatabaseHelper.TPColumns.id
					.name()};
			Cursor participantsCursor = db.query(
					GCDatabaseHelper.TABLE_PERSONS, participantIdColumn,
					whereClause, null, null, null, null);
			participantsCursor.moveToFirst();
			while (!participantsCursor.isAfterLast()) {
				ContentValues participantValues = new ContentValues(5);
				participantValues.put(
						GCDatabaseHelper.TTPColumns.event_id.name(), eventId);
				participantValues.put(
						GCDatabaseHelper.TTPColumns.transaction_id.name(), id);
				participantValues.put(
						GCDatabaseHelper.TTPColumns.participant_id.name(),
						participantsCursor.getLong(0));
				participantValues.put(GCDatabaseHelper.TTPColumns.value.name(),
						"0");
				participantValues.put(
						GCDatabaseHelper.TTPColumns.marked.name(), 1);
				db.insert(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						null, participantValues);
				participantsCursor.moveToNext();
			}
			return readTransactionWithId(id);
		}
		return null;
	}

	/** Updates the main data of given transaction. Only the main data
     * are updated, i.e. values of participants are not updated in the
     * database and has to be updated separately.
	 * @param aTransaction Transaction to update in the database.
	 */
	public void updateTransaction(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(7);
		values.put(GCDatabaseHelper.TTColumns.name.name(),
				aTransaction.getName());
		values.put(GCDatabaseHelper.TTColumns.date.name(),
				dateFormat.format(aTransaction.getDate()));
		values.put(GCDatabaseHelper.TTColumns.currency.name(), aTransaction
				.getCurrency().toString());
		values.put(GCDatabaseHelper.TTColumns.rate.name(), aTransaction
				.getRate().toString());
		values.put(GCDatabaseHelper.TTColumns.amount.name(), aTransaction
				.getAmount().toString());
		values.put(GCDatabaseHelper.TTColumns.receiver_id.name(),
				aTransaction.getReceiverId());
		values.put(GCDatabaseHelper.TTColumns.split_evenly.name(),
				aTransaction.getSplitEvenly());
		values.put(GCDatabaseHelper.TTColumns.note.name(),
				aTransaction.getNote());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates name of given transaction in the database.
	 * @param aTransaction Transaction to update.
	 */
	public void updateTransactionName(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.name.name(),
				aTransaction.getName());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates note associated with given transaction in the
     * database.
	 * @param aTransaction Transaction to update.
	 */
	public void updateTransactionNote(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.note.name(),
				aTransaction.getNote());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates amount of given transaction in the database.
	 * @param aTransaction Transaction to update.
	 */
	public void updateTransactionAmount(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.amount.name(), aTransaction
				.getAmount().toString());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates date of given transaction in the database.
	 * @param aTransaction Transaction to update.
	 */
	public void updateTransactionDate(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.date.name(),
				dateFormat.format(aTransaction.getDate()));
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates currency of given transaction.
	 * @param aTransaction Transaction to update.
	 */
	public void updateTransactionCurrency(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.currency.name(), aTransaction
				.getCurrency().toString());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates rate of transaction currency to default event
     * currency.
	 * @param aTransaction Transaction to update.
	 */
	public void updateTransactionRate(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.rate.name(), aTransaction
				.getRate().toString());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates receiver id of given transaction.
	 * @param aTransaction Transaction to update.
	 */
	public void updateTransactionReceiverId(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.receiver_id.name(),
				aTransaction.getReceiverId());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates split evenly status of given transaction.
	 * @param aTransaction Transaction to update.
	 */
	public void updateTransactionSplitEvenly(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.split_evenly.name(),
				aTransaction.getSplitEvenly());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	/** Updates the value of given participant within given
     * transaction. The value and mark status of the participant
     * is retrieved from transaction.
	 * @param transaction Transaction in which the value of
     * participant with id equal to <code>participantId</code> should be changed.
	 * @param participantId Id of the participant whose value should
     * be changed.
	 */
	public void updateTransactionParticipantValue(
			ClearingTransaction transaction, long participantId) {
		long eventId = transaction.getEventId();
		long transactionId = transaction.getId();
		BigDecimal value = transaction.getParticipantValue(participantId);
		boolean mark = transaction.isParticipantMarked(participantId);
		ContentValues values = new ContentValues(6);
		values.put(GCDatabaseHelper.TTPColumns.event_id.name(), eventId);
		values.put(GCDatabaseHelper.TTPColumns.transaction_id.name(),
				transactionId);
		values.put(GCDatabaseHelper.TTPColumns.participant_id.name(),
				participantId);
		values.put(GCDatabaseHelper.TTPColumns.value.name(), value.toString());
		values.put(GCDatabaseHelper.TTPColumns.marked.name(), mark);
		db.insertWithOnConflict(
				GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	/** Resets the values of all participants in given transactions.
     * The value of all participants within <code>aTransaction</code>
     * is set to 0 in the database. The mark status of all
     * participants is set to <code>defaultMark</code>. Note, that
     * this method does not modify the values and mark status within
     * <code>aTransaction</code> object.
	 * @param aTransaction
	 * @param defaultMark
	 */
	public void resetTransactionParticipantsValues(
			ClearingTransaction aTransaction, boolean defaultMark) {
		ContentValues values = new ContentValues(2);
		values.put(GCDatabaseHelper.TTPColumns.value.name(), "0");
		values.put(GCDatabaseHelper.TTPColumns.marked.name(), defaultMark);
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTPColumns.transaction_id.name(),
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS, values,
				whereClause, null);
	}

	/** Finds participants with minimum value within given event.
     * Note, that right now, the <code>computeBalanceOfPerson</code>
     * method is used to compute the value of the person. It means,
     * that the value is always taken cumulatively over all
     * transactions and all currencies (converted to the default
     * currency of the event).
	 * @param eventId Id of the event in which to look for the person
     * with lowest value.
	 * @return The <code>ParticipantValue</code> object representing
     * the value of participant with minimum value among others within
     * event with id equal to <code>eventId</code>.
	 */
	public ParticipantValue findParticipantWithMinValue(long eventId) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.event_id.name(), eventId);
		String[] TABLE_PERSONS_ID_COLUMN = {GCDatabaseHelper.TPColumns.id
				.name()};
		Cursor result = db.query(GCDatabaseHelper.TABLE_PERSONS,
				TABLE_PERSONS_ID_COLUMN, whereClause, null, null, null, null);
		result.moveToFirst();
		ParticipantValue valueInfo = null;
		if (!result.isAfterLast()) {
			long minId = result.getInt(0);
			BigDecimal minValue = computeBalanceOfPerson(eventId, minId);
			result.moveToNext();
			while (!result.isAfterLast()) {
				BigDecimal value = computeBalanceOfPerson(eventId,
						result.getInt(0));
				if (value.compareTo(minValue) < 0) {
					minId = result.getInt(0);
					minValue = value;
				}
				result.moveToNext();
			}
			valueInfo = new ParticipantValue(minId, minValue);
		}
		return valueInfo;
	}

	/** Returns all values of participants within given event. The
     * balances are computed cumulatively over all currencies and
     * transactions. Values in different currencies are converted to
     * the default currency of the event. For that purpose,
     * <code>computeBalanceOfPerson</code> is used to compute the
     * value.
	 * @param eventId Id of the event in which compute the values of
     * participants.
	 * @return Participant values in a <code>Vector</code>. They are
     * not sorted in any special way.
	 */
	public Vector<ParticipantValue> readEventParticipantValues(long eventId) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.event_id.name(), eventId);
		final String[] TABLE_PERSONS_ID_COLUMN = {GCDatabaseHelper.TPColumns.id
				.name()};
		Cursor result = db.query(GCDatabaseHelper.TABLE_PERSONS,
				TABLE_PERSONS_ID_COLUMN, whereClause, null, null, null, null);
		Vector<ParticipantValue> values = new Vector<ParticipantValue>(
				result.getCount());
		result.moveToFirst();
		while (!result.isAfterLast()) {
			BigDecimal value = computeBalanceOfPerson(eventId, result.getInt(0));
			ParticipantValue valueInfo = new ParticipantValue(result.getInt(0),
					value);
			values.add(valueInfo);
			result.moveToNext();
		}
		return values;
	}

	/** Returns all values of participants within given event
     * separately for each currency.
	 * @param eventId Id of the event in which compute the values of
     * participants.
	 * @return The <code>SortedMap</code> which assigns the vector of
     * values to every name of a currency, which is used in some
     * transaction of the event.
	 */
	public SortedMap<String, Vector<ParticipantValue>> readEventParticipantValuesPerCurrency(
			long eventId) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.event_id.name(), eventId);
		final String[] TABLE_PERSONS_ID_COLUMN = {GCDatabaseHelper.TPColumns.id
				.name()};
		Cursor result = db.query(GCDatabaseHelper.TABLE_PERSONS,
				TABLE_PERSONS_ID_COLUMN, whereClause, null, null, null, null);
		SortedMap<String, Vector<ParticipantValue>> values = new TreeMap<String, Vector<ParticipantValue>>();
		result.moveToFirst();
		while (!result.isAfterLast()) {
			SortedMap<String, BigDecimal> personValues = computeAllBalancesOfPerson(
					eventId, result.getInt(0));
			for (Map.Entry<String, BigDecimal> entry : personValues.entrySet()) {
				ParticipantValue valueInfo = new ParticipantValue(
						result.getInt(0), entry.getValue());
				Vector<ParticipantValue> curValues = values.get(entry.getKey());
				if (curValues == null) {
					curValues = new Vector<ParticipantValue>();
					values.put(entry.getKey(), curValues);
				}
				curValues.add(valueInfo);
			}
			result.moveToNext();
		}
		return values;
	}

	/** Sets the default rate for given pair of currencies.
	 * @param left First currency.
	 * @param right Second currency.
	 * @param rate The default rate, i.e. 1 value of first currency
     * corresponds to <code>rate</code> values in the second currency.
	 */
	public void setDefaultRate(Currency left, Currency right, BigDecimal rate) {
		String whereClause = String.format("%s=\"%s\" AND %s=\"%s\"",
				GCDatabaseHelper.TRColumns.left_currency.name(),
				left.toString(),
				GCDatabaseHelper.TRColumns.right_currency.name(),
				right.toString());
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TRColumns.rate.name(), rate.toString());
		if (db.update(GCDatabaseHelper.TABLE_RATES, values, whereClause, null) == 0) {
			values.put(GCDatabaseHelper.TRColumns.left_currency.name(),
					left.toString());
			values.put(GCDatabaseHelper.TRColumns.right_currency.name(),
					right.toString());
			db.insert(GCDatabaseHelper.TABLE_RATES, null, values);
		}
	}

	/** Retrieves the default rate for given pair of currencies.
	 * @param left First currency.
	 * @param right Second currency.
	 * @return The default rate between the first and the second
     * currency. I.e. 1 value of first currency corresponds to
     * <code>rate</code> values in the second currency, where
     * <code>rate</code> denotes the value being returned.
	 */
	public BigDecimal getDefaultRate(Currency left, Currency right) {
		if (left.equals(right)) {
			return BigDecimal.ONE;
		}
		String whereClause = String.format("%s=\"%s\" AND %s=\"%s\"",
				GCDatabaseHelper.TRColumns.left_currency.name(),
				left.toString(),
				GCDatabaseHelper.TRColumns.right_currency.name(),
				right.toString());
		final String[] RATE_COLUMN = {GCDatabaseHelper.TRColumns.rate.name()};
		Cursor result = db.query(GCDatabaseHelper.TABLE_RATES, RATE_COLUMN,
				whereClause, null, null, null, null);
		result.moveToFirst();
		BigDecimal rate = BigDecimal.ONE;
		if (!result.isAfterLast()) {
			rate = new BigDecimal(result.getString(0));
		}
		return rate;
	}
}
