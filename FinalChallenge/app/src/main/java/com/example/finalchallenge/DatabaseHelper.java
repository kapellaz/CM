package com.example.finalchallenge;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fitness.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_UTILIZADOR = "utilizador";
    private static final String TABLE_TREINO_EXEC = "treino_exec";
    private static final String TABLE_TREINO_PLANO = "treino_plano";
    private static final String TABLE_TREINO_EXERCICIO = "treino_exercicio";
    private static final String TABLE_TREINO_EXERCICIO_PLANO = "treino_exercicio_plano";
    private static final String TABLE_EXERCICIO = "exercicio";
    private static final String TABLE_SERIES = "series";
    private static final String TABLE_AMIGOS = "amigos";
    private static final String TABLE_PEDIDO_AMIZADE = "pedido_amizade";

    // Create Table Statements
    private static final String CREATE_TABLE_UTILIZADOR = "CREATE TABLE " + TABLE_UTILIZADOR + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT, " +
            "role TEXT, " +
            "created_at TEXT);";

    private static final String CREATE_TABLE_TREINO_EXEC = "CREATE TABLE " + TABLE_TREINO_EXEC + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nome TEXT, " +
            "user_id INTEGER, " +
            "data TEXT, " +
            "FOREIGN KEY(user_id) REFERENCES " + TABLE_UTILIZADOR + "(id));";

    private static final String CREATE_TABLE_TREINO_PLANO = "CREATE TABLE " + TABLE_TREINO_PLANO + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nome TEXT, " +
            "user_id INTEGER, " +
            "FOREIGN KEY(user_id) REFERENCES " + TABLE_UTILIZADOR + "(id));";

    private static final String CREATE_TABLE_TREINO_EXERCICIO = "CREATE TABLE " + TABLE_TREINO_EXERCICIO + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "exercicio_id INTEGER, " +
            "treino_id INTEGER, " +
            "FOREIGN KEY(exercicio_id) REFERENCES " + TABLE_EXERCICIO + "(id), " +
            "FOREIGN KEY(treino_id) REFERENCES " + TABLE_TREINO_EXEC + "(id));";

    private static final String CREATE_TABLE_TREINO_EXERCICIO_PLANO = "CREATE TABLE " + TABLE_TREINO_EXERCICIO_PLANO + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "exercicio_id INTEGER, " +
            "treino_id INTEGER, " +
            "series INTEGER, " +
            "repeticoes INTEGER, " +
            "FOREIGN KEY(exercicio_id) REFERENCES " + TABLE_EXERCICIO + "(id), " +
            "FOREIGN KEY(treino_id) REFERENCES " + TABLE_TREINO_PLANO + "(id));";

    private static final String CREATE_TABLE_EXERCICIO = "CREATE TABLE " + TABLE_EXERCICIO + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nome TEXT, " +
            "user_id INTEGER, " +
            "exercicios_id INTEGER, " +
            "FOREIGN KEY(user_id) REFERENCES " + TABLE_UTILIZADOR + "(id));";

    private static final String CREATE_TABLE_SERIES = "CREATE TABLE " + TABLE_SERIES + "(" +
            "peso INTEGER, " +
            "num_repeticoes INTEGER, " +
            "treino_exercicio_id INTEGER, " +
            "FOREIGN KEY(treino_exercicio_id) REFERENCES " + TABLE_TREINO_EXERCICIO + "(id));";

    private static final String CREATE_TABLE_AMIGOS = "CREATE TABLE " + TABLE_AMIGOS + "(" +
            "id_amigo1 INTEGER, " +
            "id_amigo2 INTEGER, " +
            "FOREIGN KEY(id_amigo1) REFERENCES " + TABLE_UTILIZADOR + "(id), " +
            "FOREIGN KEY(id_amigo2) REFERENCES " + TABLE_UTILIZADOR + "(id));";

    private static final String CREATE_TABLE_PEDIDO_AMIZADE = "CREATE TABLE " + TABLE_PEDIDO_AMIZADE + "(" +
            "id_envio INTEGER, " +
            "id_recebe INTEGER, " +
            "FOREIGN KEY(id_envio) REFERENCES " + TABLE_UTILIZADOR + "(id), " +
            "FOREIGN KEY(id_recebe) REFERENCES " + TABLE_UTILIZADOR + "(id));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_UTILIZADOR);
        db.execSQL(CREATE_TABLE_TREINO_EXEC);
        db.execSQL(CREATE_TABLE_TREINO_PLANO);
        db.execSQL(CREATE_TABLE_TREINO_EXERCICIO);
        db.execSQL(CREATE_TABLE_TREINO_EXERCICIO_PLANO);
        db.execSQL(CREATE_TABLE_EXERCICIO);
        db.execSQL(CREATE_TABLE_SERIES);
        db.execSQL(CREATE_TABLE_AMIGOS);
        db.execSQL(CREATE_TABLE_PEDIDO_AMIZADE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AMIGOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEDIDO_AMIZADE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREINO_EXERCICIO_PLANO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREINO_EXERCICIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCICIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREINO_PLANO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREINO_EXEC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UTILIZADOR);
        onCreate(db);
    }
}