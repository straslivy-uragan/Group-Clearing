/**
 * File containing definition of a class describing persons
 * participating on clearing events.
 */
package cz.su.GroupClearing;

import java.io.PrintStream;

/**
 * @author su
 * A class describing a person participating on a clearing event. A
 * person is given by it name, id and possible note. Name is a single
 * string identifying the person in question, this is not a person
 * managing application, so one string should be enough. A person is
 * part of a transaction, even if we would add support for getting
 * person info from Contacts, we would use ClearingPerson as an
 * intermediate proxy. We thus store event related personal info here,
 * too. Among other it concerns current balance
 */
public class ClearingPerson implements Comparable<ClearingPerson> {
   private int id;
   private String name;
   private String note;
   private int balance;

   public int getBalance() {
      return balance;
   }
   public void setBalance(int balance) {
      this.balance = balance;
   }
   /**
    * @param id
    */
   public ClearingPerson(int id) {
      super();
      this.id = id;
      this.name = "";
      this.note = "";
      this.balance = 0;
   }
   
   /**
    * @param id
    * @param name
    * @param note
    * @param balance
    */
   public ClearingPerson(int id, String name,
         String note, int balance) {
      super();
      this.id = id;
      this.name = (name != null ? name : "");
      this.note = (note != null ? note : "");
      this.balance = balance;
   }
   /**
    * @return the note
    */
   public String getNote() {
      return note;
   }
   /**
    * @param note the note to set
    */
   public void setNote(String note) {
      this.note = note;
   }
   /**
    * @return the id
    */
   public int getId() {
      return id;
   }
   /**
    * @return the name
    */
   public String getName() {
      return name;
   }
   /**
    * @param name the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   @Override
      public String toString() {
         return "ClearingPerson [id=" + id
            + ", name=" + name
            + ", note=" + note
            + ", balance=" + balance + "]";
      }

   public void print(PrintStream outStream) {
      outStream.print("<participant id=\"");
      outStream.print(id);
      outStream.println('"');
      outStream.print("name=\"");
      outStream.print(name);
      outStream.println('"');
      outStream.print("balance=\"");
      outStream.print(balance);
      outStream.println('"');
      outStream.print("note=\"");
      outStream.print(note);
      outStream.println("\" >");
   }

   public int compareTo(ClearingPerson otherPerson) {
      if (otherPerson == null)
      {
         return 1;
      }
      return name.compareTo(otherPerson.getName());
   }
}
