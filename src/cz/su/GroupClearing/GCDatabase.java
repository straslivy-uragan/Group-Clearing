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

public class GCDatabase {
	private SQLiteDatabase db;
	private final SimpleDateFormat dateFormat;

	public GCDatabase(Context context) {
		db = (new GCDatabaseHelper(context)).getWritableDatabase();
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public void close() {
		db.close();
		db = null;
	}

	public ClearingEvent createNewEvent() {
		ContentValues values = new ContentValues(5);
		values.put(GCDatabaseHelper.TE_NAME, "");
		values.put(GCDatabaseHelper.TE_NOTE, "");
		values.put(GCDatabaseHelper.TE_START_DATE,
				dateFormat.format(new Date()));
		values.put(GCDatabaseHelper.TE_FINISH_DATE,
				dateFormat.format(new Date()));
		values.put(GCDatabaseHelper.TE_CURRENCY,
				Currency.getInstance(Locale.getDefault()).toString());
		long id = db.insert(GCDatabaseHelper.TABLE_EVENTS, null, values);
		if (id > 0) {
			return readEventWithId(id);
		}
		return null;
	}

	public ClearingEvent readEventWithId(long id) {
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TE_ID, id);
		Cursor eventCursor = db.query(GCDatabaseHelper.TABLE_EVENTS,
				GCDatabaseHelper.TABLE_EVENTS_COLUMNS, whereClause, null, null,
				null, null);
		eventCursor.moveToFirst();
		if (eventCursor.isAfterLast()) {
			return null;
		}
		SimpleDateFormat dateParser = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
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

	public void deleteEventWithId(long id) {
		db.beginTransaction();
		try {
			// Delete transactions in the event.
			String whereClause = String.format("%s=%d",
					GCDatabaseHelper.TTP_EVENT_ID, id);
			db.delete(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
					whereClause, null);
			whereClause = String.format("%s=%d", GCDatabaseHelper.TT_EVENT_ID,
					id);
			db.delete(GCDatabaseHelper.TABLE_TRANSACTIONS, whereClause, null);
			// Delete participants of the event from gc_persons table.
			whereClause = String.format("%s=%d", GCDatabaseHelper.TP_EVENT_ID,
					id);
			db.delete(GCDatabaseHelper.TABLE_PERSONS, whereClause, null);

			// Delete the event itself
			whereClause = String.format("%s=%d", GCDatabaseHelper.TE_ID, id);
			db.delete(GCDatabaseHelper.TABLE_EVENTS, whereClause.toString(),
					null);
			db.setTransactionSuccessful();
		} finally {
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
	 *            Event to be saved.
	 */
	public void updateEvent(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(5);
		values.put(GCDatabaseHelper.TE_NAME, anEvent.getName());
		values.put(GCDatabaseHelper.TE_NOTE, anEvent.getNote());
		values.put(GCDatabaseHelper.TE_START_DATE,
				dateFormat.format(anEvent.getStartDate()));
		values.put(GCDatabaseHelper.TE_FINISH_DATE,
				dateFormat.format(anEvent.getFinishDate()));
		values.put(GCDatabaseHelper.TE_CURRENCY, anEvent.getDefaultCurrency()
				.toString());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TE_ID,
				anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause, null);
	}

	public void updateEventName(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TE_NAME, anEvent.getName());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TE_ID,
				anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause, null);
	}

