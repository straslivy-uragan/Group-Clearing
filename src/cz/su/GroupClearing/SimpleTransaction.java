package cz.su.GroupClearing;

import java.math.BigDecimal;
import java.util.Currency;

/** Class representing the base of a transaction for computing
 * clearance suggestion. This class is used in
 * <code>SuggestClearanceActivity</code>, see there for further
 * description of how it is used. This class represents a simple
 * transaction of giving money by one participant to another one. Thus
 * only payer, payee, amount and currency are stored in this
 * transaction object.
 * @see cz.su.GroupClearing.SuggestClearanceActivity
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class SimpleTransaction {
    /** Id of the receiver or payee. Receiver is the participant who receives
     * money in this transaction. */
    private final long receiverId;
    /** Id of the payer of the money in this transaction. */
    private final long payerId;
    /** Amount of money being transferred from payer to payee. */
    private final BigDecimal value;
    /** Currency in which the transaction is performed. */
    private final Currency currency;

    /** Initializes the transaction object with given values.
     * @param receiverId Id of the payee, i.e. the
     * participant who receives the money.
     * @param payerId Id of the payer, i.e. the participant who pays the
     * money.
     * @param value Amount of money being transferred from payer to
     * payee.
     * @param currency Currency in which the transaction is performed.
     */
    public SimpleTransaction(long receiverId, long payerId,
            BigDecimal value, Currency currency) {
        this.receiverId = receiverId;
        this.payerId = payerId;
        this.value = value;
        this.currency = currency;
    }

    /** Returns the id of the payee of this transaction.
     *
     * @return Id of the payee (or receiver) of this transaction.
     */
    public long getReceiverId() {
        return receiverId;
    }

    /** Returns the id of the payer of this transaction.
     *
     * @return Id of the payer of this transaction.
     */
    public long getPayerId() {
        return payerId;
    }

    /** Returns the amount being transferred in this transaction.
     *
     * @return The amount being transferred in this transaction from
     * payer to payee.
     */
    public BigDecimal getValue() {
        return value;
    }

    /** Returns the currency in which this transaction is performed.
     *
     * @return The currency in which this transaction is performed.
     */
    public Currency getCurrency() {
        return currency;
    }
}
