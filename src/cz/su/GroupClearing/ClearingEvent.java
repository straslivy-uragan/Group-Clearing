/**
 * Definition of class with clearing event.
 */
package cz.su.GroupClearing;

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;
import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.text.DateFormat;

import cz.su.GroupClearing.ClearingPerson;
import cz.su.GroupClearing.ClearingTransaction;

/**
 * @author su
 */
/**
 * Class describing a clearing event. An event is basically a time spanned event
 * with start and (possible) finish, it contains usually several payments which
 * are split between several persons. All these properties are part of data
 * stored in an object of this class.
 */
public class ClearingEvent {
	private int id;
	/**
	 * Store persons as objects. This array should be sorted in the same way as
	 * columns in table with transactions. No, that is not a good idea,
    * it would mean, that when user would move a column, then it would
    * be necessary to move them in every transaction.
	 */
	private Vector<ClearingPerson> participants;
	/**
	 * Store id, or transactions? Probably transactions, it make no sense to
	 * have transactions separately. Event is the owner of the transaction.
	 */
	private Vector<ClearingTransaction> transactions;

	private GregorianCalendar startCalendar;
	private GregorianCalendar finishCalendar;
	private String name;
	private String note;
	private Currency defaultCurrency;

	private boolean modified;
	private File eventFile;

   private int maxParticipantId = 0;
   private int maxTransactionId = 0;

   public ClearingEvent(int id) {
		super();
		this.id = id;
		reset();
	}

	public void reset() {
		if (participants == null) {
			participants = new Vector<ClearingPerson>();
		} else {
			participants.clear();
		}
		if (transactions == null) {
			transactions = new Vector<ClearingTransaction>();
		} else {
			transactions.clear();
		}
		startCalendar = new GregorianCalendar();
		finishCalendar = new GregorianCalendar();
		defaultCurrency = Currency.getInstance(Locale.getDefault());
		modified = false;
		eventFile = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		if (name == null || !name.equals(newName)) {
			name = newName;
			modified = true;
		}
	}

	public String getNote() {
		return note;
	}

	public void setNote(String newNote) {
		if (note == null || !note.equals(newNote)) {
			note = newNote;
			modified = true;
		}
	}

	public Currency getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(Currency newDefaultCurrency) {
		if (defaultCurrency == null
				|| !defaultCurrency.equals(newDefaultCurrency)) {
			defaultCurrency = newDefaultCurrency;
			modified = true;
		}
	}

	public int getId() {
		return id;
	}
	public void setId(int newId) {
		id = newId;
	}
	
	public void setStartDate(int year, int month, int day) {
		if (year != getStartYear()) {
			startCalendar.set(Calendar.YEAR, year);
			modified = true;
		}
		if (month != getStartMonth()) {
			startCalendar.set(Calendar.MONTH, month);
			modified = true;
		}
		if (day != getStartDayOfMonth()) {
			startCalendar.set(Calendar.DAY_OF_MONTH, day);
			modified = true;
		}
	}
   public void setStartDate(Date newStartDate) {
      startCalendar.setTime(newStartDate);
   }

	public void setFinishDate(int year, int month, int day) {
		if (year != getFinishYear()) {
			finishCalendar.set(Calendar.YEAR, year);
			modified = true;
		}
		if (month != getFinishMonth()) {
			finishCalendar.set(Calendar.MONTH, month);
			modified = true;
		}
		if (day != getFinishDayOfMonth()) {
			finishCalendar.set(Calendar.DAY_OF_MONTH, day);
			modified = true;
		}
	}
   public void setFinishDate(Date newFinishDate) {
      finishCalendar.setTime(newFinishDate);
   }

	public Date getStartDate() {
		return startCalendar.getTime();
	}

	public Date getFinishDate() {
		return finishCalendar.getTime();
	}

	/**
	 * @return the startYear
	 */
	public int getStartYear() {
		return startCalendar.get(Calendar.YEAR);
	}

	/**
	 * @return the startMonthOfYear
	 */
	public int getStartMonth() {
		return startCalendar.get(Calendar.MONTH);
	}

