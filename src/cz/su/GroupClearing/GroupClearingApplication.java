z**
 * 
 */
package cz.su.GroupClearing;

import java.util.Iterator;
import java.util.Vector;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;

import cz.su.GroupClearing.ClearingEvent;
import cz.su.GroupClearing.ClearingEventReader;
import cz.su.GroupClearing.GroupClearingException;

/**
 * @author su
 *
 */
public class GroupClearingApplication extends Application {
   private static GroupClearingApplication instance;
   private Vector<ClearingEvent> events;
   private ClearingEvent activeEvent;
   private int indexOfActiveEvent;
   private int nextId;

   public static GroupClearingApplication getInstance() {
      return instance;
   }

   public Vector<ClearingEvent> getEvents() {
      return events;
   }

   public ClearingEvent getActiveEvent() {
      if (activeEvent == null)
      {
         activeEvent = new ClearingEvent(nextId ++);
      }
      return activeEvent;
   }

   @Override
      public final void onCreate() {
         super.onCreate();
         instance = this;
         events = new Vector<ClearingEvent>();
         activeEvent = null;
         indexOfActiveEvent = -1;
         nextId = 1;
      }

   public void prepareActiveEvent(int indexOfEvent) {
      indexOfActiveEvent = indexOfEvent;
      if (indexOfEvent >= events.size())
      {
         activeEvent = new ClearingEvent(nextId ++);
      }
      else
      {
         activeEvent = events.elementAt(indexOfEvent);
      }
   }

   public void saveActiveEvent() {
      if (indexOfActiveEvent >= events.size()
            && indexOfActiveEvent >= 0
            && activeEvent != null)
      {
         events.addElement(activeEvent);
      }
      try {
          saveModifiedEvents();
      }
      catch (GroupClearingException e) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(e.getMessage());
         builder.setTitle(R.string.alert_title);
         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
               }
               });
         AlertDialog alert = builder.create();
         alert.show();
      }
   }

   public void deleteActiveEvent() {
      if (indexOfActiveEvent < events.size())
      {
         events.removeElementAt(indexOfActiveEvent);
      }
      try {
         File eventFile = getFileForEvent(activeEvent);
         eventFile.delete();
      }
      catch (GroupClearingException e) {
      }
      activeEvent = null;
      indexOfActiveEvent = -1;
   }

   public void deleteEventWithId(int id) {
      for (int position = 0; position < events.size(); ++ position)
      {
         ClearingEvent anEvent = events.get(position);
         if (anEvent.getId() == id)
         {
            events.remove(position);
            try {
               File eventFile = getFileForEvent(anEvent);
               eventFile.delete();
            }
            catch (GroupClearingException e) {
            }
            break;
         }
      }
   }

   public File getFileForEvent(ClearingEvent event) throws GroupClearingException {
	   File eventDir = getExternalFilesDir(null);
      if (eventDir == null)
      {
         throw new GroupClearingException(
               getString(R.string.no_external_storage_alert));
      }
      return new File(eventDir, "event-"+event.getId()+".gcl");
   }
   
   public void readEvents() throws GroupClearingException {
      File eventDir = getExternalFilesDir(null);
      if (eventDir == null)
      {
         throw new GroupClearingException(
               getString(R.string.no_external_storage_alert));
      }
      File[] eventFiles = eventDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
            return filename.endsWith(".gcl");
            }
            });
      boolean syntaxError = false;
      if (eventFiles != null)
      {
         for (int i = 0; i < eventFiles.length; ++i)
         {
            ClearingEvent event = null;
            try {
               event = ClearingEventReader.readEventFromFile(eventFiles[i]);
            }
            catch (IOException e) {
               continue;
            }
            catch (GCSyntaxException e) {
               syntaxError = true;
               continue;
            }
            events.addElement(event);
            if (event.getId() >= nextId)
            {
               nextId = event.getId() + 1;
            }
         }
      }
      if (syntaxError)
      {
         throw new GCSyntaxException(getString(R.string.syntax_error_alert));
      }
   }

   public void saveModifiedEvents() throws GroupClearingException {
      Iterator<ClearingEvent> eventsIterator = events.iterator();
      while (eventsIterator.hasNext())
      {
         ClearingEvent anEvent = eventsIterator.next();
         
         if (anEvent.isModified())
         {
            try {
               if (anEvent.getFile() == null)
               {
                  anEvent.setFile(getFileForEvent(anEvent));
               }
               anEvent.printToFile();
               anEvent.resetModified();
            }
            catch (IOException e)
            {
               throw new GroupClearingException(
                     getString(R.string.save_failed_alert));
            }
         }
      }
   }
}
