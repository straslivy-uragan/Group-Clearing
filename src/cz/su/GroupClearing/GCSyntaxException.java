package cz.su.GroupClearing;

public class GCSyntaxException extends GroupClearingException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

   public GCSyntaxException () {
      super();
   }
   public GCSyntaxException (String message) {
      super (message);
   }
   public GCSyntaxException(String message, Throwable cause) {
      super(message, cause);
   }
   public GCSyntaxException(Throwable cause) {
      super(cause);
   }
}
