package cz.su.GroupClearing;

/** Class defining exception thrown when nonexisting event is being
 * queried.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class GCEventDoesNotExistException extends GroupClearingException {
	/** Serial version uid of this exception.
	 */
	private static final long serialVersionUID = 1L;

    /** Constructor with no parameters.
     */
   public GCEventDoesNotExistException () {
      super();
   }
   /** This constructor allows to associate a message with the
    * exception.
    * @param message Message associated with the exception.
    */
   public GCEventDoesNotExistException (String message) {
      super (message);
   }
   /** This constructor allows to associate a message and a cause with
    * the exception.
    * @param message Message associated with the exception.
    * @param cause Cause of the exception, i.e. another
    * <code>Throwable</code>
    * object due to which this one is thrown.
    */
   public GCEventDoesNotExistException(String message, Throwable cause) {
      super(message, cause);
   }
   /** This constructor allows to associate a cause object with the
    * exception.
    * @param cause Cause of the exception, i.e. another
    * <code>Throwable</code> object due to which this is thrown.
    */
   public GCEventDoesNotExistException(Throwable cause) {
      super(cause);
   }

}
