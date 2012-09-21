/**
 * File containing definition of a class describing persons
 * participating on clearing events.
 */
package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.SortedMap;

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
   private final long id;
   private String name;
   private String note;
   private BigDecimal balance = BigDecimal.ZERO;
   private SortedMap<String, BigDecimal> allBalances = null;

   public BigDecimal getBalance() {
       if (balance == null) {
           balance = BigDecimal.ZERO;
       }
      return balance;
   }
   public void setBalance(BigDecimal balance) {
      this.balance = balance;
   }
   
   public void setAllBalances(SortedMap<String, BigDecimal> allBalances) {
       this.allBalances = allBalances;
   }

   public SortedMap<String, BigDecimal> getAllBalances() {
       return allBalances;
   }
   
   /**
    * @param id
    */
   public ClearingPerson(long id) {
      super();
      this.id = id;
      this.name = "";
      this.note = "";
      this.balance = BigDecimal.ZERO;
   }
   
   /**
    * @param id
    * @param name
    * @param note
    * @param balance
    */
   public ClearingPerson(long id, String name,
         BigDecimal balance, String note) {
      super();
      this.id = id;
      this.name = (name != null ? name : "");
      this.note = (note != null ? note : "");
      this.balance = balance;
   }

   public ClearingPerson(long id, String name,
         SortedMap<String, BigDecimal> allBalances, String note) {
      super();
      this.id = id;
      this.name = (name != null ? name : "");
      this.note = (note != null ? note : "");
      this.allBalances = allBalances;
      balance = BigDecimal.ZERO;
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
   public long getId() {
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
         return name;
      }

   @Override
public int compareTo(ClearingPerson otherPerson) {
      if (otherPerson == null)
      {
         return 1;
      }
      return name.compareTo(otherPerson.getName());
   }
}
