package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.finalchallenge.classes.Execution;
import com.example.finalchallenge.classes.Exercicio;
import com.example.finalchallenge.classes.Exercise;
import com.example.finalchallenge.classes.ExerciseDetailed;
import com.example.finalchallenge.classes.TreinoPlano;
import com.example.finalchallenge.classes.TreinosDetails;
import com.example.finalchallenge.classes.TreinosDone;
import com.example.finalchallenge.classes.Utilizador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fitness.db";
    private static final int DATABASE_VERSION = 16;

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
    private static final String TABLE_TREINO_DONE = "treino_done";

    // Create Table Statements
    private static final String CREATE_TABLE_UTILIZADOR = "CREATE TABLE " + TABLE_UTILIZADOR + "(" +
            "id TEXT, " +
            "username TEXT, " +
            "password TEXT); ";

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

    // Tabela para guardar os treinos feitos e a data em que foram realizados
    private static final String CREATE_TABLE_TREINO_DONE = "CREATE TABLE " + TABLE_TREINO_DONE + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "treino_id INTEGER, " +
            "data TEXT, " +
            "exec INTEGER, " +
            "user_id TEXT, " +
            "FOREIGN KEY(treino_id) REFERENCES " + TABLE_TREINO_EXEC + "(id));";


    private static final String CREATE_TABLE_TREINO_EXERCICIO_PLANO = "CREATE TABLE " + TABLE_TREINO_EXERCICIO_PLANO + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "exercicio_id INTEGER, " +
            "treino_id INTEGER, " +
            "series INTEGER, " +
            "repeticoes INTEGER, " +
            "order_id INTEGER, " +
            "FOREIGN KEY(exercicio_id) REFERENCES " + TABLE_EXERCICIO + "(id), " +
            "FOREIGN KEY(treino_id) REFERENCES " + TABLE_TREINO_PLANO + "(id));";

    private static final String CREATE_TABLE_EXERCICIO = "CREATE TABLE " + TABLE_EXERCICIO + "(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nome TEXT)";



    private static final String CREATE_TABLE_SERIES = "CREATE TABLE " + TABLE_SERIES + "(" +
            "peso INTEGER, " +
            "numero_serie," +
            "treino_exercicio_id INTEGER, " +
            "plano_id INTEGER, " +
            "exec INTEGER, " +
            "FOREIGN KEY(plano_id) REFERENCES " + TABLE_TREINO_PLANO + "(id), " +
            "FOREIGN KEY(treino_exercicio_id) REFERENCES " + TABLE_TREINO_EXERCICIO_PLANO + "(id));";

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
        db.execSQL(CREATE_TABLE_TREINO_DONE);
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREINO_DONE);
        onCreate(db);
    }


    @SuppressLint("Range")
    public int get_training_execs(int id){
        //get max exec from table treino done for a trainid
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(exec) FROM " + TABLE_TREINO_DONE + " WHERE treino_id = ?", new String[]{String.valueOf(id)});
        int exec = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                exec = cursor.getInt(cursor.getColumnIndex("MAX(exec)"));
            }
            cursor.close();
        }
        db.close();
        return exec;
    }

    public void inserttreinodone(int treino_id, String data, int exec,String user_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("treino_id", treino_id);
        values.put("data", data);
        values.put("exec", exec);
        values.put("user_id",user_id);
        db.insert(TABLE_TREINO_DONE, null, values);
        db.close();
    }


    public void inserttableexercicioplano(int exercicio_id, int treino_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("exercicio_id", exercicio_id);
        values.put("treino_id", treino_id);
        db.insert(TABLE_TREINO_EXERCICIO_PLANO, null, values);
        db.close();
    }




    //funcao para dar update à tabela TABLE_SERIES
    public void insertSeries(int peso, int numero_serie, int treino_exercicio_id, int treino_id, int exec) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("peso", peso);
        values.put("treino_exercicio_id", treino_exercicio_id);
        values.put("numero_serie", numero_serie);
        values.put("plano_id", treino_id);
        values.put("exec", exec);
        db.insert(TABLE_SERIES, null, values);
        db.close();
    }