	/**
	 * @return the startDayOfMonth
	 */
	public int getStartDayOfMonth() {
		return startCalendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @return the finishYear
	 */
	public int getFinishYear() {
		return finishCalendar.get(Calendar.YEAR);
	}

	/**
	 * @return the finishMonthOfYear
	 */
	public int getFinishMonth() {
		return finishCalendar.get(Calendar.MONTH);
	}

	/**
	 * @return the finishDayOfMonth
	 */
	public int getFinishDayOfMonth() {
		return finishCalendar.get(Calendar.DAY_OF_MONTH);
	}

   public int getNumberOfParticipants() {
      return participants.size();
   }

   public ClearingPerson getParticipant(int index) {
      if (index < participants.size())
      {
         return participants.get(index);
      }
      return null;
   }

   public void newParticipantWithName(String aName) {
      ClearingPerson participant = new ClearingPerson(++ maxParticipantId);
      participant.setName(aName);
      participants.add(participant);
      modified = true;
   }

   public void addParticipant(ClearingPerson participant) {
      participants.add(participant);
      modified = true;
   }

   /**@brief Removes participant from the list.
    *
    * No check, whether this participant is involved in some
    * transaction is performed.
    */
   public void removeParticipant(int id) {
      for (int position = 0; position < participants.size(); ++ position)
      {
         ClearingPerson participant = participants.get(position);
         if (participant.getId() == id) {
            participants.remove(position);
         }
      }
   }

   public int getNumberOfTransactions() {
      return transactions.size();
   }

   public ClearingTransaction getTransaction(int index) {
      if (index < transactions.size())
      {
         return transactions.get(index);
      }
      return null;
   }

   public void removeTransaction(int id) {
      for (int position = 0; position < transactions.size(); ++ position)
      {
         ClearingTransaction transaction = transactions.get(position);
         if (transaction.getId() == id) {
            transactions.remove(position);
         }
      }
   }

   public ClearingTransaction newTransaction() {
      ClearingTransaction aTransaction =
         new ClearingTransaction(++ maxTransactionId);
      transactions.add(aTransaction);
      modified = true;
      return aTransaction;
   }

	public boolean isModified() {
		return modified;
	}

	public void resetModified() {
		modified = false;
	}

   public void participantWasModified() {
      modified = true;
   }

	public void setFile(File newEventFile) {
		eventFile = newEventFile;
	}

	public File getFile() {
		return eventFile;
	}

	public String getSubtitle() {
		StringBuilder subtitle = new StringBuilder();

		if (startCalendar != null) {
			DateFormat df = DateFormat.getDateInstance();
			subtitle.append(df.format(startCalendar.getTime()));
			if (finishCalendar != null
					&& (getFinishYear() != getStartYear()
							|| getFinishMonth() != getStartMonth()
							|| getFinishDayOfMonth() != getStartDayOfMonth())) {
				subtitle.append(" -- ");
				subtitle.append(df.format(finishCalendar.getTime()));
			}
		} else {
			subtitle.append("?.?.????");
		}
      subtitle.append(' ');
		return subtitle.toString();
	}

	public void print(PrintStream outStream) {
		outStream.print("<event ");
		outStream.print("id=\"");
		outStream.print(id);
		outStream.println('"');
		outStream.print("name=\"");
		outStream.print(name);
		outStream.println('"');
		outStream.print("note=\"");
		outStream.print(note);
		outStream.println('"');
		outStream.print("defaultCurrency=\"");
		outStream.print(defaultCurrency.toString());
		outStream.println('"');
		outStream.print("startDate=\"");
		outStream.print(getStartDayOfMonth());
		outStream.print('.');
		outStream.print(getStartMonth());
		outStream.print('.');
		outStream.print(getStartYear());
		outStream.println('"');
		outStream.print("finishDate=\"");
		outStream.print(getFinishDayOfMonth());
		outStream.print('.');
		outStream.print(getFinishMonth());
		outStream.print('.');
		outStream.print(getFinishYear());
		outStream.println("\">");
		ClearingPerson person = null;
		Iterator<ClearingPerson> participantsEnumerator = participants
				.iterator();
		while (participantsEnumerator.hasNext()) {
			person = participantsEnumerator.next();
			person.print(outStream);
		}
		ClearingTransaction transaction = null;
		Iterator<ClearingTransaction> transactionsEnumerator = transactions
				.iterator();
		while (transactionsEnumerator.hasNext()) {
			transaction = transactionsEnumerator.next();
			transaction.print(outStream);
		}
		outStream.println("</event>");
	}

	public void printToFile()
      throws IOException
   {
		PrintStream outStream = null;
      try {
         outStream = new PrintStream(eventFile);
         print(outStream);
      }
      finally {
         if (outStream != null)
         {
            outStream.close();
         }
      }
	}

	@Override
	public String toString() {
		return name;
	}

}
