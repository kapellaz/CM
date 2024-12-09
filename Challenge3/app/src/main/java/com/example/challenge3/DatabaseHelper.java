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

    private static final String CREATE_TABLE_ARDUINO_CONFIGURATION = "CREATE TABLE " + TABLE_ARDUINO_CONFIGURATION + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT, " +
            COLUMN_CONTACT + " TEXT);";



    // Criação da tabela com a coluna isRead
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



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARDUINO_CONFIGURATION);
        onCreate(db);
    }


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
                db.close();  // Fecha o banco de dados de forma segura
            }
        }
    }


    public ArrayList<Message> getAllMessages(String contactName, String username) {

        ArrayList<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        try{
            db = this.getReadableDatabase();
            String selection = "(" + COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?) OR (" +
                    COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?)";

            String[] selectionArgs = new String[]{username, contactName, contactName, username};

            // Ordenar as mensagens por tempo, do mais recente para o mais antigo
            Cursor cursor = db.query(TABLE_MESSAGES, null, selection, selectionArgs, null, null, COLUMN_TIME + " ASC");

            if (cursor != null) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();  // Fecha o banco de dados de forma segura
            }
        }
        return messages;
    }


    // Método para obter a última mensagem entre dois usuários
    @SuppressLint("Range")
    public Message getLastMessage(String sender, String receiver) {
        SQLiteDatabase db = null;
        Message lastMessage = null;
        try{
            db = this.getReadableDatabase();

            // Consulta SQL para pegar a última mensagem entre o sender e receiver, ordenando por data
            String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE (" + COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?) " +
                    "OR (" + COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?) " +
                    "ORDER BY " + COLUMN_TIME + " DESC LIMIT 1";

            Cursor cursor = db.rawQuery(query, new String[]{sender, receiver, receiver, sender});

            if (cursor != null && cursor.moveToFirst()) {

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
                db.close();  // Fecha o banco de dados de forma segura
            }
        }
        return lastMessage;
    }

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
                db.close();  // Fecha o banco de dados de forma segura
            }
        }
    }


    // Método para atualizar o campo isRead das mensagens
    public void updateMessageReadStatus(String sender, String receiver, boolean isSender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.update(TABLE_MESSAGES, values, COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?", new String[]{sender, receiver});
        db.update(TABLE_MESSAGES, values, COLUMN_USER_SEND + " = ? AND " + COLUMN_USER_RECEIVE + " = ?", new String[]{receiver, sender});
        db.close();
    }




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

        // Processar os resultados
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String contact = cursor.getString(cursor.getColumnIndex("contact"));
                    if (!contacts.contains(contact)) { // Evita contatos duplicados
                        contacts.add(contact);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();  // Fecha o banco de dados de forma segura
            }
        }


        return contacts;
    }



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
                db.close();  // Fecha o banco de dados de forma segura
            }
        }
    }

    public void deleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Apaga todos os registros da tabela de mensagens
        db.execSQL("DELETE FROM " + TABLE_MESSAGES);

        db.close();
    }


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
            e.printStackTrace(); // Log the exception
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close the cursor
            }
            db.close(); // Close the database connection
        }

        return selectedContacts;
    }


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