	public void updateEventNote(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TE_NOTE, anEvent.getNote());
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(GCDatabaseHelper.TE_ID);
		whereClause.append('=');
		whereClause.append(anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values,
				whereClause.toString(), null);
	}

	public void updateEventStartDate(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TE_START_DATE,
				dateFormat.format(anEvent.getStartDate()));
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(GCDatabaseHelper.TE_ID);
		whereClause.append('=');
		whereClause.append(anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values,
				whereClause.toString(), null);
	}

	public void updateEventFinishDate(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TE_FINISH_DATE,
				dateFormat.format(anEvent.getFinishDate()));
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(GCDatabaseHelper.TE_ID);
		whereClause.append('=');
		whereClause.append(anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values,
				whereClause.toString(), null);
	}

	public void updateEventCurrency(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TE_CURRENCY, anEvent.getDefaultCurrency()
				.toString());
		StringBuilder whereClause = new StringBuilder();
		whereClause.append(GCDatabaseHelper.TE_ID);
		whereClause.append('=');
		whereClause.append(anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values,
				whereClause.toString(), null);
	}

	public ClearingPerson readPersonWithId(long id) {
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TP_ID, id);
		Cursor personCursor = db.query(GCDatabaseHelper.TABLE_PERSONS,
				GCDatabaseHelper.TABLE_PERSONS_COLUMNS, whereClause, null,
				null, null, null);
		personCursor.moveToFirst();
		if (personCursor.isAfterLast()) {
			return null;
		}
		ClearingPerson person = new ClearingPerson(id,
				personCursor.getString(2), computeBalanceOfPerson(
						personCursor.getInt(1), id), personCursor.getString(3));
		return person;
	}

	public long computeBalanceOfPerson(long eventId, long personId) {
		long amount = 0;
		// Get all positive amounts
		/* String query = String.format(
				"SELECT SUM(%s) FROM %s WHERE %s=%d AND %s=%d",
				GCDatabaseHelper.TT_AMOUNT,
				GCDatabaseHelper.TABLE_TRANSACTIONS,
				GCDatabaseHelper.TT_EVENT_ID, eventId,
				GCDatabaseHelper.TT_RECEIVER_ID, personId);
		Cursor sumCursor = db.rawQuery(query, null);
		sumCursor.moveToFirst();
		if (!sumCursor.isAfterLast()) {
			amount = sumCursor.getLong(0);
		}
         */
		// Get values in all transactions
        String query = String.format(
                "SELECT SUM(%s) FROM %s WHERE %s=%d AND %s=%d AND %s<>0",
                GCDatabaseHelper.TTP_VALUE,
                GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
                GCDatabaseHelper.TTP_EVENT_ID, eventId,
                GCDatabaseHelper.TTP_PARTICIPANT_ID, personId,
                GCDatabaseHelper.TTP_MARK);
        Cursor sumCursor = db.rawQuery(query, null);
        sumCursor.moveToFirst();
        if (!sumCursor.isAfterLast()) {
            amount += sumCursor.getLong(0);
        }
		return amount;
	}

	public Vector<ClearingPerson> readParticipantsOfEvent(long eventId) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TP_EVENT_ID, eventId);
		Cursor result = db.query(GCDatabaseHelper.TABLE_PERSONS,
				GCDatabaseHelper.TABLE_PERSONS_COLUMNS, whereClause, null,
				null, null, GCDatabaseHelper.TP_NAME);
		Vector<ClearingPerson> participants = new Vector<ClearingPerson>();
		result.moveToFirst();
		while (!result.isAfterLast()) {
			ClearingPerson person = new ClearingPerson(result.getInt(0),
					result.getString(2), computeBalanceOfPerson(eventId,
							result.getInt(0)), result.getString(3));
			participants.add(person);
			result.moveToNext();
		}
		return participants;
	}

	public void updateParticipantName(ClearingPerson participant) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TP_NAME, participant.getName());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TP_ID,
				participant.getId());
		db.update(GCDatabaseHelper.TABLE_PERSONS, values, whereClause, null);
	}

	public void deleteParticipantWithId(long id) {
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TP_ID, id);
		db.delete(GCDatabaseHelper.TABLE_PERSONS, whereClause, null);
	}

	public SQLiteDatabase getDB() {
		return db;
	}

	public ClearingPerson createNewParticipant(long eventId, String name) {
		ContentValues values = new ContentValues(4);
		values.put(GCDatabaseHelper.TP_EVENT_ID, eventId);
		values.put(GCDatabaseHelper.TP_NAME, name);
		values.put(GCDatabaseHelper.TP_NOTE, "");
		long id = db.insert(GCDatabaseHelper.TABLE_PERSONS, null, values);
		if (id > 0) {
			return readPersonWithId(id);
		}
		return null;
	}

	public Vector<ClearingTransaction> readTransactionsOfEvent(long eventId) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TT_EVENT_ID, eventId);
		Cursor transactionsCursor = db.query(
				GCDatabaseHelper.TABLE_TRANSACTIONS,
				GCDatabaseHelper.TABLE_TRANSACTIONS_COLUMNS, whereClause, null,
				null, null, GCDatabaseHelper.TT_ID);
		whereClause = String.format("%s=%d", GCDatabaseHelper.TTP_EVENT_ID,
				eventId);
		Cursor participantsCursor = db.query(
				GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
				GCDatabaseHelper.TABLE_TTP_COLUMNS, whereClause, null, null,
				null, GCDatabaseHelper.TTP_TRANSACTION_ID);
		// Go through both cursors simultaneously.
		Vector<ClearingTransaction> transactions = new Vector<ClearingTransaction>(
				transactionsCursor.getCount());
		transactionsCursor.moveToFirst();
		participantsCursor.moveToFirst();
		while (!transactionsCursor.isAfterLast()) {
			ClearingTransaction aTransaction = new ClearingTransaction(
					transactionsCursor.getInt(0), transactionsCursor.getInt(1));
			aTransaction.setName(transactionsCursor.getString(2));
			SimpleDateFormat dateParser = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			aTransaction.setDate(dateParser.parse(
					transactionsCursor.getString(3), new ParsePosition(0)));
			aTransaction.setCurrency(Currency.getInstance(transactionsCursor
					.getString(4)));
			aTransaction.setAmount(transactionsCursor.getLong(5));
			aTransaction.setReceiverId(transactionsCursor.getLong(6), null);
			aTransaction.setSplitEvenly(transactionsCursor.getInt(7) != 0, null);
			aTransaction.setNote(transactionsCursor.getString(8));
			// Participants
			while (!participantsCursor.isAfterLast()
					&& participantsCursor.getInt(2) == transactionsCursor
							.getInt(0)) {
				aTransaction.addParticipant(participantsCursor.getInt(3),
						participantsCursor.getInt(4),
                        participantsCursor.getInt(5) != 0);
				participantsCursor.moveToNext();
			}
			transactionsCursor.moveToNext();
			transactions.add(aTransaction);
		}
		return transactions;
	}

	public ClearingTransaction readTransactionWithId(long id) {
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID, id);
		Cursor transactionCursor = db.query(
				GCDatabaseHelper.TABLE_TRANSACTIONS,
				GCDatabaseHelper.TABLE_TRANSACTIONS_COLUMNS, whereClause, null,
				null, null, null);
		transactionCursor.moveToFirst();
		if (transactionCursor.isAfterLast()) {
			return null;
		}
		whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTP_TRANSACTION_ID, id);
		Cursor participantsCursor = db.query(
				GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
				GCDatabaseHelper.TABLE_TTP_COLUMNS, whereClause, null, null,
				null, null);
		participantsCursor.moveToFirst();
		ClearingTransaction aTransaction = new ClearingTransaction(
				transactionCursor.getInt(0), transactionCursor.getInt(1));
		aTransaction.setName(transactionCursor.getString(2));
		SimpleDateFormat dateParser = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		aTransaction.setDate(dateParser.parse(transactionCursor.getString(3),
				new ParsePosition(0)));
		aTransaction.setCurrency(Currency.getInstance(transactionCursor
				.getString(4)));
		aTransaction.setAmount(transactionCursor.getLong(5));
		aTransaction.setReceiverId(transactionCursor.getLong(6), null);
		aTransaction.setSplitEvenly(transactionCursor.getInt(7) != 0, null);
		aTransaction.setNote(transactionCursor.getString(8));
		while (!participantsCursor.isAfterLast()) {
			aTransaction.addParticipant(participantsCursor.getInt(3),
					participantsCursor.getInt(4),
                    participantsCursor.getInt(5) != 0);
			participantsCursor.moveToNext();
		}
		return aTransaction;
	}

	public void deleteTransactionWithId(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTP_TRANSACTION_ID, id);
		db.delete(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS, whereClause,
				null);
		whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID, id);
		db.delete(GCDatabaseHelper.TABLE_TRANSACTIONS, whereClause, null);
	}

	public ClearingTransaction createNewTransaction(long eventId) throws GCEventDoesNotExistException {
		// Get default currency of the event
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TE_ID,
				eventId);
		String[] currencyColumn = {GCDatabaseHelper.TE_CURRENCY};
		Cursor currencyCursor = db.query(GCDatabaseHelper.TABLE_EVENTS,
				currencyColumn, whereClause, null, null, null, null);
		currencyCursor.moveToFirst();
		if (currencyCursor.isAfterLast()) {
			throw new GCEventDoesNotExistException("Cannot create transaction.");
		}
		String transactionCurrency = currencyCursor.getString(0);

		ContentValues values = new ContentValues(8);
		values.put(GCDatabaseHelper.TT_EVENT_ID, eventId);
		values.put(GCDatabaseHelper.TT_NAME, "");
		values.put(GCDatabaseHelper.TT_DATE, dateFormat.format(new Date()));
		values.put(GCDatabaseHelper.TT_CURRENCY, transactionCurrency);
		values.put(GCDatabaseHelper.TT_AMOUNT, 0);
		values.put(GCDatabaseHelper.TT_RECEIVER_ID, -1);
		values.put(GCDatabaseHelper.TT_SPLIT_EVENLY, true);
		values.put(GCDatabaseHelper.TT_NOTE, "");
		long id = db.insert(GCDatabaseHelper.TABLE_TRANSACTIONS, null, values);
		if (id > 0) {
			/* Add all participants with value 0 to TTP */
			whereClause = String.format("%s=%d", GCDatabaseHelper.TP_EVENT_ID,
					eventId);
			String[] participantIdColumn = {GCDatabaseHelper.TP_ID};
			Cursor participantsCursor = db.query(
					GCDatabaseHelper.TABLE_PERSONS, participantIdColumn,
					whereClause, null, null, null, null);
			participantsCursor.moveToFirst();
			while (!participantsCursor.isAfterLast()) {
				ContentValues participantValues = new ContentValues(5);
				participantValues.put(GCDatabaseHelper.TTP_EVENT_ID, eventId);
				participantValues.put(GCDatabaseHelper.TTP_TRANSACTION_ID, id);
				participantValues.put(GCDatabaseHelper.TTP_PARTICIPANT_ID,
						participantsCursor.getLong(0));
				participantValues.put(GCDatabaseHelper.TTP_VALUE, 0);
				participantValues.put(GCDatabaseHelper.TTP_MARK, 1);
				db.insert(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
						null, participantValues);
				participantsCursor.moveToNext();
			}
			return readTransactionWithId(id);
		}
		return null;
	}

	public void updateTransaction(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(7);
		values.put(GCDatabaseHelper.TT_NAME, aTransaction.getName());
		values.put(GCDatabaseHelper.TT_DATE,
				dateFormat.format(aTransaction.getDate()));
		values.put(GCDatabaseHelper.TT_CURRENCY, aTransaction.getCurrency()
				.toString());
		values.put(GCDatabaseHelper.TT_AMOUNT, aTransaction.getAmount());
		values.put(GCDatabaseHelper.TT_RECEIVER_ID,
				aTransaction.getReceiverId());
		values.put(GCDatabaseHelper.TT_SPLIT_EVENLY,
				aTransaction.getSplitEvenly());
		values.put(GCDatabaseHelper.TT_NOTE, aTransaction.getNote());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID,
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionName(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TT_NAME, aTransaction.getName());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID,
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionNote(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TT_NOTE, aTransaction.getNote());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID,
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionAmount(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TT_AMOUNT, aTransaction.getAmount());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID,
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionDate(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TT_DATE,
				dateFormat.format(aTransaction.getDate()));
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID,
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionReceiverId(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TT_RECEIVER_ID,
				aTransaction.getReceiverId());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID,
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionSplitEvenly(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TT_SPLIT_EVENLY,
				aTransaction.getSplitEvenly());
		String whereClause = String.format("%s=%d", GCDatabaseHelper.TT_ID,
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionParticipantValue(long eventId,
			long transactionId, long participantId,
            long value, boolean mark) {
		ContentValues values = new ContentValues(6);
		values.put(GCDatabaseHelper.TTP_EVENT_ID, eventId);
		values.put(GCDatabaseHelper.TTP_TRANSACTION_ID, transactionId);
		values.put(GCDatabaseHelper.TTP_PARTICIPANT_ID, participantId);
		values.put(GCDatabaseHelper.TTP_VALUE, value);
		values.put(GCDatabaseHelper.TTP_MARK, mark);
		db.insertWithOnConflict(
				GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

    public void resetTransactionParticipantsValues(
            ClearingTransaction aTransaction, boolean defaultMark) {
        ContentValues values = new ContentValues(2);
        values.put(GCDatabaseHelper.TTP_VALUE, 0);
        values.put(GCDatabaseHelper.TTP_MARK, defaultMark);
        String whereClause = String.format("%s=%d", 
                GCDatabaseHelper.TTP_TRANSACTION_ID, aTransaction.getId());
        db.update(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
                values, whereClause, null);
    }
}
