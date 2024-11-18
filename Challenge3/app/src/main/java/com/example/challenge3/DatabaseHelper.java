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
    private static final int DATABASE_VERSION = 1;

    // Tabela de mensagens
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_SEND = "userSend";
    private static final String COLUMN_USER_RECEIVE = "userReceive";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_TIME = "time";

    // Criação da tabela
    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_SEND + " TEXT, " +
            COLUMN_USER_RECEIVE + " TEXT, " +
            COLUMN_TEXT + " TEXT, " +
            COLUMN_TIME + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MESSAGES);  // Cria a tabela
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);  // Remove a tabela se existir
        onCreate(db);  // Cria novamente
    }

    // Método para inserir uma mensagem
    public void insertMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_SEND, message.getUserSend());
        values.put(COLUMN_USER_RECEIVE, message.getUserReceive());
        values.put(COLUMN_TEXT, message.getText());
        values.put(COLUMN_TIME, message.getTime());

        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public ArrayList<Message> getAllMessages(String contactName, String username) {
        ArrayList<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // A consulta deve trazer as mensagens entre o username e o contactName
        // A busca será realizada considerando as duas possibilidades:
        // 1. O usuário enviou a mensagem (userSend = username e userReceive = contactName)
        // 2. O contato enviou a mensagem (userSend = contactName e userReceive = username)

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
                            cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
                    );
                    messages.add(message);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return messages;
    }


    // Método para obter a lista de usuários com quem o "username" teve comunicação
    public ArrayList<String> getContactsWithUser(String username) {
        ArrayList<String> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta SQL para buscar os contatos com quem o "username" teve comunicação
        String query = "SELECT DISTINCT " +
                "CASE WHEN " + COLUMN_USER_SEND + " = ? THEN " + COLUMN_USER_RECEIVE + " ELSE " + COLUMN_USER_SEND + " END AS contact " +
                "FROM " + TABLE_MESSAGES + " " +
                "WHERE " + COLUMN_USER_SEND + " = ? OR " + COLUMN_USER_RECEIVE + " = ? " +
                "AND " + COLUMN_USER_SEND + " != ? OR " + COLUMN_USER_RECEIVE + " != ?";

        // Argumentos para substituir os placeholders "?"
        String[] selectionArgs = { username, username, username, username, username };

        // Executa a consulta SQL
        Cursor cursor = db.rawQuery(query, selectionArgs);

        // Processar os resultados
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // Adicionar o contato à lista de resultados
                    @SuppressLint("Range") String contact = cursor.getString(cursor.getColumnIndex("contact"));
                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return contacts;

    }

    public void deleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Apaga todos os registros da tabela de mensagens
        db.execSQL("DELETE FROM " + TABLE_MESSAGES);

        db.close();
    }
}
