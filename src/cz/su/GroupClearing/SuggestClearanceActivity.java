package cz.su.GroupClearing;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Vector;
import java.math.BigDecimal;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SuggestClearanceActivity extends Activity {

	long myEventId = -1;
	Vector<SimpleTransaction> simpleTransactions = null;
	GCDatabase db = null;
	TableLayout sctable = null;
	LayoutInflater inflater;
	HashMap<Long, ClearingPerson> participants;
	ClearingEvent myEvent = null;
	GroupClearingApplication myApp = GroupClearingApplication.getInstance();
	TableRow headerRow = null;
	int sortedBy = UNSORTED;

	public static final int UNSORTED = 0;
	public static final int SORTED_BY_PAYER_ID = 1;
	public static final int SORTED_BY_PAYER_NAME = 2;
	public static final int SORTED_BY_RECEIVER_ID = 3;
	public static final int SORTED_BY_RECEIVER_NAME = 4;

	Comparator<SimpleTransaction> payerIdComparator = new Comparator<SimpleTransaction>() {
		@Override
		public int compare(SimpleTransaction A, SimpleTransaction B) {
			return (int) (A.getPayerId() - B.getPayerId());
		}
	};

	Comparator<SimpleTransaction> payerNameComparator = new Comparator<SimpleTransaction>() {
		@Override
		public int compare(SimpleTransaction A, SimpleTransaction B) {
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

	Comparator<SimpleTransaction> receiverIdComparator = new Comparator<SimpleTransaction>() {
		@Override
		public int compare(SimpleTransaction A, SimpleTransaction B) {
			return (int) (A.getReceiverId() - B.getReceiverId());
		}
	};

	Comparator<SimpleTransaction> receiverNameComparator = new Comparator<SimpleTransaction>() {
		@Override
		public int compare(SimpleTransaction A, SimpleTransaction B) {
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggest_clearance);
		myEventId = getIntent().getLongExtra("cz.su.GroupClearing.EventId", -1);
		sctable = (TableLayout) findViewById(R.id.sctable);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (db == null) {
			db = new GCDatabase(this);
		}
		myEvent = db.readEventWithId(myEventId);
		computeClearanceSuggestion();
		readParticipants();
		sortByPayerName();
		fillTableRows();
	}

	void computeClearanceSuggestion() {
		Vector<ParticipantValue> values = db
				.readEventParticipantValues(myEventId);
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
		simpleTransactions = new Vector<SimpleTransaction>();
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
					positiveValue.getId(), negativeValue.getId(), value);
			simpleTransactions.add(simpleTrans);
		}
		while (!negativeValues.isEmpty()) {
			ParticipantValue negativeValue = negativeValues.poll();
			SimpleTransaction simpleTrans = new SimpleTransaction(-1,
					negativeValue.getId(), negativeValue.getValue().abs());
			simpleTransactions.add(simpleTrans);
		}
		while (!positiveValues.isEmpty()) {
			ParticipantValue positiveValue = positiveValues.poll();
			SimpleTransaction simpleTrans = new SimpleTransaction(-1,
					positiveValue.getId(), positiveValue.getValue());
			simpleTransactions.add(simpleTrans);
		}
	}

	void readParticipants() {
		Vector<ClearingPerson> persons = db.readParticipantsOfEvent(myEventId);
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

	ClearingPerson getParticipant(long id) {
		ClearingPerson participant = participants.get(Long.valueOf(id));
		if (participant == null) {
			participant = new ClearingPerson(-1, "???", BigDecimal.ZERO, "");
		}
		return participant;
	}

	TableRow getHeaderRow() {
		if (headerRow == null) {
			headerRow = (TableRow) inflater.inflate(
					R.layout.sctable_header_row, null);
		}
		return headerRow;
	}

	TableRow createRow(int index, boolean even) {
		SimpleTransaction transaction = simpleTransactions.get(index);
		TableRow row = null;
		if (even) {
			row = (TableRow) inflater.inflate(R.layout.sctable_even_row, null);
		} else {
			row = (TableRow) inflater.inflate(R.layout.sctable_odd_row, null);
		}
		ClearingPerson payer = getParticipant(transaction.getPayerId());
		TextView payer_text = (TextView) row
				.findViewById(R.id.sctable_row_payer);
		payer_text.setText(payer.getName());
		ClearingPerson receiver = getParticipant(transaction.getReceiverId());
		TextView receiver_text = (TextView) row
				.findViewById(R.id.sctable_row_receiver);
		receiver_text.setText(receiver.getName());
		TextView amount = (TextView) row.findViewById(R.id.sctable_row_amount);
		amount.setText(myApp.formatCurrencyValueWithSymbol(
				transaction.getValue(), myEvent.getDefaultCurrency()));
		return row;
	}

	void fillTableRows() {
		sctable.removeAllViews();
		boolean even = false;
		long oldId = -1;
		sctable.addView(getHeaderRow());
		for (int index = 0; index < simpleTransactions.size(); ++index) {
			switch (sortedBy) {
				case UNSORTED :
					even = !even;
					break;
				case SORTED_BY_PAYER_ID :
				case SORTED_BY_PAYER_NAME : {
					long newId = simpleTransactions.get(index).getPayerId();
					if (newId != oldId) {
						even = !even;
						oldId = newId;
					}
					break;
				}
				case SORTED_BY_RECEIVER_ID :
				case SORTED_BY_RECEIVER_NAME : {
					long newId = simpleTransactions.get(index).getReceiverId();
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

	void sortByPayerId() {
		Collections.sort(simpleTransactions, payerIdComparator);
		sortedBy = SORTED_BY_PAYER_ID;
	}

	void sortByPayerName() {
		Collections.sort(simpleTransactions, payerNameComparator);
		sortedBy = SORTED_BY_PAYER_NAME;
	}

	void sortByReceiverId() {
		Collections.sort(simpleTransactions, receiverIdComparator);
		sortedBy = SORTED_BY_RECEIVER_ID;
	}

	void sortByReceiverName() {
		Collections.sort(simpleTransactions, receiverNameComparator);
		sortedBy = SORTED_BY_RECEIVER_NAME;
	}

	public void onCreateTransactionsClicked(View v) {
		sortByPayerId();
		try {
			long prevPayerId = -1;
			ClearingTransaction transaction = null;
			BigDecimal value = BigDecimal.ZERO;
			for (int index = 0; index < simpleTransactions.size(); ++index) {
				SimpleTransaction trans = simpleTransactions.get(index);
				long payerId = trans.getPayerId();
				long receiverId = trans.getReceiverId();
				if (payerId < 0 || receiverId < 0) {
					continue;
				}
				if (transaction == null || payerId != prevPayerId) {
					if (transaction != null) {
						transaction.recomputeAndSaveChanges(db);
					}
					transaction = db.createNewTransaction(myEventId);
					ClearingPerson payer = participants.get(Long
							.valueOf(payerId));
					transaction.setName(getResources().getString(
							R.string.sc_transaction_name)
							+ " " + payer.getName());
					db.updateTransactionName(transaction);
					transaction.setSplitEvenly(false);
					db.updateTransactionSplitEvenly(transaction);
					transaction.setReceiverId(payerId);
					db.updateTransactionReceiverId(transaction);
					prevPayerId = payerId;
					value = BigDecimal.ZERO;
				}
				value = value.add(trans.getValue());
				transaction.setAndSaveParticipantValue(receiverId,
						trans.getValue().negate(), db);
				transaction.setAndSaveParticipantValue(payerId, value, db);
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
