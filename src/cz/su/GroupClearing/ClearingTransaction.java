/**
 * 
 */
package cz.su.GroupClearing;

import java.util.Currency;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Locale;
import java.io.PrintStream;

/**
 * @author su
 * 
 */
public class ClearingTransaction {
	private int id;
	private String name;
	private GregorianCalendar calendarDate;
	private int amount;
	private Currency currency;
	private int receiverId;
	private int[] participantId;
	private int participantSize;
	private String note;

	private static final int INITIAL_PARTICIPANT_CAPACITY = 10;
	private static final int PARTICIPANT_CAPACITY_DELTA = 5;

	public ClearingTransaction(int newId) {
		super();
		id = newId;
		calendarDate = new GregorianCalendar();
		currency = Currency.getInstance(Locale.getDefault());
		amount = 0;
		receiverId = -1;
		participantId = new int[INITIAL_PARTICIPANT_CAPACITY];
		participantSize = 0;
		note = "";
	}

	public Date getDate() {
		return calendarDate.getTime();
	}

	public void setDate(int year, int month, int day) {
		calendarDate.set(Calendar.YEAR, year);
		calendarDate.set(Calendar.MONTH, month);
		calendarDate.set(Calendar.DAY_OF_MONTH, day);
	}

	public int getYear() {
		return calendarDate.get(Calendar.YEAR);
	}

	public int getMonth() {
		return calendarDate.get(Calendar.MONTH);
	}

	public int getDayOfMonth() {
		return calendarDate.get(Calendar.DAY_OF_MONTH);
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public int getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(int newReceiverId) {
		receiverId = newReceiverId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public int getId() {
		return id;
	}

	public int numberOfParticipants() {
		return participantSize;
	}

	public int getParticipantId(int index) {
		return participantId[index];
	}

	public void setParticipantId(int index, int id) {
		participantId[index] = id;
	}

	public void addParticipant(int id) {
		if (participantSize >= participantId.length) {
			int[] newParticipantId = new int[participantId.length
					+ PARTICIPANT_CAPACITY_DELTA];
			for (int i = 0; i < participantId.length; ++i) {
				newParticipantId[i] = participantId[i];
			}
			participantId = newParticipantId;
		}
		participantId[participantSize] = id;
		++participantSize;
	}

	@Override
	public String toString() {
		return "ClearingTransaction [id=" + id + ", calendarDate="
				+ calendarDate + ", amount=" + amount + ", currency="
				+ currency + ", receiver=" + receiverId + ", splittingPersons="
				+ participantId + ", note=" + note + "]";
	}

	public void print(PrintStream outStream) {
		outStream.print("transaction id=\"");
		outStream.print(id);
		outStream.println('"');
		outStream.print("name=\"");
		outStream.print(name);
		outStream.println('"');
		outStream.print("date=\"");
		outStream.print(getDayOfMonth());
		outStream.print('.');
		outStream.print(getMonth());
		outStream.print('.');
		outStream.print(getYear());
		outStream.println('"');
		outStream.print("amount=\"");
		outStream.print(amount);
		outStream.print(currency);
		outStream.println('"');
		outStream.print("receiver=\"");
		outStream.print(receiverId);
		outStream.println('"');
		outStream.print("participants=\"");
		if (participantSize > 0) {
			outStream.print(participantId[0]);
			for (int i = 1; i < participantSize; ++i) {
				outStream.print(',');
				outStream.print(participantId[i]);
			}
		}
		outStream.println('"');
		outStream.print("note=\"");
		outStream.println(note);
		outStream.println("\" >");
	}

}
