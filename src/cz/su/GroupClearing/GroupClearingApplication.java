/**
 * 
 */
package cz.su.GroupClearing;

import android.app.Application;

/**
 * @author su
 * 
 */
public class GroupClearingApplication extends Application
{
   private static GroupClearingApplication instance;

   public static GroupClearingApplication getInstance()
   {
	return instance;
   }

   @Override
   public final void onCreate()
   {
      super.onCreate();
      instance = this;
   }

}
