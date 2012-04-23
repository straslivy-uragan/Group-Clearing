/**
 * 
 */
package cz.su.GroupClearing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;

import cz.su.GroupClearing.ClearingEvent;
import cz.su.GroupClearing.GCSyntaxException;

/**
 * @author su
 *
 */
public class ClearingEventReader {

   private static final ClearingEventReader instance = new ClearingEventReader();

   public class TagInfo {
      private static final int EOF = -1;
      private static final int END = 0;
      private static final int ID = 1;
      private static final int NAME = 2;
      private static final int NOTE = 3;
      private static final int DEFAULT_CURRENCY = 4;
      private static final int START_DATE = 5;
      private static final int FINISH_DATE = 6;
      private static final int BALANCE = 7;
      private static final int UNKNOWN = 8;

      private int tag;

      public TagInfo() {
         tag = UNKNOWN;
      }
      public int getTag() {
         return tag;
      }
      public void setTag(int newTag) {
         tag = newTag;
      }
   };

   private ClearingEventReader() {
   }

   public static ClearingEventReader getInstance() {
      return instance;
   }

   private int skipWhitespaces(Reader reader, int lastChar) throws IOException {
      int inputChar = lastChar;
      while (Character.isWhitespace(inputChar))
      {
         inputChar = reader.read();
      }
      return inputChar;
   }

   private int readTag(Reader reader, int lastChar, TagInfo tag)
      throws IOException {
      int inputChar = skipWhitespaces(reader, lastChar);
      StringBuilder tagBuilder = new StringBuilder();
      if (inputChar < 0)
      {
         tag.setTag(TagInfo.EOF);
         return inputChar;
      }
      if (inputChar == '>')
      {
         tag.setTag(TagInfo.END);
         return inputChar;
      }
      while (Character.isLetter(inputChar))
      {
         tagBuilder.append(Character.toChars(
                  Character.toLowerCase(inputChar)));
         inputChar = reader.read();
      }
      String tagString = tagBuilder.toString();
      if (tagString.equals("id"))
      {
         tag.setTag(TagInfo.ID);
      }
      else if (tagString.equals("name"))
      {
         tag.setTag(TagInfo.NAME);
      }
      else if (tagString.equals("note"))
      {
         tag.setTag(TagInfo.NOTE);
      }
      else if (tagString.equals("defaultcurrency"))
      {
         tag.setTag(TagInfo.DEFAULT_CURRENCY);
      }
      else if (tagString.equals("startdate"))
      {
         tag.setTag(TagInfo.START_DATE);
      }
      else if (tagString.equals("finishdate"))
      {
         tag.setTag(TagInfo.FINISH_DATE);
      }
      else if (tagString.equals("balance"))
      {
         tag.setTag(TagInfo.BALANCE);
      }
      else
      {
         tag.setTag(TagInfo.UNKNOWN);
      }
      return inputChar;
   }

   private int readValue(Reader reader,
         int lastChar, StringBuilder valueBuilder)
      throws IOException, GCSyntaxException {
      // Skip =
      int inputChar = skipWhitespaces(reader, lastChar);
      if (inputChar != '=')
      {
         throw new GCSyntaxException();
      }
      inputChar = skipWhitespaces(reader, reader.read());
      if (inputChar == '"')
      {
         inputChar = reader.read();
         while (inputChar != -1 && inputChar != '"')
         {
            valueBuilder.append(Character.toChars(inputChar));
            inputChar = reader.read();
         }
         if (inputChar == -1)
         {
            throw new GCSyntaxException();
         }
      }
      else
      {
         while (inputChar != -1 && !Character.isWhitespace(inputChar))
         {
            valueBuilder.append(Character.toChars(inputChar));
            inputChar = reader.read();
         }
         if (inputChar == -1)
         {
            throw new GCSyntaxException ();
         }
      }
      return inputChar;
   }

   public static ClearingEvent readEventFromFile(File inputFile)
		   throws IOException, GCSyntaxException {
      return instance.readEventFromFileInner(inputFile);
   }