/*
    public List<TreinoExec> getAllTreinosByUserId(int userId) {
        List<TreinoExec> treinos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();


        // Consulta SQL para buscar todos os treinos do usuário
        Cursor cursor = db.query(TABLE_TREINO_EXEC,
                new String[] {"id", "nome", "data"},
                "user_id = ?", // Filtra pelo user_id
                new String[] {String.valueOf(userId)}, // Passa o user_id como parâmetro
                null, null, null);

        // Verifica se há resultados e os adiciona à lista
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String nome = cursor.getString(cursor.getColumnIndex("nome"));
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("data"));

                    // Cria o objeto TreinoExec e adiciona à lista
                    TreinoExec treino = new TreinoExec(id, nome, userId, data);
                    treinos.add(treino);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return treinos;
    }


*/

    //funcao que retorna todos os treinos done
    public List<TreinosDone> getAllTreinosDone() {
        List<TreinosDone> treinos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TREINO_DONE,
                new String[] {"id", "treino_id", "data", "exec"},
                null, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") int treino_id = cursor.getInt(cursor.getColumnIndex("treino_id"));
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("data"));
                    @SuppressLint("Range") int exec = cursor.getInt(cursor.getColumnIndex("exec"));
                    TreinosDone treino = new TreinosDone(id, treino_id, data, exec);
                    treinos.add(treino);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return treinos;
    }

    public List<TreinosDone> getAllTreinosDoneByUserId(String userId) {
        List<TreinosDone> treinos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TREINO_DONE,
                new String[] {"id", "treino_id", "data", "exec"},
                "user_id = ?", // Filtra pelo user_id
                new String[] {String.valueOf(userId)}, // Passa o user_id como parâmetro
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") int treino_id = cursor.getInt(cursor.getColumnIndex("treino_id"));
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("data"));
                    @SuppressLint("Range") int exec = cursor.getInt(cursor.getColumnIndex("exec"));
                    TreinosDone treino = new TreinosDone(id, treino_id, data, exec);
                    treinos.add(treino);
                } while (cursor.moveToNext());
                }
                cursor.close();
                }

                db.close();
                return treinos;
                }






    public TreinosDetails getTreinoDetails(TreinosDone treinoDone) {
        System.out.println("TreinoDone: " + treinoDone);
        int treinoId = treinoDone.getTreino_id();
        TreinosDetails treinoDetails = new TreinosDetails(treinoDone.getId(), treinoDone.getTreino_id(), treinoDone.getData(), new ArrayList<>(),treinoDone.getExec());
        SQLiteDatabase db = this.getReadableDatabase();
        System.out.println("TreinoId: " + treinoId);
        List<Exercise> exercises = getExercisesForTraining(treinoId);
        System.out.println(exercises);

        List<ExerciseDetailed> exercisesDetailed = new ArrayList<>();
        //for every exercise in exercises, get the pair serie weight and serie number
        for (Exercise exercise : exercises) {

            Map<Integer, Integer> seriesMap = new HashMap<>(); // Chave: número da série, Valor: peso

            Cursor cursor = db.query(
                    TABLE_SERIES,
                    new String[] {"peso", "numero_serie"},
                    "treino_exercicio_id = ? AND plano_id = ? AND exec = ?", // Filtra pelo treino_exercicio_id e plano_id
                    new String[] {String.valueOf(exercise.getId()), String.valueOf(treinoId), String.valueOf(treinoDone.getExec())}, // Passa os valores
                    null, null, null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int peso = cursor.getInt(cursor.getColumnIndex("peso"));
                        @SuppressLint("Range") int numeroSerie = cursor.getInt(cursor.getColumnIndex("numero_serie"));
                        seriesMap.put(numeroSerie, peso); // Adiciona a série ao mapa
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            ExerciseDetailed exerciseDetailed = new ExerciseDetailed(exercise, seriesMap);
            exercisesDetailed.add(exerciseDetailed);
        }

        treinoDetails.setExercise(exercisesDetailed);
        TreinosDetails details = treinoDetails;

        // Prepare details to show in the dialog
        StringBuilder detailsText = new StringBuilder();
        for (ExerciseDetailed exercise : details.getExercise()) {
            detailsText.append(exercise);
            detailsText.append("Series Information:\n");

            // Iterate over the series map to display the data
            for (Map.Entry<Integer, Integer> entry : exercise.getSeriesMap().entrySet()) {
                detailsText.append("Set ").append(entry.getKey())
                        .append(": ").append(entry.getValue()).append(" kilos\n");
            }
        }
        System.out.println(detailsText);
        return treinoDetails;
    }


    public List<Exercise> getExercisesForTraining(int treinoId) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query para obter os exercícios associados ao treino
        String query = "SELECT te.id, e.nome, te.series, te.repeticoes, te.order_id " +
                "FROM " + TABLE_EXERCICIO + " e " +
                "INNER JOIN " + TABLE_TREINO_EXERCICIO_PLANO + " te ON e.id = te.exercicio_id " +
                "WHERE te.treino_id = ? order by te.order_id ASC";

        // Executar a consulta
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(treinoId)});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    // Criar o objeto Exercise com os dados recuperados
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                    int series = cursor.getInt(cursor.getColumnIndexOrThrow("series"));
                    int repetitions = cursor.getInt(cursor.getColumnIndexOrThrow("repeticoes"));
                    int order = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));

                    Exercise exercise = new Exercise(id, name, series, repetitions,order);
                    exercises.add(exercise);
                }
            } finally {
                // Garantir que o cursor seja fechado corretamente
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }


        return exercises;
    }


    public List<TreinoPlano> getAllTreinosPlanoByUserId(String userId) {
        List<TreinoPlano> treinosPlano = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta SQL para buscar todos os planos de treino de um usuário específico
        Cursor cursor = db.query(TABLE_TREINO_PLANO,
                new String[] {"id", "nome", "user_id"},
                "user_id = ?", // Filtra pelo user_id
                new String[] {String.valueOf(userId)}, // Passa o user_id como parâmetro
                null, null, null);

        // Verifica se há resultados e os adiciona à lista
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // Obtém os valores das colunas
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String nome = cursor.getString(cursor.getColumnIndex("nome"));
                    @SuppressLint("Range") int userIdFromDb = cursor.getInt(cursor.getColumnIndex("user_id"));

                    // Cria o objeto TreinoPlano e adiciona à lista
                    TreinoPlano treinoPlano = new TreinoPlano(id, nome, userIdFromDb);
                    treinosPlano.add(treinoPlano);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return treinosPlano;
    }


    public void deleteTreinoPlanoById(int treinoPlanoId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Iniciar uma transação para garantir a consistência
        db.beginTransaction();
        try {
            // Excluir os exercícios associados ao plano de treino na tabela TREINO_EXERCICIO_PLANO
            String deleteTreinoExercicioPlanoSql = "DELETE FROM " + TABLE_TREINO_EXERCICIO_PLANO + " WHERE treino_id = ?";
            db.execSQL(deleteTreinoExercicioPlanoSql, new Object[]{treinoPlanoId});

            // Excluir o plano de treino na tabela TREINO_PLANO
            String deleteTreinoPlanoSql = "DELETE FROM " + TABLE_TREINO_PLANO + " WHERE id = ?";
            db.execSQL(deleteTreinoPlanoSql, new Object[]{treinoPlanoId});

            // Commitar a transação
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Em caso de erro, a transação será revertida
            e.printStackTrace();
        } finally {
            // Fim da transação, garantindo o fechamento
            db.endTransaction();
        }
    }


    public void deleteExercicioFromPlano(int treinoPlanoId, int exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();


        db.beginTransaction();
        try {

            Log.d("DeleteExercicio", "TreinoPlanoId: " + treinoPlanoId + ", ExerciseId: " + exerciseId);

            String deleteExercicioPlanoSql = "DELETE FROM " + TABLE_TREINO_EXERCICIO_PLANO + " WHERE treino_id = ? AND exercicio_id = ?";
            Log.d("DeleteExercicio", "SQL: " + deleteExercicioPlanoSql);


            db.execSQL(deleteExercicioPlanoSql, new Object[]{treinoPlanoId, exerciseId});

            db.setTransactionSuccessful();

        } catch (Exception e) {
            // Em caso de erro, a transação será revertida
            e.printStackTrace();

        } finally {
            // Fim da transação, garantindo o fechamento
            db.endTransaction();
            db.close();
        }
    }


    public void insertExercicioFromPlano(int treinoPlanoId, int exerciseId,int series, int repeticoes,int order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues treinoExercicioValues = new ContentValues();
        treinoExercicioValues.put("exercicio_id", exerciseId);
        treinoExercicioValues.put("treino_id", treinoPlanoId);  // Usar o treino_id do plano inserido
        treinoExercicioValues.put("series", series);  // Exemplificando 3 séries
        treinoExercicioValues.put("repeticoes", repeticoes);  // Exemplificando 12 repetições
        treinoExercicioValues.put("order_id", order);

        // Inserir na tabela de associação
        db.insert(TABLE_TREINO_EXERCICIO_PLANO, null, treinoExercicioValues);
    }

    @SuppressLint("Range")
    public int getExerciseIdByName(String exerciseName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int exerciseId = -1;
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_EXERCICIO + " WHERE nome = ?", new String[]{exerciseName});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                exerciseId = cursor.getInt(cursor.getColumnIndex("id"));
            }
            cursor.close();
        }

        db.close();
        return exerciseId;
    }


    /**
     * SO PARA METER DADOS NA DB
     */


    public void inserirPlanosTreino2() {
        SQLiteDatabase db = this.getWritableDatabase();


        // Inserir exercícios
        String[] exercicios = {
                "Supino", "Agachamento", "Rosca Direta", "Desenvolvimento", "Elevação Lateral",
                "Leg Press", "Abdominal", "Flexão de Braço", "Stiff", "Pull-up"
        };


        for (String exercicio : exercicios) {
            // Verificar se o exercício já está na tabela "exercicio", senão, inserir
            ContentValues values = new ContentValues();
            values.put("nome", exercicio);


            // Inserir o exercício
            long exercicioId = db.insert(TABLE_EXERCICIO, null, values);
            System.out.println(exercicioId);

        }


        db.close();
    }

    // Função auxiliar para inserir plano de treino e retornar o ID
    private long insertPlanoTreino(SQLiteDatabase db, String nome, int userId) {
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("user_id", userId);

        // Inserir plano de treino e retornar o ID gerado
        return db.insert(TABLE_TREINO_PLANO, null, values);
    }


    public Utilizador loginUser(String username, String password) {
        Utilizador user = new Utilizador();
        SQLiteDatabase db = this.getReadableDatabase(); // Assuming you are working in a SQLiteOpenHelper class

        // Query to fetch the user by username
        String query = "SELECT id, username FROM " + TABLE_UTILIZADOR + " WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username,password});

        if (cursor.moveToFirst()) {
            // Retrieve the data from the cursor
            user.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        }else{
            user=null;
        }
        cursor.close();
        db.close();
        return user;
    }

    public void addUser(Utilizador user, String password){
        SQLiteDatabase db = this.getWritableDatabase(); // Use getWritableDatabase for inserting data

        // Insert query to add a new user
        String query = "INSERT INTO " + TABLE_UTILIZADOR +  "(id, username, password) VALUES (?, ?, ?)";

        // Execute the query to insert a new user
        db.execSQL(query, new Object[]{user.getId(), user.getUsername(), password});

        db.close(); // Close the database connection
    }



    public List<Exercicio> getAllExercicios() {
        List<Exercicio> exercicios = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_EXERCICIO,
                new String[]{"id", "nome"},
                null,
                null,
                null,
                null,
                null);


        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // Obtém o id e nome do exercício
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String nome = cursor.getString(cursor.getColumnIndex("nome"));


                    Exercicio exercicio = new Exercicio(id, nome);
                    exercicios.add(exercicio);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return exercicios;
    }



    public long createPlan(String planName, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Inserir valores para a nova entrada
        values.put("nome", planName);
        values.put("user_id", userId);

        // Inserir na tabela e retornar o ID gerado
        long newPlanId = db.insert(TABLE_TREINO_PLANO, null, values);

        if (newPlanId == -1) {
            Log.e("DatabaseError", "Erro ao inserir plano na tabela TREINO_PLANO");
        } else {
            Log.d("DatabaseSuccess", "Plano inserido com sucesso, ID: " + newPlanId);
        }

        db.close();
        return newPlanId;
    }


    public void updateExerciseOrderInPlan(int treinoId, List<Exercise> exerciseList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            for (int i = 0; i < exerciseList.size(); i++) {
                Exercise exercise = exerciseList.get(i);
                // Atualizar a ordem do exercício no banco de dados
                ContentValues values = new ContentValues();
                values.put("order_id", i); // A nova ordem é a posição no list (índice)
                db.update(TABLE_TREINO_EXERCICIO_PLANO, values, "exercicio_id = ? AND treino_id = ?",
                        new String[]{String.valueOf(exercise.getId()), String.valueOf(treinoId)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void updateExerciseDetailsInPlan(int treinoId, List<Exercise> exerciseList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            for (Exercise exercise : exerciseList) {
                // Atualizar séries e repetições no banco de dados
                ContentValues values = new ContentValues();
                values.put("series", exercise.getSeries()); // Atualiza o número de séries
                values.put("repeticoes", exercise.getRepetitions()); // Atualiza o número de repetições

                db.update(TABLE_TREINO_EXERCICIO_PLANO, values, "exercicio_id = ? AND treino_id = ?",
                        new String[]{String.valueOf(exercise.getId()), String.valueOf(treinoId)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    //getExerciseExecutionsOverTime
    public Map<String, List<Integer>> getExecucoesPorExercicio(String userId, int exercicioId) {
        Map<String, List<Integer>> execucoesMap = new HashMap<>();

        // Passo 1: Buscar todos os treinos feitos por este usuário
        List<TreinosDone> treinosDone = getAllTreinosDoneForUser(userId);

        SQLiteDatabase db = this.getReadableDatabase();

        for (TreinosDone treinoDone : treinosDone) {
            int treinoId = treinoDone.getTreino_id();
            int execucao = treinoDone.getExec();
            String data = treinoDone.getData();

            // Passo 2: Verificar se o exercício está presente neste treino
            List<Exercise> exercises = getExercisesForTraining(treinoId);
            for (Exercise exercise : exercises) {
                if (exercise.getId() == exercicioId) {
                    // Passo 3: Buscar execuções para o exercício específico neste treino
                    Map<Integer, Integer> seriesMap = getExecucoesForExercicio(db, treinoId, exercicioId, execucao);
                    System.out.println("yah" + seriesMap);

                    // Passo 4: Associar a data e os pesos no Map
                    if (!seriesMap.isEmpty()) {
                        // Se já existe uma lista de pesos para a data, apenas adicionamos os novos pesos
                        if (!execucoesMap.containsKey(data)) {
                            execucoesMap.put(data, new ArrayList<>());
                        }

                        // Adiciona todos os pesos das séries encontradas ao mapa de execução
                        execucoesMap.get(data).addAll(seriesMap.values());
                    }
                }
            }
        }

        db.close();
        return execucoesMap;
    }

    // Função para buscar as execuções de um exercício em um treino
    public Map<Integer, Integer> getExecucoesForExercicio(SQLiteDatabase db, int treinoId, int exercicioId, int execucao) {
        Map<Integer, Integer> seriesMap = new HashMap<>();

        Cursor cursor = db.query(
                TABLE_SERIES,
                new String[] {"peso", "numero_serie"},
                "treino_exercicio_id = ? AND plano_id = ? AND exec = ?", // Filtra pelo treino_exercicio_id e plano_id
                new String[] {String.valueOf(exercicioId), String.valueOf(treinoId), String.valueOf(execucao)}, // Passa os valores
                null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int peso = cursor.getInt(cursor.getColumnIndex("peso"));
                    @SuppressLint("Range") int numeroSerie = cursor.getInt(cursor.getColumnIndex("numero_serie"));
                    seriesMap.put(numeroSerie, peso); // Adiciona a série ao mapa
                } while (cursor.moveToNext());
            }
            cursor.close();
        }



        return seriesMap;
    }



    // Função para buscar os treinos feitos por um usuário específico
    public List<TreinosDone> getAllTreinosDoneForUser(String userId) {
        List<TreinosDone> treinos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TREINO_DONE,
                new String[] {"id", "treino_id", "data", "exec", "user_id"},
                "user_id = ?", // Filtra pelo user_id
                new String[] {String.valueOf(userId)},
                null, null, "data"
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") int treino_id = cursor.getInt(cursor.getColumnIndex("treino_id"));
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("data"));
                    @SuppressLint("Range") int exec = cursor.getInt(cursor.getColumnIndex("exec"));
                    TreinosDone treino = new TreinosDone(id, treino_id, data, exec);
                    treinos.add(treino);
                    System.out.println("DEUUUUU");
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return treinos;
    }

}