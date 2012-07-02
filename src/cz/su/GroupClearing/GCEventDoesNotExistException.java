package cz.su.GroupClearing;

public class GCEventDoesNotExistException extends GroupClearingException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

   public GCEventDoesNotExistException () {
      super();
   }
   public GCEventDoesNotExistException (String message) {
      super (message);
   }
   public GCEventDoesNotExistException(String message, Throwable cause) {
      super(message, cause);
   }
   public GCEventDoesNotExistException(Throwable cause) {
      super(cause);
   }

}
