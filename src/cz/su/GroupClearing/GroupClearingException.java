package cz.su.GroupClearing;

public class GroupClearingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

   public GroupClearingException () {
      super();
   }
   public GroupClearingException (String message) {
      super (message);
   }
   public GroupClearingException(String message, Throwable cause) {
      super(message, cause);
   }
   public GroupClearingException(Throwable cause) {
      super(cause);
   }
}
