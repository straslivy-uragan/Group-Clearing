/**
 * File containing definition of a class describing persons
 * participating on clearing events.
 */
package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.SortedMap;

/**
 * A class describing a person participating on a clearing event. A
 * person is given by its name, id and possible note. Name is a single
 * string identifying the person in question, this is not a person
 * managing application, so one string should be enough. Right now, in
 * database person is always participating in exactly one event. If
 * user desires to have a person participating in more events, in each
 * event such a person has to be created. This object also allows to
 * store balance of the person in two possible forms, either as a one
 * <code>BigDecimal</code> value, or as a map, which maps a value to
 * a string with name of a currency. The latter form is used in case
 * multiple currencies are supported and they are treated separately.
 *
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class ClearingPerson implements Comparable<ClearingPerson> {
    /** Id number identifying this person.
     */
   private final long id;
   /** The string with name of this person.
    */
   private String name;
   /** The string with note associated with this person.
    */
   private String note;
   /** Balance of this person. This is only one value in case all
    * currencies are being converted to a single one or multiple
    * currencies are not supported in the event.
    */
   private BigDecimal balance = BigDecimal.ZERO;
   /**
    * All balances of this person in different currencies. All balance
    * values of this person in all possible currencies used in the
    * event transactions.
    */
   private SortedMap<String, BigDecimal> allBalances = null;

  
   /** Constructor which constructs the person object with given id.
    * Other than <code>id</code>, the remaining values are reset to
    * defaults, strings to empty strings, balance to 0.
    * <code>allBalances</code> to
    * <code>null</code>.
    * @param id
    */
   public ClearingPerson(long id) {
      super();
      this.id = id;
      this.name = "";
      this.note = "";
      this.balance = BigDecimal.ZERO;
   }

   /** Constructor which constructs the person object with given
    * values. This version initializes person with a single balance
    * value.
    * @param id Id number identifying person being created.
    * @param name Name of the person.
    * @param note Note associated with the person.
    * @param balance Balance of the person.
    */
   public ClearingPerson(long id, String name,
           BigDecimal balance, String note) {
       super();
       this.id = id;
       this.name = (name != null ? name : "");
       this.note = (note != null ? note : "");
       this.balance = balance;
   }

   /** Constructor which constructs the person object with given
    * values. This version initializes person with multiple balance
    * values in multiple currencies.
    * @param id Id number identifying person being created.
    * @param name Name of the person.
    * @param note Note associated with the person.
    * @param allBalances All balances in different currencies.
    */
   public ClearingPerson(long id, String name, String note,
           SortedMap<String, BigDecimal> allBalances) {
       super();
       this.id = id;
       this.name = (name != null ? name : "");
       this.note = (note != null ? note : "");
       this.allBalances = allBalances;
       balance = BigDecimal.ZERO;
   }

   /** Returns the note associated with this person.
    * @return The note associated with this person.
    */
   public String getNote() {
      return note;
   }
   /** Sets the note associated with this person.
    * @param note New note associated with this person.
    */
   public void setNote(String note) {
      this.note = note;
   }
   /** Returns id number identifying this person.
    *
    * @return The id number identifying this person.
    */
   public long getId() {
      return id;
   }
   /** Returns the name of this person.
    * @return The name of this person.
    */
   public String getName() {
      return name;
   }
   /** Sets the new name of this person.
    * @param name New name of the person.
    */
   public void setName(String name) {
      this.name = name;
   }

   /** Returns the single valued version of the balance of this
    * person.
    * @return Single valued balance of this person.
    */
   public BigDecimal getBalance() {
       if (balance == null) {
           balance = BigDecimal.ZERO;
       }
       return balance;
   }
   /** Sets the single valued balance of this person.
    *
    * @param balance New single valued balance of this person.
    */
   public void setBalance(BigDecimal balance) {
      this.balance = balance;
   }
   
   /** Set all balances for different currencies. The balances are
    * given as a map mapping names of currencies to corresonding
    * balance values.
    *
    * @param allBalances New map of names of currencies to
    * corresponding balance values.
    */
   public void setAllBalances(SortedMap<String, BigDecimal> allBalances) {
       this.allBalances = allBalances;
   }

   /** Returns all different balances in all currencies.
    * @return The map mapping names of currencies to corresponding 
    * balance values.
    */
   public SortedMap<String, BigDecimal> getAllBalances() {
       return allBalances;
   }
  
   /** Returns the string representation of this object.
    * In case of <code>ClearingPerson</code>, the string
    * representation is the name of this person.
    *
    * @return The string representation of the object, in this case
    * the name of the person.
    */
   @Override
       public String toString() {
           return name;
       }

   /** Compares this person to another one. The only field which is
    * compared is the name of the person, id is not considered. This
    * is for sorting by name.
    *
    * @param otherPerson Other person to compare to.
    *
    * @return Comparison result, 1 if this person should go before
    * <code>otherPerson</code>, 0, if they are equal and -1, if
    * <code>otherPerson</code> should go before this one. Compares
    * only names.
    */
   @Override
       public int compareTo(ClearingPerson otherPerson) {
           if (otherPerson == null)
           {
               return 1;
           }
           return name.compareTo(otherPerson.getName());
       }
}