   private void readEventHeader(Reader reader, ClearingEvent event)
      throws IOException, GCSyntaxException {
         int inputChar = skipWhitespaces(reader, reader.read());
         if (inputChar != '<')
         {
            throw new GCSyntaxException();
         }
         inputChar = skipWhitespaces(reader, reader.read());
         StringBuilder eventTagBuilder = new StringBuilder();
         while (Character.isLetter(inputChar))
         {
            eventTagBuilder.append(Character.toChars(
                     Character.toLowerCase(inputChar)));
            inputChar = reader.read();
         }
         if (!eventTagBuilder.toString().equals("event"))
         {
            throw new GCSyntaxException();
         }
         TagInfo tagInfo = new TagInfo ();
         inputChar = readTag(reader, inputChar, tagInfo);
         while (tagInfo.getTag() != TagInfo.END
               && tagInfo.getTag() != TagInfo.EOF)
         {
            StringBuilder value = new StringBuilder();
            inputChar = readValue(reader, inputChar, value);
            switch (tagInfo.getTag()) {
               case TagInfo.ID:
                  try {
                     event.setId(Integer.parseInt(value.toString()));
                  }
                  catch(NumberFormatException e) {
                     throw new GCSyntaxException(e);
                  }
                  break;
               case TagInfo.NAME:
                  event.setName(value.toString());
                  break;
               case TagInfo.NOTE:
                  event.setNote(value.toString());
                  break;
               case TagInfo.DEFAULT_CURRENCY:
                  try {
                     event.setDefaultCurrency(
                           Currency.getInstance(value.toString()));
                  }
                  catch (IllegalArgumentException e) {
                     throw new GCSyntaxException(e);
                  }
                  break;
               case TagInfo.START_DATE:
                  try {
                     SimpleDateFormat df = new SimpleDateFormat("dd.mm.yyyy");
                     event.setStartDate(df.parse(value.toString()));
                  }
                  catch (ParseException e) {
                     throw new GCSyntaxException(e);
                  }
                  break;
               case TagInfo.FINISH_DATE:
                  try {
                     SimpleDateFormat df = new SimpleDateFormat("dd.mm.yyyy");
                     event.setFinishDate(df.parse(value.toString()));
                  }
                  catch (ParseException e) {
                     throw new GCSyntaxException(e);
                  }
                  break;
               default:
                  throw new GCSyntaxException();
            }
            inputChar = readTag(reader, reader.read(), tagInfo);
         }
         if (tagInfo.getTag() == TagInfo.EOF)
         {
            throw new GCSyntaxException();
         }
      }

   private void readParticipant(Reader reader, int inputChar,
         ClearingEvent event) throws IOException, GCSyntaxException {
      TagInfo tagInfo = new TagInfo ();
      inputChar = readTag(reader, inputChar, tagInfo);
      int id = 0;
      String name = null;
      String note = null;
      int balance = 0;
      while (tagInfo.getTag() != TagInfo.END
            && tagInfo.getTag() != TagInfo.EOF)
      {
         StringBuilder value = new StringBuilder();
         inputChar = readValue(reader, inputChar, value);
         switch (tagInfo.getTag()) {
            case TagInfo.ID:
               try {
                  id = Integer.parseInt(value.toString());
               }
               catch(NumberFormatException e) {
                  throw new GCSyntaxException(e);
               }
               break;
            case TagInfo.NAME:
               name = value.toString();
               break;
            case TagInfo.NOTE:
               note = value.toString();
               break;
            case TagInfo.BALANCE:
               try {
                  balance = Integer.parseInt(value.toString());
               }
               catch(NumberFormatException e) {
                  throw new GCSyntaxException(e);
               }
               break;
            default:
               throw new GCSyntaxException();
         }
         inputChar = readTag(reader, reader.read(), tagInfo);
      }
      ClearingPerson participant = new ClearingPerson(id, name, note, balance);
      event.addParticipant(participant);
   }

   private void readTransaction(Reader reader, int inputChar,
         ClearingEvent event) throws IOException, GCSyntaxException {
   }

   private void readEventContent(Reader reader, ClearingEvent event)
      throws IOException, GCSyntaxException {
         boolean closingTagRead = false;
         boolean isClosingTag = false;
         while (! closingTagRead)
         {
            int inputChar = skipWhitespaces(reader, reader.read());
            if (inputChar != '<')
            {
               throw new GCSyntaxException();
            }
            inputChar = skipWhitespaces(reader, reader.read());
            StringBuilder primaryTagBuilder = new StringBuilder();
            if (inputChar == '/')
            {
               isClosingTag = true;
               inputChar = skipWhitespaces(reader, reader.read());
            }
            while (Character.isLetter(inputChar))
            {
               primaryTagBuilder.append(Character.toChars(
                        Character.toLowerCase(inputChar)));
               inputChar = reader.read();
            }
            String primaryTagString = primaryTagBuilder.toString();
            if (isClosingTag)
            {
               if (!primaryTagString.equals("event"))
               {
                  throw new GCSyntaxException();
               }
               inputChar = skipWhitespaces(reader, inputChar);
               if (inputChar != '>')
               {
                  throw new GCSyntaxException();
               }
               closingTagRead = true;
            }
            else
            {
               if (primaryTagString.equals("participant")
                     || primaryTagString.equals("person"))
               {
                  readParticipant(reader, inputChar, event);
               }
               else if (primaryTagString.equals("transaction"))
               {
                  readTransaction(reader, inputChar, event);
               }
               else
               {
                  throw new GCSyntaxException();
               }
            }
         }
      }

   private ClearingEvent readEventFromFileInner(File inputFile)
   throws IOException, GCSyntaxException {
      ClearingEvent event = new ClearingEvent(0);
      event.setFile(inputFile);
      Reader reader = null;
      try {
         reader = new BufferedReader (new FileReader (inputFile));
         readEventHeader(reader, event);
         readEventContent(reader, event);
      }
      finally {
         if (reader != null)
         {
            reader.close();
         }
      }
      event.resetModified();
      return event;
   }

}
