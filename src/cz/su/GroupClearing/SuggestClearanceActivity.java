package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Class representing the activity which presents to the user a suggestion of
 * how the values of participants within the event can be balanced (or cleared)
 * off. In this activity, the values of participants are computed and then a
 * suggestion is computed of who should pay whom in order to make the values
 * participants as balanced as possible. It means that if the transactions would
 * be performed, sum of the positive values or sum of the negative values of
 * participants (or both) would be zero. The suggestion is then presented to the
 * user in a form of a table. Depending on the status of
 * <code>convertToEventCurrency</code> preference either a single suggestion is
 * computed in the main event currency, or multiple suggestions are computed in
 * all currencies used within the event. All these suggestions are nevertheless
 * presented in one table.
 * 
 * Each line of this table then corresponds to a <code>SimpleTransaction</code>
 * object representing a simple transaction of giving money by one participant
 * to another. After suggestion is presented user has the possibility to create
 * the clearing transactions according to the suggestion with a click of a
 * button. In this case the simple transactions are grouped in more complex
 * transactions with one receiver and more payers. I.e. the simple transactions
 * are grouped by their receiver and then the simple transactions with the same
 * receiver are merged into one complex transaction.
 * 
 * The object allows to sort simple transaction lines in the suggestion by payer
 * id/name or receiver id/name, but not all of these possibilities are actually
 * being used.
 * 
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class SuggestClearanceActivity extends Activity {

	/** The enum desribing sorting order of records in
     * <code>simpleTransactions</code>.
	 * @author Strašlivý Uragán
     * @version 1.0
     * @since 1.0
	 */
	public enum SortOrder {
		/** No particular order. Natural order from the database.
         */
		UNSORTED,
		/** Sorted by the ids of payers.
         */
		SORTED_BY_PAYER_ID,
		/** Sorted by the names of payers.
         */
		SORTED_BY_PAYER_NAME,
		/** Sorted by the ids of payees (receivers of money).
         */
		SORTED_BY_RECEIVER_ID,
		/** Sorted by the names of payees (receivers of money).
         */
		SORTED_BY_RECEIVER_NAME
	};

	/** Id of the event for which the suggestion should be computed. */
	long myEventId = -1;
	/** Simple transactions which together form the suggestion. */
	Vector<SimpleTransaction> simpleTransactions = null;
	/** Object representing the connection to the database. */
	GCDatabase db = null;
	/** Layout of the main suggestion table. */
	TableLayout sctable = null;
	/** Inflater used to inflate layouts and other UI elements. */
	LayoutInflater inflater;
	/** Participants hashed by their ids. */
	HashMap<Long, ClearingPerson> participants;
	/**
	 * Row widgets with currency names. Hashed by the names of currencies. These
	 * are reused, that is why we store them in this hashmap.
	 */
	HashMap<String, TableRow> currencyRows;
	/**
	 * Event in which suggestion is computed.
	 */
	ClearingEvent myEvent = null;
	/**
	 * Application object for accessing global preferences and properties.
	 */
	GroupClearingApplication myApp = GroupClearingApplication.getInstance();
	/**
	 * Header row of the table with suggestions. Stored for possible reusing.
	 */
	TableRow headerRow = null;
	/**
	 * By what element the records are sorted. Can be sorted by payee/payer
	 * id/name. Describes the sort order of <code>simpleTransactions</code>.
	 */
	SortOrder sortedBy = SortOrder.UNSORTED;

	/** Tag for passing event id parameter to this activity. For use
     * with <code>Intent.putExtra()</code>.
	 */
	public final static String EVENT_ID_PARAM_TAG = "cz.su.GroupClearing.EventId";

	/** Comparator for sorting <code>simpleTransactions</code> by payer id.
	 */
	Comparator<SimpleTransaction> payerIdComparator = new Comparator<SimpleTransaction>() {
		@Override
		public int compare(SimpleTransaction A, SimpleTransaction B) {
			if (!A.getCurrency().equals(B.getCurrency())) {
				return A.getCurrency().toString()
						.compareTo(B.getCurrency().toString());
			}
			return (int) (A.getPayerId() - B.getPayerId());
		}
	};

	/** Comparator for sorting <code>simpleTransactions</code> by payer name.
	 */
	Comparator<SimpleTransaction> payerNameComparator = new Comparator<SimpleTransaction>() {
		@Override
		public int compare(SimpleTransaction A, SimpleTransaction B) {
			if (!A.getCurrency().equals(B.getCurrency())) {
				return A.getCurrency().toString()
						.compareTo(B.getCurrency().toString());
			}
			ClearingPerson payerA = participants.get(Long.valueOf(A
					.getPayerId()));
			ClearingPerson payerB = participants.get(Long.valueOf(B
					.getPayerId()));
			ClearingPerson receiverA = participants.get(Long.valueOf(A
					.getReceiverId()));
			ClearingPerson receiverB = participants.get(Long.valueOf(B
					.getReceiverId()));
			int comp = payerA.getName().compareTo(payerB.getName());
			if (comp == 0) {
				comp = (int) (payerA.getId() - payerB.getId());
				if (comp == 0) {
					comp = receiverA.getName().compareTo(receiverB.getName());
					if (comp == 0) {
						comp = (int) (receiverA.getId() - receiverB.getId());
					}
				}
			}
			return comp;
		}
	};

	/** Comparator for sorting <code>simpleTransactions</code> by payee/receiver id.
	 */
	Comparator<SimpleTransaction> receiverIdComparator = new Comparator<SimpleTransaction>() {
		@Override
		public int compare(SimpleTransaction A, SimpleTransaction B) {
			if (!A.getCurrency().equals(B.getCurrency())) {
				return A.getCurrency().toString()
						.compareTo(B.getCurrency().toString());
			}
			return (int) (A.getReceiverId() - B.getReceiverId());
		}
	};

	/** Comparator for sorting <code>simpleTransactions</code> by payee/receiver name.
	 */
	Comparator<SimpleTransaction> receiverNameComparator = new Comparator<SimpleTransaction>() {
		@Override
		public int compare(SimpleTransaction A, SimpleTransaction B) {
			if (!A.getCurrency().equals(B.getCurrency())) {
				return A.getCurrency().toString()
						.compareTo(B.getCurrency().toString());
			}
			ClearingPerson payerA = participants.get(Long.valueOf(A
					.getPayerId()));
			ClearingPerson payerB = participants.get(Long.valueOf(B
					.getPayerId()));
			ClearingPerson receiverA = participants.get(Long.valueOf(A
					.getReceiverId()));
			ClearingPerson receiverB = participants.get(Long.valueOf(B
					.getReceiverId()));
			int comp = receiverA.getName().compareTo(receiverB.getName());
			if (comp == 0) {
				comp = (int) (receiverA.getId() - receiverB.getId());
				if (comp == 0) {
					comp = payerA.getName().compareTo(payerB.getName());
					if (comp == 0) {
						comp = (int) (payerA.getId() - payerB.getId());
					}
				}
			}
			return comp;
		}
	};

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggest_clearance);
		myEventId = getIntent().getLongExtra(EVENT_ID_PARAM_TAG, -1);
		sctable = (TableLayout) findViewById(R.id.sctable);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		simpleTransactions = new Vector<SimpleTransaction>();
		currencyRows = new HashMap<String, TableRow>();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (db == null) {
			db = new GCDatabase(this);
		}
		myEvent = db.readEventWithId(myEventId);
		if (myApp.getConvertToEventCurrency()) {
			computeClearanceSuggestion();
		} else {
			computeClearPerCurrencySuggestion();
		}
		readParticipants();
		sortByPayerName();
		fillTableRows();
	}

    /** Computes clearance suggestion for values in single given
     * currency. This method computes suggestion only for values in
     * given currency. It creates the corresponding
     * <code>SimpleTransaction</code>
     * objects, and adds them to <code>simpleTransactions</code>
     * array. The values themselves are passed using <code>values</code> parameter.
     * 
     * The algorithm for computing the clearance suggestion is as
     * follows.
     *
     * <ol>
     * <li>Divide values to positive (<code>positiveValues</code>) and
     * negative (<code>negativeValues</code>) priority queues sort by
     * the absolute value.</li>
     * <li>While both <code>positiveValues</code> and
     * <code>negativeValues</code> queues are nonempty.
     * <ol>
     * <li>Take biggest positive value and negative value (in absolute
     * value). Take the smaller one and create
     * <code>SimpleTransaction</code> for transferring this amount
     * from payer to the receiver (given by <code>ParticipantValue</code> object).
     * </li>
     * <li>
     * The rest of the opposite value is stored in the appropriate
     * queue again.
     * </li>
     * </ol>
     * </li>
     * <li>
     * The remaining nonempty queue is emptied and corresponding
     * <code>SimpleTransaction</code> objects are added, receiver or
     * payer is -1 in this case (depending on which queue is nonempty).
     * </li>
     * </ol>
     *
	 * @param currency Currency in which transactions should be
     * created.
	 * @param values Values of participants for which the suggestion
     * should be computed.
	 */
	void computeClearanceWithCurrency(Currency currency,
			Vector<ParticipantValue> values) {
		Comparator<ParticipantValue> positiveComparator = new Comparator<ParticipantValue>() {
			@Override
			public int compare(ParticipantValue A, ParticipantValue B) {
				return B.getValue().compareTo(A.getValue());
			}
		};
		Comparator<ParticipantValue> negativeComparator = new Comparator<ParticipantValue>() {
			@Override
			public int compare(ParticipantValue A, ParticipantValue B) {
				return A.getValue().compareTo(B.getValue());
			}
		};
		PriorityQueue<ParticipantValue> positiveValues = new PriorityQueue<ParticipantValue>(
				values.size(), positiveComparator);
		PriorityQueue<ParticipantValue> negativeValues = new PriorityQueue<ParticipantValue>(
				values.size(), negativeComparator);
		for (ParticipantValue valueInfo : values) {
			if (valueInfo.getValue().signum() > 0) {
				positiveValues.offer(valueInfo);
			} else if (valueInfo.getValue().signum() < 0) {
				negativeValues.offer(valueInfo);
			}
		}
		while (!negativeValues.isEmpty() && !positiveValues.isEmpty()) {
			ParticipantValue positiveValue = positiveValues.poll();
			ParticipantValue negativeValue = negativeValues.poll();
			BigDecimal value = BigDecimal.ZERO;
			BigDecimal negValueAbs = negativeValue.getValue().abs();
			if (positiveValue.getValue().compareTo(negValueAbs) > 0) {
				value = negValueAbs;
				positiveValue.setValue(positiveValue.getValue().add(
						negativeValue.getValue()));
				positiveValues.offer(positiveValue);
			} else if (positiveValue.getValue().compareTo(negValueAbs) == 0) {
				value = positiveValue.getValue();
			} else /* < */{
				value = positiveValue.getValue();
				negativeValue.setValue(negativeValue.getValue().add(
						positiveValue.getValue()));
				negativeValues.offer(negativeValue);
			}
			SimpleTransaction simpleTrans = new SimpleTransaction(
					positiveValue.getId(), negativeValue.getId(), value,
					currency);
			simpleTransactions.add(simpleTrans);
		}
		while (!negativeValues.isEmpty()) {
			ParticipantValue negativeValue = negativeValues.poll();
			SimpleTransaction simpleTrans = new SimpleTransaction(-1,
					negativeValue.getId(), negativeValue.getValue().abs(),
					currency);
			simpleTransactions.add(simpleTrans);
		}
		while (!positiveValues.isEmpty()) {
			ParticipantValue positiveValue = positiveValues.poll();
			SimpleTransaction simpleTrans = new SimpleTransaction(-1,
					positiveValue.getId(), positiveValue.getValue(), currency);
			simpleTransactions.add(simpleTrans);
		}
	}

    /** Computes all suggestions for all currencies. Relies on
     * <code>computeClearanceWithCurrency(Currency,Vector<ParticipantValue>)</code>,
     * it just calls this method for all currencies and their
     * corresponding participant values mappings.
	 */
	void computeClearPerCurrencySuggestion() {
		SortedMap<String, Vector<ParticipantValue>> values = db
				.readEventParticipantValuesPerCurrency(myEventId);
		simpleTransactions.clear();
		for (Map.Entry<String, Vector<ParticipantValue>> entry : values
				.entrySet()) {
			computeClearanceWithCurrency(Currency.getInstance(entry.getKey()),
					entry.getValue());
		}
	}

    /** Computes clearance suggestion in case of a single event
     * currency. It just invokes
     * <code>computeClearanceWithCurrency(Currency,Vector<ParticipantValue>)</code>
     * with <code>myEvent.getDefaultCurrency()</code> as currency.
	 */
	void computeClearanceSuggestion() {
		Vector<ParticipantValue> values = db
				.readEventParticipantValues(myEventId);
		simpleTransactions.clear();
		computeClearanceWithCurrency(myEvent.getDefaultCurrency(), values);
	}

    /** Reads all participants from the database. Populates
     * <code>persons</code> array and <code>participants</code>
     * <code>HashMap</code>.
	 */
	void readParticipants() {
		Vector<ClearingPerson> persons = db.readParticipantsOfEvent(myEventId,
				GCDatabase.ComputeBalance.COMPUTE_CUMULATIVE);
		if (participants == null) {
			participants = new HashMap<Long, ClearingPerson>(persons.size());
		} else {
			participants.clear();
		}
		for (ClearingPerson person : persons) {
			Long id = Long.valueOf(person.getId());
			participants.put(id, person);
		}
	}

	/** Returns participant with given <code>id</code> as it is stored in
     * <code>participants</code> <code>HashMap</code>.
	 * @param id Id of participant to be returned.
	 * @return Participant with given <code>id</code>.
	 */
	ClearingPerson getParticipant(long id) {
		ClearingPerson participant = participants.get(Long.valueOf(id));
		if (participant == null) {
			participant = new ClearingPerson(-1, "???", BigDecimal.ZERO, "");
		}
		return participant;
	}

	/** Returns widget with header row.
	 * @return <code>TableRow</code> widget with header row.
	 */
	TableRow getHeaderRow() {
		if (headerRow == null) {
			headerRow = (TableRow) inflater.inflate(
					R.layout.sctable_header_row, null);
		}
		return headerRow;
	}

	/** Returns widget with currency header.
	 * @param currency Currency for which the header row should be
     * returned.
	 * @return Row of the currency given in parameter
     * <code>currency</code>.
	 */
	TableRow getCurrencyRow(Currency currency) {
		TableRow row = currencyRows.get(currency.toString());
		if (row == null) {
			row = (TableRow) inflater.inflate(R.layout.sctable_currency_row,
					null);
			TextView currencyText = (TextView) row
					.findViewById(R.id.sctable_currency_label);
			currencyText.setText(getResources().getString(
					R.string.sctable_currency)
					+ " " + currency.toString());
		}
		return row;
	}

	/** Creates new row for simple transaction specified in
     * parameters.
	 * @param index Index of <code>SimpleTransaction</code> object
     * within <code>simpleTransactions</code> array.
	 * @param even If the row is even (even and odd row has slightly
     * different style).
	 * @return Newly created row for specified simple transaction with
     * specified style.
	 */
	TableRow createRow(int index, boolean even) {
		SimpleTransaction transaction = simpleTransactions.get(index);
		TableRow row = null;
		if (even) {
			row = (TableRow) inflater.inflate(R.layout.sctable_even_row, null);
		} else {
			row = (TableRow) inflater.inflate(R.layout.sctable_odd_row, null);
		}
		ClearingPerson payer = getParticipant(transaction.getPayerId());
		TextView payerText = (TextView) row
				.findViewById(R.id.sctable_row_payer);
		payerText.setText(payer.getName());
		ClearingPerson receiver = getParticipant(transaction.getReceiverId());
		TextView receiverText = (TextView) row
				.findViewById(R.id.sctable_row_receiver);
		receiverText.setText(receiver.getName());
		TextView amount = (TextView) row.findViewById(R.id.sctable_row_amount);
		amount.setText(myApp.formatCurrencyValueWithSymbol(
				transaction.getValue(), transaction.getCurrency()));
		return row;
	}

	/** Fills table row widgets with data stored in
     * <code>simpleTransactions</code>. The rows are added in the same
     * order as they are stored in <code>simpleTransactions</code>
     * array. If <code>convertToEventCurrency</code> preference value
     * is false, then at the beginning and at the places where
     * currency changes between two consecutive transactions, currency
     * header row is inserted. 
	 */
	void fillTableRows() {
		sctable.removeAllViews();
		boolean even = false;
		long oldId = -1;
		sctable.addView(getHeaderRow());
		Currency oldCurrency = null;
		for (int index = 0; index < simpleTransactions.size(); ++index) {
			SimpleTransaction transaction = simpleTransactions.get(index);
			if (!myApp.getConvertToEventCurrency()
					&& (oldCurrency == null || !oldCurrency.equals(transaction
							.getCurrency()))) {
				oldCurrency = transaction.getCurrency();
				sctable.addView(getCurrencyRow(oldCurrency));
				oldId = -1;
				even = false;
			}
			switch (sortedBy) {
				case UNSORTED :
					even = !even;
					break;
				case SORTED_BY_PAYER_ID :
				case SORTED_BY_PAYER_NAME : {
					long newId = transaction.getPayerId();
					if (newId != oldId) {
						even = !even;
						oldId = newId;
					}
					break;
				}
				case SORTED_BY_RECEIVER_ID :
				case SORTED_BY_RECEIVER_NAME : {
					long newId = transaction.getReceiverId();
					if (newId != oldId) {
						even = !even;
						oldId = newId;
					}
					break;
				}

			}
			TableRow row = createRow(index, even);
			sctable.addView(row);
		}
	}

	/** Sorts <code>simpleTransactions</code> by the id of the payer.
	 */
	void sortByPayerId() {
		Collections.sort(simpleTransactions, payerIdComparator);
		sortedBy = SortOrder.SORTED_BY_PAYER_ID;
	}

	/** Sorts <code>simpleTransactions</code> by the name of the payer.
	 */
	void sortByPayerName() {
		Collections.sort(simpleTransactions, payerNameComparator);
		sortedBy = SortOrder.SORTED_BY_PAYER_NAME;
	}

	/** Sorts <code>simpleTransactions</code> by the id of the
     * payee/receiver.
	 * 
	 */
	void sortByReceiverId() {
		Collections.sort(simpleTransactions, receiverIdComparator);
		sortedBy = SortOrder.SORTED_BY_RECEIVER_ID;
	}

	/** Sorts <code>simpleTransactions</code> by the name of the
     * payee/receiver.
	 */
	void sortByReceiverName() {
		Collections.sort(simpleTransactions, receiverNameComparator);
		sortedBy = SortOrder.SORTED_BY_RECEIVER_NAME;
	}

	/** Reacts to event when button for creating transactions was
     * clicked. This function creates the full transactions
     * following the clearing suggestion. These transactions are
     * aggregated by the receivers. I.e. multiple simple transactions
     * are joined to form a general transaction with possible multiple
     * payers but a single payee/receiver.
	 * @param v The view of button which was clicked.
	 */
	public void onCreateTransactionsClicked(View v) {
		sortByPayerId();
		try {
			long prevPayerId = -1;
			Currency prevCurrency = null;
			ClearingTransaction transaction = null;
			BigDecimal value = BigDecimal.ZERO;
			for (int index = 0; index < simpleTransactions.size(); ++index) {
				SimpleTransaction trans = simpleTransactions.get(index);
				long payerId = trans.getPayerId();
				long receiverId = trans.getReceiverId();
				if (payerId < 0 || receiverId < 0) {
					continue;
				}
				if (transaction == null || payerId != prevPayerId
						|| prevCurrency == null
						|| !prevCurrency.equals(trans.getCurrency())) {
					if (transaction != null) {
						transaction.recomputeAndSaveChanges(db);
					}
					transaction = db.createNewTransaction(myEventId);
					ClearingPerson payer = participants.get(Long
							.valueOf(payerId));
					if (myApp.getConvertToEventCurrency()) {
						transaction.setName(getResources().getString(
								R.string.sc_transaction_name)
								+ " " + payer.getName());
					} else {
						transaction.setName(getResources().getString(
								R.string.sc_transaction_name)
								+ " "
								+ trans.getCurrency().toString()
								+ ": "
								+ payer.getName());
					}
					transaction.setSplitEvenly(false);
					transaction.setReceiverId(payerId);
					transaction.setCurrency(trans.getCurrency());
					transaction.setRate(db.getDefaultRate(trans.getCurrency(),
							myEvent.getDefaultCurrency()));
					db.updateTransaction(transaction);
					prevPayerId = payerId;
					value = BigDecimal.ZERO;
				}
				value = value.add(trans.getValue());
				transaction.setParticipantValue(receiverId, trans.getValue()
						.negate());
				db.updateTransactionParticipantValue(transaction, receiverId);
				transaction.setParticipantValue(payerId, value);
				db.updateTransactionParticipantValue(transaction, payerId);
			}
			if (transaction != null) {
				transaction.recomputeAndSaveChanges(db);
			}
		} catch (GCEventDoesNotExistException e) {
			// TODO: Warn the user???
		}
		finish();
	}
}
