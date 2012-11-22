package cz.su.GroupClearing;

/** A general exception used in the program. It also serves as a base
 * class to other exceptions used in the Group Clearing program.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class GroupClearingException extends Exception {

	/** Serial version uid of this exception.
	 */
	private static final long serialVersionUID = 1L;

    /** Constructor with no parameters.
     */
   public GroupClearingException () {
      super();
   }
   /** This constructor allows to associate a message with the
    * exception.
    * @param message Message associated with the exception.
    */
   public GroupClearingException (String message) {
      super (message);
   }
   /** This constructor allows to associate a message and a cause with
    * the exception.
    * @param message Message associated with the exception.
    * @param cause Cause of the exception, i.e. another
    * <code>Throwable</code>
    * object due to which this one is thrown.
    */
   public GroupClearingException(String message, Throwable cause) {
      super(message, cause);
   }

   /** This constructor allows to associate a cause object with the
    * exception.
    * @param cause Cause of the exception, i.e. another
    * <code>Throwable</code> object due to which this is thrown.
    */
   public GroupClearingException(Throwable cause) {
      super(cause);
   }
}
