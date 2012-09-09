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

    public ClearingEvent eventFromRow(Cursor row) {
        if (row.isAfterLast()) {
            return null;
        }
        SimpleDateFormat dateParser = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        ClearingEvent event = new ClearingEvent(row.
                getLong(GCDatabaseHelper.TEColumns.id.ordinal()));
        event.setName(row.getString(GCDatabaseHelper.TEColumns.name
                    .ordinal()));
        event.setNote(row.getString(GCDatabaseHelper.TEColumns.note
                    .ordinal()));
        Date startDate = dateParser.parse(row
                .getString(GCDatabaseHelper.TEColumns.start_date.ordinal()),
                new ParsePosition(0));
        event.setStartDate(startDate);
        Date finishDate = dateParser.parse(row
                .getString(GCDatabaseHelper.TEColumns.finish_date.ordinal()),
                new ParsePosition(0));
        event.setFinishDate(finishDate);
        event.setDefaultCurrency(Currency.getInstance(row
                    .getString(GCDatabaseHelper.TEColumns.currency.ordinal())));
        return event;
    }

	public ClearingEvent readEventWithId(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TEColumns.id.name(), id);
		Cursor eventCursor = db.query(GCDatabaseHelper.TABLE_EVENTS,
				GCDatabaseHelper.TABLE_EVENTS_COLUMNS, whereClause, null, null,
				null, null);
		eventCursor.moveToFirst();
        return eventFromRow(eventCursor);
	}


	public ClearingEvent[] readEvents() {
		Cursor eventCursor = db.query(GCDatabaseHelper.TABLE_EVENTS,
				GCDatabaseHelper.TABLE_EVENTS_COLUMNS, null, null, null,
				null, null);
		eventCursor.moveToFirst();
        if (eventCursor.isAfterLast()) {
            return null;
        }
        ClearingEvent[] events = new ClearingEvent[eventCursor.getCount()];
        int rowIndex = 0;
        while(!eventCursor.isAfterLast()) {
            events[rowIndex++] = eventFromRow(eventCursor);
            eventCursor.moveToNext();
        }
        return events;
	}

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

	public void updateEventName(ClearingEvent anEvent) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TEColumns.name.name(), anEvent.getName());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TEColumns.id.name(), anEvent.getId());
		db.update(GCDatabaseHelper.TABLE_EVENTS, values, whereClause, null);
	}

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

	public ClearingPerson readPersonWithId(long id) {
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
						.ordinal()), computeBalanceOfPerson(
						personCursor.getInt(GCDatabaseHelper.TPColumns.event_id
								.ordinal()), id),
				personCursor.getString(GCDatabaseHelper.TPColumns.note
						.ordinal()));
		return person;
	}

	public long computeBalanceOfPerson(long eventId, long personId) {
		long amount = 0;
		// Get values in all transactions
        /* "SELECT value, currency, rate 
           FROM TABLE_TRANSACTION_PARTICIPANTS INNER JOIN TABLE_TRANSACTIONS
           ON TABLE_TRANSACTION_PARTICIPANTS.event_id=TABLE_TRANSACTIONS.event_id
           AND TABLE_TRANSACTION_PARTICIPANTS.transaction_id=TABLE_TRANSACTIONS.id
           WHERE TABLE_TRANSACTION_PARTICIPANTS.event_id=eventId 
           AND TABLE_TRANSACTION_PARTICIPANTS.participant_id=personId
           AND value<>0 AND marked<>0"
         */
        Currency eventCurrency = getCurrencyOfEvent(eventId);
        String eventCurrencyName = eventCurrency.toString();
        String query = String.format(
                "SELECT %s, %s, %s FROM %s INNER JOIN %s ON %s.%s=%s.%s AND %s.%s=%s.%s WHERE %s.%s=%d AND %s.%s=%d AND %s<>0 AND %s<>0",
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
                GCDatabaseHelper.TTPColumns.event_id.name(),
                eventId,
                GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS,
                GCDatabaseHelper.TTPColumns.participant_id.name(),
                personId,
                GCDatabaseHelper.TTPColumns.value.name(),
                GCDatabaseHelper.TTPColumns.marked.name());
        Cursor sumCursor = db.rawQuery(query, null);
		sumCursor.moveToFirst();
		while (!sumCursor.isAfterLast()) {
            long value = sumCursor.getLong(0);
            String currencyName = sumCursor.getString(1);
            double rate = sumCursor.getDouble(2);
            if (eventCurrencyName.compareTo(currencyName) == 0) {
                amount += value;
            } else {
                amount += Math.round(value * rate);
            }
            sumCursor.moveToNext();
		}
		return amount;
	}

	public Vector<ClearingPerson> readParticipantsOfEvent(long eventId) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.event_id.name(), eventId);
		Cursor result = db.query(GCDatabaseHelper.TABLE_PERSONS,
				GCDatabaseHelper.TABLE_PERSONS_COLUMNS, whereClause, null,
				null, null, GCDatabaseHelper.TPColumns.name.name());
		Vector<ClearingPerson> participants = new Vector<ClearingPerson>();
		result.moveToFirst();
		while (!result.isAfterLast()) {
			ClearingPerson person = new ClearingPerson(result
					.getInt(GCDatabaseHelper.TPColumns.id.ordinal()), result
					.getString(GCDatabaseHelper.TPColumns.name.ordinal()),
					computeBalanceOfPerson(eventId, result
							.getInt(GCDatabaseHelper.TPColumns.id.ordinal())),
					result.getString(GCDatabaseHelper.TPColumns.note.ordinal()));
			participants.add(person);
			result.moveToNext();
		}
		return participants;
	}

	public void updateParticipantName(ClearingPerson participant) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TPColumns.name.name(),
				participant.getName());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.id.name(), participant.getId());
		db.update(GCDatabaseHelper.TABLE_PERSONS, values, whereClause, null);
	}

	public void deleteParticipantWithId(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TPColumns.id.name(), id);
		db.delete(GCDatabaseHelper.TABLE_PERSONS, whereClause, null);
	}

	public SQLiteDatabase getDB() {
		return db;
	}

	public ClearingPerson createNewParticipant(long eventId, String name) {
		ContentValues values = new ContentValues(4);
		values.put(GCDatabaseHelper.TPColumns.event_id.name(), eventId);
		values.put(GCDatabaseHelper.TPColumns.name.name(), name);
		values.put(GCDatabaseHelper.TPColumns.note.name(), "");
		long id = db.insert(GCDatabaseHelper.TABLE_PERSONS, null, values);
		if (id > 0) {
			return readPersonWithId(id);
		}
		return null;
	}

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
			ClearingTransaction aTransaction = new ClearingTransaction(
					transactionsCursor.getInt(GCDatabaseHelper.TTColumns.id
							.ordinal()),
					transactionsCursor
							.getInt(GCDatabaseHelper.TTColumns.event_id
									.ordinal()));
			aTransaction.setName(transactionsCursor
					.getString(GCDatabaseHelper.TTColumns.name.ordinal()));
			SimpleDateFormat dateParser = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			aTransaction.setDate(dateParser.parse(transactionsCursor
					.getString(GCDatabaseHelper.TTColumns.date.ordinal()),
					new ParsePosition(0)));
			aTransaction.setCurrency(Currency.getInstance(transactionsCursor
					.getString(GCDatabaseHelper.TTColumns.currency.ordinal())));
			aTransaction.setRate(transactionsCursor
					.getDouble(GCDatabaseHelper.TTColumns.rate.ordinal()));
			aTransaction.setAmount(transactionsCursor
					.getLong(GCDatabaseHelper.TTColumns.amount.ordinal()));
			aTransaction.setReceiverId(transactionsCursor
					.getLong(GCDatabaseHelper.TTColumns.receiver_id.ordinal()));
			aTransaction
					.setSplitEvenly(transactionsCursor
							.getInt(GCDatabaseHelper.TTColumns.split_evenly
									.ordinal()) != 0);
			aTransaction.setNote(transactionsCursor
					.getString(GCDatabaseHelper.TTColumns.note.ordinal()));
			// Participants
			while (!participantsCursor.isAfterLast()
					&& participantsCursor
							.getLong(GCDatabaseHelper.TTPColumns.transaction_id
									.ordinal()) == transactionsCursor
							.getLong(GCDatabaseHelper.TTColumns.id.ordinal())) {
				aTransaction.addParticipant(participantsCursor
						.getInt(GCDatabaseHelper.TTPColumns.participant_id
								.ordinal()), participantsCursor
						.getLong(GCDatabaseHelper.TTPColumns.value.ordinal()),
						participantsCursor
								.getInt(GCDatabaseHelper.TTPColumns.marked
										.ordinal()) != 0);
				participantsCursor.moveToNext();
			}
			transactionsCursor.moveToNext();
			transactions.add(aTransaction);
		}
		return transactions;
	}

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
		ClearingTransaction aTransaction = new ClearingTransaction(
				transactionCursor.getInt(GCDatabaseHelper.TTColumns.id
						.ordinal()),
				transactionCursor.getInt(GCDatabaseHelper.TTColumns.event_id
						.ordinal()));
		aTransaction.setName(transactionCursor
				.getString(GCDatabaseHelper.TTColumns.name.ordinal()));
		SimpleDateFormat dateParser = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		aTransaction.setDate(dateParser.parse(transactionCursor
				.getString(GCDatabaseHelper.TTColumns.date.ordinal()),
				new ParsePosition(0)));
		aTransaction.setCurrency(Currency.getInstance(transactionCursor
				.getString(GCDatabaseHelper.TTColumns.currency.ordinal())));
		aTransaction.setRate(transactionCursor
				.getDouble(GCDatabaseHelper.TTColumns.rate.ordinal()));
		aTransaction.setAmount(transactionCursor
				.getLong(GCDatabaseHelper.TTColumns.amount.ordinal()));
		aTransaction.setReceiverId(transactionCursor
				.getLong(GCDatabaseHelper.TTColumns.receiver_id.ordinal()));
		aTransaction
				.setSplitEvenly(transactionCursor
						.getInt(GCDatabaseHelper.TTColumns.split_evenly
								.ordinal()) != 0);
		aTransaction.setNote(transactionCursor
				.getString(GCDatabaseHelper.TTColumns.note.ordinal()));
		participantsCursor.moveToFirst();

		while (!participantsCursor.isAfterLast()) {
			aTransaction.addParticipant(participantsCursor
					.getInt(GCDatabaseHelper.TTPColumns.participant_id
							.ordinal()), participantsCursor
					.getLong(GCDatabaseHelper.TTPColumns.value.ordinal()),
					participantsCursor
							.getInt(GCDatabaseHelper.TTPColumns.marked
									.ordinal()) != 0);
			participantsCursor.moveToNext();
		}

		return aTransaction;
	}

	public void deleteTransactionWithId(long id) {
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTPColumns.transaction_id.name(), id);
		db.delete(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS, whereClause,
				null);
		whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), id);
		db.delete(GCDatabaseHelper.TABLE_TRANSACTIONS, whereClause, null);
	}

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
		values.put(GCDatabaseHelper.TTColumns.rate.name(), 1.0);
		values.put(GCDatabaseHelper.TTColumns.amount.name(), 0);
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
						0);
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

	public void updateTransaction(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(7);
		values.put(GCDatabaseHelper.TTColumns.name.name(),
				aTransaction.getName());
		values.put(GCDatabaseHelper.TTColumns.date.name(),
				dateFormat.format(aTransaction.getDate()));
		values.put(GCDatabaseHelper.TTColumns.currency.name(), aTransaction
				.getCurrency().toString());
		values.put(GCDatabaseHelper.TTColumns.rate.name(),
				aTransaction.getRate());
		values.put(GCDatabaseHelper.TTColumns.amount.name(),
				aTransaction.getAmount());
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

	public void updateTransactionName(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.name.name(),
				aTransaction.getName());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionNote(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.note.name(),
				aTransaction.getNote());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionAmount(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.amount.name(),
				aTransaction.getAmount());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionDate(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.date.name(),
				dateFormat.format(aTransaction.getDate()));
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionCurrency(
			ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.currency.name(), aTransaction
				.getCurrency().toString());
        String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

    public void updateTransactionRate(ClearingTransaction aTransaction) {
        ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.rate.name(), aTransaction
				.getRate());
        String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
    }

	public void updateTransactionReceiverId(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.receiver_id.name(),
				aTransaction.getReceiverId());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionSplitEvenly(ClearingTransaction aTransaction) {
		ContentValues values = new ContentValues(1);
		values.put(GCDatabaseHelper.TTColumns.split_evenly.name(),
				aTransaction.getSplitEvenly());
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTColumns.id.name(), aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTIONS, values, whereClause,
				null);
	}

	public void updateTransactionParticipantValue(long eventId,
			long transactionId, long participantId, long value, boolean mark) {
		ContentValues values = new ContentValues(6);
		values.put(GCDatabaseHelper.TTPColumns.event_id.name(), eventId);
		values.put(GCDatabaseHelper.TTPColumns.transaction_id.name(),
				transactionId);
		values.put(GCDatabaseHelper.TTPColumns.participant_id.name(),
				participantId);
		values.put(GCDatabaseHelper.TTPColumns.value.name(), value);
		values.put(GCDatabaseHelper.TTPColumns.marked.name(), mark);
		db.insertWithOnConflict(
				GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	public void resetTransactionParticipantsValues(
			ClearingTransaction aTransaction, boolean defaultMark) {
		ContentValues values = new ContentValues(2);
		values.put(GCDatabaseHelper.TTPColumns.value.name(), 0);
		values.put(GCDatabaseHelper.TTPColumns.marked.name(), defaultMark);
		String whereClause = String.format("%s=%d",
				GCDatabaseHelper.TTPColumns.transaction_id.name(),
				aTransaction.getId());
		db.update(GCDatabaseHelper.TABLE_TRANSACTION_PARTICIPANTS, values,
				whereClause, null);
	}

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
			long minValue = computeBalanceOfPerson(eventId, minId);
			result.moveToNext();
			while (!result.isAfterLast()) {
				long value = computeBalanceOfPerson(eventId, result.getInt(0));
				if (value < minValue) {
					minId = result.getInt(0);
					minValue = value;
				}
				result.moveToNext();
			}
			valueInfo = new ParticipantValue(minId, minValue);
		}
		return valueInfo;
	}

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
			long value = computeBalanceOfPerson(eventId, result.getInt(0));
			ParticipantValue valueInfo = new ParticipantValue(result.getInt(0),
					value);
			values.add(valueInfo);
			result.moveToNext();
		}
		return values;
	}

    public void setDefaultRate(Currency left, Currency right, double rate) {
        String whereClause = String.format("%s=\"%s\" AND %s=\"%s\"",
                GCDatabaseHelper.TRColumns.left_currency.name(),
                left.toString(),
                GCDatabaseHelper.TRColumns.right_currency.name(),
                right.toString());
        ContentValues values = new ContentValues(1);
        values.put(GCDatabaseHelper.TRColumns.rate.name(), rate);
        if (db.update(GCDatabaseHelper.TABLE_RATES, values, whereClause, null)
                == 0) {
            values.put(GCDatabaseHelper.TRColumns.left_currency.name(),
                    left.toString());
            values.put(GCDatabaseHelper.TRColumns.right_currency.name(),
                    right.toString());
            db.insert(GCDatabaseHelper.TABLE_RATES, null, values);
        }
    }

    public double getDefaultRate(Currency left, Currency right) {
        if (left.equals(right)) {
            return 1.0;
        }
        String whereClause = String.format("%s=\"%s\" AND %s=\"%s\"",
                GCDatabaseHelper.TRColumns.left_currency.name(),
                left.toString(),
                GCDatabaseHelper.TRColumns.right_currency.name(),
                right.toString());
        final String[] RATE_COLUMN = {GCDatabaseHelper.TRColumns.rate.name()};
        Cursor result = db.query(GCDatabaseHelper.TABLE_RATES,
                RATE_COLUMN, whereClause, null, null, null, null);
        result.moveToFirst();
        double rate = 1.0;
        if (!result.isAfterLast())
        {
            rate = result.getDouble(0);
        }
        return rate;
    }
}
