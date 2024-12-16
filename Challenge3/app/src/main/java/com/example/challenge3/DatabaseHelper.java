package com.example.challenge3;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;



public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat_db";
    private static final int DATABASE_VERSION = 10;

    // Tabela de mensagens
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_SEND = "userSend";
    private static final String COLUMN_USER_RECEIVE = "userReceive";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_TIME = "time";
    private static final String TABLE_ARDUINO_CONFIGURATION = "arduino_configuration";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_CONTACT = "contact";

     /**
       * Creating table Arduino configuration - SQL
      */
    private static final String CREATE_TABLE_ARDUINO_CONFIGURATION = "CREATE TABLE " + TABLE_ARDUINO_CONFIGURATION + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT, " +
            COLUMN_CONTACT + " TEXT);";



    /**
     * Creating table Message - SQL
     */
    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_SEND + " TEXT, " +
            COLUMN_USER_RECEIVE + " TEXT, " +
            COLUMN_TEXT + " TEXT, " +
            COLUMN_TIME + " TEXT, " +
            "isRead INTEGER DEFAULT 0);";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    /**
     * Initializes and sets up essential components creating new tables
     * Requests notification permissions if needed.
     * @param db - db connection
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MESSAGES);  // Create messages table
        db.execSQL(CREATE_TABLE_ARDUINO_CONFIGURATION); // Create arduino_configuration table

        // Log table creation
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{TABLE_ARDUINO_CONFIGURATION});
        if (cursor.moveToFirst()) {
            System.out.println("Table " + TABLE_ARDUINO_CONFIGURATION + " created successfully.");
        } else {
            System.out.println("Table " + TABLE_ARDUINO_CONFIGURATION + " creation failed.");
        }
        cursor.close();
    }



    /**
     * Method that updates the database
     * @param db - db connection
     * @param oldVersion - old version
     * @param newVersion - new version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARDUINO_CONFIGURATION);
        onCreate(db);
    }



    /**
     * Method input into DB a new message
     * @param message - Message sent
     */
    public void insertMessage(Message message) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_SEND, message.getUserSend());
            values.put(COLUMN_USER_RECEIVE, message.getUserReceive());
            values.put(COLUMN_TEXT, message.getText());
            values.put(COLUMN_TIME, message.getTime());
            values.put("isRead", 0);
            db.insert(TABLE_MESSAGES, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }



    /**
     * Method that returns all messaages between contactName and username
     * @param contactName - Contact that username will contact
     * @param username - Username name
     * @return All messages between both
     */
    public ArrayList<Message> getAllMessages(String contactName, String username) {

        ArrayList<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        try{
            db = this.getReadableDatabase();
            String selection = "(" + COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?) OR (" +
                    COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?)";

            String[] selectionArgs = new String[]{username, contactName, contactName, username};

            // Sort messages by time, from newest to oldest
            Cursor cursor = db.query(TABLE_MESSAGES, null, selection, selectionArgs, null, null, COLUMN_TIME + " ASC");

            if (cursor.moveToFirst()) {
                do {
                    // Adiciona as mensagens à lista
                    @SuppressLint("Range") Message message = new Message(
                            cursor.getString(cursor.getColumnIndex(COLUMN_USER_SEND)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_USER_RECEIVE)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_TEXT)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_TIME)),
                            cursor.getInt(cursor.getColumnIndex("isRead"))

                    );
                    messages.add(message);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return messages;
    }



    /**
     * Method to get the last message between two users
     */
    @SuppressLint("Range")
    public Message getLastMessage(String sender, String receiver) {
        SQLiteDatabase db = null;
        Message lastMessage = null;
        try{
            db = this.getReadableDatabase();

            // SQL query to get the last message between the sender and receiver, ordering by date
            String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE (" + COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?) " +
                    "OR (" + COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?) " +
                    "ORDER BY " + COLUMN_TIME + " DESC LIMIT 1";

            Cursor cursor = db.rawQuery(query, new String[]{sender, receiver, receiver, sender});

            if (cursor.moveToFirst()) {

                lastMessage = new Message(
                        cursor.getString(cursor.getColumnIndex(COLUMN_USER_SEND)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_USER_RECEIVE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TEXT)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndex("isRead"))
                );

                cursor.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return lastMessage;
    }



    /**
     * Method that update the state of 'isRead' to True - That happens when a user enters into a chat
     * @param receiver - contact
     * @param sender - username
     */
    public void markMessagesAsRead(String receiver, String sender) {
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("isRead", 1);
            db.update(TABLE_MESSAGES, values, COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?", new String[]{sender, receiver});
            } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }



    /**
     * Method that returns all contacts between username and other contacts
     * @param username - username logged
     * @return
     */
    public ArrayList<String> getContactsWithUser(String username) {
        SQLiteDatabase db = null;
        ArrayList<String> contacts = new ArrayList<>();

        try{

        db = this.getReadableDatabase();


        String query = "SELECT DISTINCT " +
                "CASE WHEN " + COLUMN_USER_SEND + " = ? THEN " + COLUMN_USER_RECEIVE + " ELSE " + COLUMN_USER_SEND + " END AS contact, " +
                COLUMN_TIME + " " +
                "FROM " + TABLE_MESSAGES + " " +
                "WHERE " + COLUMN_USER_SEND + " = ? OR " + COLUMN_USER_RECEIVE + " = ? " +
                "ORDER BY " + COLUMN_TIME + " DESC";


        String[] selectionArgs = { username, username, username };


        Cursor cursor = db.rawQuery(query, selectionArgs);


            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String contact = cursor.getString(cursor.getColumnIndex("contact"));
                    if (!contacts.contains(contact)) { // avoid duplicated contacts
                        contacts.add(contact);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }


        return contacts;
    }



    /**
     * Method that delete all messages between the username and contact selected
     * @param userSend - username logged
     * @param userReceive - contact selected
     */
    public void deleteConversation(String userSend, String userReceive) {
        SQLiteDatabase db = null;
        try{
        db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES,
                "(" + COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?) OR " +
                        "(" + COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?)",
                new String[]{userSend, userReceive, userReceive, userSend});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }



    /**
     * Method that get all contact selected in arduino notifications
     * @param username - username logged
     */
    public ArrayList<String> getContactsForArduinoNotification(String username) {
        ArrayList<String> selectedContacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_ARDUINO_CONFIGURATION, new String[]{COLUMN_CONTACT},
                    COLUMN_USERNAME + " = ?", new String[]{username}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    selectedContacts.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return selectedContacts;
    }


    /**
     * Method that save contact for arduino notifications
     * @param username - username logged
     * @param contact - contact selected
     */
    public void saveContactForArduinoNotification(String username, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_CONTACT, contact);
        db.insert(TABLE_ARDUINO_CONFIGURATION, null, values);
    }

    public void removeContactFromArduinoNotification(String username, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ARDUINO_CONFIGURATION, COLUMN_USERNAME + " = ? AND " + COLUMN_CONTACT + " = ?",
                new String[]{username, contact});
    }
}
