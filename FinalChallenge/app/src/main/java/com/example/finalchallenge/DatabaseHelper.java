package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.finalchallenge.classes.Exercicio;
import com.example.finalchallenge.classes.Exercise;
import com.example.finalchallenge.classes.ExerciseDetailed;
import com.example.finalchallenge.classes.SeriesInfo;
import com.example.finalchallenge.classes.TreinoExercicioPlano;
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
    private static final int DATABASE_VERSION = 52;

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
            "valid INTEGER, " +
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
            "oxigenacao INTEGER," +
            "batimentos INTEGER," +
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

    /**
     * Method that get trainingexecs
     * @param id - treino id
     */
    @SuppressLint("Range")
    public int get_training_execs(int id){

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

    /**
     * Method that insert done training to DB
     * @params - info need to insert sets
     */
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






    /**
     * Method that insert sets to DB
     * @params - info need to insert sets
     */
    public void insertSeries(int peso, int numero_serie, int treino_exercicio_id, int treino_id, int exec,int oxigenacao,int batimentos) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("peso", peso);
        values.put("treino_exercicio_id", treino_exercicio_id);
        values.put("numero_serie", numero_serie);
        values.put("plano_id", treino_id);
        values.put("exec", exec);
        values.put("oxigenacao",oxigenacao);
        values.put("batimentos",batimentos);
        db.insert(TABLE_SERIES, null, values);
        db.close();
    }



    /**
     * Method that get list of done training by user id
     * @param userId - user id
     * @return list of done training
     */
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




    /**
     * Method that get details from an train done from a plan
     * @param treinoDone - train info
     * @return details
     */

    public TreinosDetails getTreinoDetails(TreinosDone treinoDone) {

        int treinoId = treinoDone.getTreino_id();
        TreinosDetails treinoDetails = new TreinosDetails(treinoDone.getId(), treinoDone.getTreino_id(), treinoDone.getData(), new ArrayList<>(),treinoDone.getExec());
        SQLiteDatabase db = this.getReadableDatabase();

        List<Exercise> exercises = getExercisesForTraining(treinoId);


        List<ExerciseDetailed> exercisesDetailed = new ArrayList<>();

        for (Exercise exercise : exercises) {

            Map<Integer, Integer> seriesMap = new HashMap<>();

            Cursor cursor = db.query(
                    TABLE_SERIES,
                    new String[] {"peso", "numero_serie"},
                    "treino_exercicio_id = ? AND plano_id = ? AND exec = ?",
                    new String[] {String.valueOf(exercise.getId_exercicio()), String.valueOf(treinoId), String.valueOf(treinoDone.getExec())},
                    null, null, null
            );



            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int peso = cursor.getInt(cursor.getColumnIndex("peso"));
                        @SuppressLint("Range") int numeroSerie = cursor.getInt(cursor.getColumnIndex("numero_serie"));
                        seriesMap.put(numeroSerie, peso);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            ExerciseDetailed exerciseDetailed = new ExerciseDetailed(exercise, seriesMap);
            exercisesDetailed.add(exerciseDetailed);
        }

        treinoDetails.setExercise(exercisesDetailed);
        TreinosDetails details = treinoDetails;


        StringBuilder detailsText = new StringBuilder();
        for (ExerciseDetailed exercise : details.getExercise()) {
            detailsText.append(exercise);
            detailsText.append("Series Information:\n");

            for (Map.Entry<Integer, Integer> entry : exercise.getSeriesMap().entrySet()) {
                detailsText.append("Set ").append(entry.getKey())
                        .append(": ").append(entry.getValue()).append(" kilos\n");
            }
        }

        return treinoDetails;
    }


    /**
     * Method that get exeercises from a plan
     * @param treinoId - user id
     * @return list of exercise
     */

    public List<Exercise> getExercisesForTraining(int treinoId) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();


        String query = "SELECT te.id, e.nome, te.series, te.repeticoes, te.order_id, te.exercicio_id " +
                "FROM " + TABLE_EXERCICIO + " e " +
                "INNER JOIN " + TABLE_TREINO_EXERCICIO_PLANO + " te ON e.id = te.exercicio_id " +
                "WHERE te.treino_id = ? order by te.order_id ASC";


        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(treinoId)});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                    int series = cursor.getInt(cursor.getColumnIndexOrThrow("series"));
                    int repetitions = cursor.getInt(cursor.getColumnIndexOrThrow("repeticoes"));
                    int order = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                    int id_exercicio = cursor.getInt(cursor.getColumnIndexOrThrow("exercicio_id"));

                    Exercise exercise = new Exercise(id,id_exercicio, name, series, repetitions,order);
                    exercises.add(exercise);
                    System.out.println(exercise);
                }
            } finally {

                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }


        return exercises;
    }

    /**
     * Method that get all plans by user id
     * @param userId - user id
     * @return list of treino plano
     */
    public List<TreinoPlano> getAllTreinosPlanoByUserId(String userId) {
        List<TreinoPlano> treinosPlano = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta SQL para buscar todos os planos de treino de um usuário específico
        Cursor cursor = db.query(TABLE_TREINO_PLANO,
                new String[] {"id", "nome", "user_id","valid"},
                "user_id = ? AND valid = 1", // Filtra pelo user_id e validação
                new String[] {String.valueOf(userId)}, // Passa o user_id como parâmetro
                null, null, null);


        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String nome = cursor.getString(cursor.getColumnIndex("nome"));
                    @SuppressLint("Range") String userIdFromDb = cursor.getString(cursor.getColumnIndex("user_id"));
                    @SuppressLint("Range") int valid = cursor.getInt(cursor.getColumnIndex("valid"));

                    TreinoPlano treinoPlano = new TreinoPlano(id, nome, userIdFromDb,valid);
                    treinosPlano.add(treinoPlano);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return treinosPlano;
    }

    /**
     * Method that delete a plan by id
     * @param treinoPlanoId - plan id
     */
    public void deleteTreinoPlanoById(int treinoPlanoId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Iniciar uma transação para garantir a consistência
        db.beginTransaction();
        try {
            String updateTreinoPlanoSql = "UPDATE " + TABLE_TREINO_PLANO + " SET valid = 0 WHERE id = ?";
            db.execSQL(updateTreinoPlanoSql, new Object[]{treinoPlanoId});


            db.setTransactionSuccessful();
        } catch (Exception e) {

            e.printStackTrace();
        } finally {

            db.endTransaction();
        }
    }

    /**
     * Method that delete an exercise from plan
     * @param treinoPlanoId - plan id
     * @param exerciseId - exercise id
     * @param user_id - user id
     */
    public void deleteExercicioFromPlano(int treinoPlanoId, Exercise exerciseId,String user_id) {
        SQLiteDatabase db = this.getWritableDatabase();


        db.beginTransaction();
        try {

            Log.d("DeleteExercicio", "TreinoPlanoId: " + treinoPlanoId + ", ExerciseId: " + exerciseId);

            String deleteExercicioPlanoSql = "DELETE FROM " + TABLE_TREINO_EXERCICIO_PLANO + " WHERE id = ? and treino_id= ?";
            Log.d("DeleteExercicio", "SQL: " + deleteExercicioPlanoSql);


            db.execSQL(deleteExercicioPlanoSql, new Object[]{exerciseId.getId(),treinoPlanoId});

            db.setTransactionSuccessful();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Method that insert a new exercise to plan
     * @params - info needed
     */

    public long insertExercicioFromPlano(int treinoPlanoId, int exerciseId,int series, int repeticoes,int order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues treinoExercicioValues = new ContentValues();
        treinoExercicioValues.put("exercicio_id", exerciseId);
        treinoExercicioValues.put("treino_id", treinoPlanoId);
        treinoExercicioValues.put("series", series);
        treinoExercicioValues.put("repeticoes", repeticoes);
        treinoExercicioValues.put("order_id", order);
        return db.insert(TABLE_TREINO_EXERCICIO_PLANO, null, treinoExercicioValues);
    }

    /**
     * Method that returns exercise id by name
     * @param exerciseName - exercise name
     * @return exercise id
     */
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
     * Method that check if user exists to login
     * @param username - username
     * @param password - password of user
     */

    public Utilizador loginUser(String username, String password) {
        Utilizador user = new Utilizador();
        SQLiteDatabase db = this.getReadableDatabase();


        String query = "SELECT id, username FROM " + TABLE_UTILIZADOR + " WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username,password});

        if (cursor.moveToFirst()) {

            user.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        }else{
            user=null;
        }
        cursor.close();
        db.close();
        return user;
    }

    /**
     * Method to add a new user
     * @param user - Class user (all info needed)
     * @param password - password of user
     */
    public void addUser(Utilizador user, String password){
        SQLiteDatabase db = this.getWritableDatabase();


        String query = "INSERT INTO " + TABLE_UTILIZADOR +  "(id, username, password) VALUES (?, ?, ?)";


        db.execSQL(query, new Object[]{user.getId(), user.getUsername(), password});

        db.close();
    }

    /**
     * Method that gets all exercises
     * @return List of exercise
     */
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

    /**
     * Method that creates a plan into DB
     * @param planName - plan name
     * @param userId - exercise list to be sent to DB
     * @param valid - 0 if is eliminated | 1 if is not eliminated
     */

    public long createPlan(String planName, String userId,int valid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();


        values.put("nome", planName);
        values.put("user_id", userId);
        values.put("valid", valid);


        long newPlanId = db.insert(TABLE_TREINO_PLANO, null, values);

        if (newPlanId == -1) {
            Log.e("DatabaseError", "Erro ao inserir plano na tabela TREINO_PLANO");
        } else {
            Log.d("DatabaseSuccess", "Plano inserido com sucesso, ID: " + newPlanId);
        }

        db.close();
        return newPlanId;
    }

    /**
     * Method that updates exercise order
     * @param treinoId - plan id
     * @param exerciseList - exercise list to be sent to DB
     */
    public void updateExerciseOrderInPlan(int treinoId, List<Exercise> exerciseList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            for (int i = 0; i < exerciseList.size(); i++) {
                Exercise exercise = exerciseList.get(i);

                ContentValues values = new ContentValues();
                values.put("order_id", i);
                db.update(TABLE_TREINO_EXERCICIO_PLANO, values, "id = ? AND treino_id = ?",
                        new String[]{String.valueOf(exercise.getId()), String.valueOf(treinoId)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Method that updates exercise details info (reps, sets)
     * @param treinoId - plan id
     * @param exerciseList - exercise list to be sent to DB
     */
    public void updateExerciseDetailsInPlan(int treinoId, List<Exercise> exerciseList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            for (Exercise exercise : exerciseList) {
                ContentValues values = new ContentValues();
                values.put("series", exercise.getSeries());
                values.put("repeticoes", exercise.getRepetitions());
                db.update(TABLE_TREINO_EXERCICIO_PLANO, values, "id = ? AND treino_id = ?",
                        new String[]{String.valueOf(exercise.getId()), String.valueOf(treinoId)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Method that get all executions exercise for an user id (using exercise ID) - to make a graph
     * @param userId - user id
     * @param exercicioId - number of exercise ID
     * @return returns a list of SeriesInfo
     */

    public Map<String, List<String>> getExecucoesPorExercicio(String userId, int exercicioId) {
        Map<String, List<String>> execucoesMap = new HashMap<>();
        List<TreinosDone> treinosDone = getAllTreinosDoneForUser(userId);

        SQLiteDatabase db = this.getReadableDatabase();

        for (TreinosDone treinoDone : treinosDone) {
            int treinoId = treinoDone.getTreino_id();
            int execucao = treinoDone.getExec();
            String data = treinoDone.getData();


            List<Exercise> exercises = getExercisesForTraining(treinoId);

            for (Exercise exercise : exercises) {

                if (exercise.getId_exercicio() == exercicioId) {

                    Map<Integer, String> seriesMap = getExecucoesForExercicio(db, treinoId, exercise.getId(), execucao);



                    if (!seriesMap.isEmpty()) {

                        if (!execucoesMap.containsKey(data)) {
                            execucoesMap.put(data, new ArrayList<>());
                        }
                        execucoesMap.get(data).addAll(seriesMap.values());
                    }
                }
            }
        }

        db.close();
        return execucoesMap;
    }


    /**
     * Method that get all executions exercise for an user id (using exercise ID) - to make a graph
     * @param treinoId - plan id
     * @param execucao - number of executions
     * @param exercicioId - number of exercise ID
     * @return returns a list of SeriesInfo
     */
    public Map<Integer, String> getExecucoesForExercicio(SQLiteDatabase db, int treinoId, int exercicioId, int execucao) {
        Map<Integer, String> seriesMap = new HashMap<>();

        Cursor cursor = db.query(
                TABLE_SERIES,
                new String[] {"peso", "numero_serie","batimentos","oxigenacao"},
                "treino_exercicio_id = ? AND plano_id = ? AND exec = ?", // Filtra pelo treino_exercicio_id e plano_id
                new String[] {String.valueOf(exercicioId), String.valueOf(treinoId), String.valueOf(execucao)}, // Passa os valores
                null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int peso = cursor.getInt(cursor.getColumnIndex("peso"));
                    @SuppressLint("Range") int numeroSerie = cursor.getInt(cursor.getColumnIndex("numero_serie"));
                    @SuppressLint("Range") int batimentos = cursor.getInt(cursor.getColumnIndex("batimentos"));
                    @SuppressLint("Range") int oxigenacao = cursor.getInt(cursor.getColumnIndex("oxigenacao"));
                    String info = peso + "|" + batimentos + "|" + oxigenacao;
                    seriesMap.put(numeroSerie, info); // Adiciona a série ao mapa
                } while (cursor.moveToNext());
            }
            cursor.close();
        }



        return seriesMap;
    }


    /**
     * Method that get all Treinos Done for an user id
     * @param userId - user id
     * @return returns a list of SeriesInfo
     */
    public List<TreinosDone> getAllTreinosDoneForUser(String userId) {
        List<TreinosDone> treinos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TREINO_DONE,
                new String[] {"id", "treino_id", "data", "exec", "user_id"},
                "user_id = ?",
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
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return treinos;
    }



    /**
     * Method that get all plans to syncronized with firebase
     * @param user_id2 - user id
     * @return returns a list of SeriesInfo
     */
    public ArrayList<TreinoPlano> getAllTreinoPlanos_sync(String user_id2) {
        ArrayList<TreinoPlano> treinoPlanosList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TREINO_PLANO + " WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{user_id2});


        if (cursor.moveToFirst()) {
            do {

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
                @SuppressLint("Range") int valid = cursor.getInt(cursor.getColumnIndex("valid"));

                TreinoPlano treinoPlano = new TreinoPlano(id, nome, userId,valid);
                treinoPlanosList.add(treinoPlano);
            } while (cursor.moveToNext());
        }


        cursor.close();
        db.close();

        return treinoPlanosList;
    }

    /**
     * Method that get all exercises from an plan to syncronized with firebase
     * @return returns a list of SeriesInfo
     */
    @SuppressLint("Range")
    public ArrayList<TreinoExercicioPlano> getAllTreinoExercicioPlanos_sync(String userId) {
        ArrayList<TreinoExercicioPlano> treinoExercicioPlanos = new ArrayList<>();


        String query = "SELECT tep.id, tep.exercicio_id, tep.treino_id, tep.series, tep.repeticoes, tep.order_id, tp.user_id " +
                "FROM " + TABLE_TREINO_EXERCICIO_PLANO + " AS tep " +
                "JOIN " + TABLE_TREINO_PLANO + " AS tp " +
                "ON tep.treino_id = tp.id " +
                "WHERE tp.user_id = ?";


        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.rawQuery(query, new String[]{userId});


        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    int exercicio_id = cursor.getInt(cursor.getColumnIndexOrThrow("exercicio_id"));
                    int treino_id = cursor.getInt(cursor.getColumnIndexOrThrow("treino_id"));
                    int series = cursor.getInt(cursor.getColumnIndexOrThrow("series"));
                    int repeticoes = cursor.getInt(cursor.getColumnIndexOrThrow("repeticoes"));
                    int order_id = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                    String user_id = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));


                    TreinoExercicioPlano plano = new TreinoExercicioPlano(id, exercicio_id, treino_id, series, repeticoes, order_id, user_id);
                    treinoExercicioPlanos.add(plano);
                } catch (IllegalArgumentException e) {

                    Log.e("DatabaseError", "Coluna não encontrada no Cursor: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseInfo", "Nenhum dado encontrado na tabela.");
        }

        // Feche o cursor e o banco de dados
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return treinoExercicioPlanos;
    }

    /**
     * Method that get all sets by an user
     * @return returns a list of SeriesInfo
     */
    @SuppressLint("Range")
    public List<SeriesInfo> getSeriesByUserId(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<SeriesInfo> seriesList = new ArrayList<>();


        String query = "SELECT s.peso, s.numero_serie, s.treino_exercicio_id, s.plano_id, " +
                "s.exec, s.oxigenacao, s.batimentos " +
                "FROM " + TABLE_SERIES + " AS s " +
                "INNER JOIN " + TABLE_TREINO_PLANO + " AS tp ON s.plano_id = tp.id " +
                "WHERE tp.user_id = ?";


        Cursor cursor = db.rawQuery(query, new String[]{userId});


        if (cursor != null) {
            while (cursor.moveToNext()) {

                int peso = cursor.getInt(cursor.getColumnIndex("peso"));
                int numeroSerie = cursor.getInt(cursor.getColumnIndex("numero_serie"));
                int treinoExercicioId = cursor.getInt(cursor.getColumnIndex("treino_exercicio_id"));
                int planoId = cursor.getInt(cursor.getColumnIndex("plano_id"));
                int exec = cursor.getInt(cursor.getColumnIndex("exec"));
                int oxigenacao = cursor.getInt(cursor.getColumnIndex("oxigenacao"));
                int batimentos = cursor.getInt(cursor.getColumnIndex("batimentos"));


                SeriesInfo serie = new SeriesInfo(peso, numeroSerie, treinoExercicioId, planoId, exec, oxigenacao, batimentos);
                seriesList.add(serie);
            }
            cursor.close();
        }

        return seriesList;
    }


    /**
     * Method that check if the exercise table is empty.
     */
    public boolean isExercisesTableEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EXERCICIO, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count == 0;
        }

        return true;
    }



    /**
     * Method that add news exercise from API
     * @param exercicios - exercise list
     */
    public void AddExerciseAPIintoBD(ArrayList<String> exercicios) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (String exercicio : exercicios) {
            ContentValues values = new ContentValues();
            values.put("nome", exercicio);


            db.insert(TABLE_EXERCICIO, null, values);

        }


        db.close();
    }

    /**
     * Method that returns name of an exercise using an ID.
     * @param exercicioId - exercise
     * @return name of exercise
     */
    public String getExerciseNameById(int exercicioId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String exerciseName = "";

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT nome FROM " + TABLE_EXERCICIO + " WHERE id = ?", new String[]{String.valueOf(exercicioId)});

            if (cursor != null && cursor.moveToFirst()) {
                exerciseName = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return exerciseName;
    }

}