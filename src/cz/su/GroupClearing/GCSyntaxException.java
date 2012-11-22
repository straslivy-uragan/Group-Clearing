package cz.su.GroupClearing;

/** Class defining exception thrown when syntax error occurs.
 * @author Strašlivý Uragán
 * @version 1.0
 * @since 1.0
 */
public class GCSyntaxException extends GroupClearingException
{
    /** Serial version uid of this exception.
     */
    private static final long serialVersionUID = 1L;
    /** Constructor with no parameters.
     */
   public GCSyntaxException()
   {
      super();
   }

   /** This constructor allows to associate a message with the
    * exception.
    * @param message Message associated with the exception.
    */
   public GCSyntaxException(String message)
   {
      super(message);
   }

   /** This constructor allows to associate a message and a cause with
    * the exception.
    * @param message Message associated with the exception.
    * @param cause Cause of the exception, i.e. another
    * <code>Throwable</code>
    * object due to which this one is thrown.
    */
   public GCSyntaxException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /** This constructor allows to associate a cause object with the
    * exception.
    * @param cause Cause of the exception, i.e. another
    * <code>Throwable</code> object due to which this is thrown.
    */
   public GCSyntaxException(Throwable cause)
   {
      super(cause);
   }
}
