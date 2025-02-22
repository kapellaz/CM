package com.example.finalchallenge;
import android.util.Log;

import com.example.finalchallenge.classes.Exercise;
import com.example.finalchallenge.classes.Utilizador;
import com.example.finalchallenge.classes.SeriesInfo;
import com.example.finalchallenge.classes.TreinoExercicioPlano;
import com.example.finalchallenge.classes.TreinoPlano;
import com.example.finalchallenge.classes.TreinosDone;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirebaseFirestorehelper {


    /**
     * Method that delete the plan an exercise
     * @param planID - plan id
     * @param planName - name of plan
     * @param userId - user id
     */
    public void createPlan(Integer planID,String planName, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        Map<String, Object> planData = new HashMap<>();
        planData.put("id",planID);
        planData.put("nome", planName);
        planData.put("user_id", userId);
        planData.put("valid", 1);


        db.collection("treino_planos")
                .add(planData)
                .addOnSuccessListener(documentReference -> {
                    String newPlanId = documentReference.getId();
                    Log.d("DatabaseSuccess", "Plano inserido com sucesso, ID: " + newPlanId);

                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseError", "Erro ao inserir plano na coleção treino_planos", e);

                });
    }

    /**
     * Method that delete the plan an exercise
     * @param treinoPlanoId - plan id
     * @param exerciseId - exercise id
     * @param series - number of set
     * @param repeticoes - number of repetitions
     * @param order - order exercise
     * @param user_id - user id logged in
     * @param id - id to represent this exercise
     */
    public void insertExercicioFromPlano(Integer treinoPlanoId, int exerciseId, int series, int repeticoes, int order,String user_id,int id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> exerciseData = new HashMap<>();
        exerciseData.put("id",id);
        exerciseData.put("exercicio_id", exerciseId);
        exerciseData.put("treino_id", treinoPlanoId);
        exerciseData.put("series", series);
        exerciseData.put("repeticoes", repeticoes);
        exerciseData.put("order_id", order);
        exerciseData.put("user_id",user_id);


        db.collection("treino_exercicios_plano")
                .add(exerciseData)
                .addOnSuccessListener(documentReference -> {
                    String newDocumentId = documentReference.getId();
                    Log.d("DatabaseSuccess", "Exercício inserido com sucesso, ID: " + newDocumentId);

                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseError", "Erro ao inserir exercício na coleção treino_exercicios_plano", e);

                });
    }

    /**
     * Method that delete the plan an exercise
     * @param planId - plan id
     * @param userId - user id account
     */
    public void deletePlanAndExercises(Integer planId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: Update the plan's 'valid' field in the treino_planos collection
        db.collection("treino_planos")
                .whereEqualTo("id", planId)
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            db.collection("treino_planos").document(document.getId())
                                    .update("valid", 0)  // Update the 'valid' field to 0
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("DatabaseSuccess", "Plano atualizado com sucesso")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("DatabaseError", "Erro ao atualizar plano", e)
                                    );
                        });
                    } else {
                        Log.d("DatabaseInfo", "Nenhum plano encontrado com o ID especificado.");
                    }
                })
                .addOnFailureListener(e -> Log.e("DatabaseError", "Erro ao buscar plano", e));
    }
    /**
     * Method that delete an exercise from an plan
     * @param treinoPlanoId - number of set
     * @param exerciseId - exercise id
     * @param user_id - user id logged
     */

    public void deleteExercicioFromPlano(int treinoPlanoId, int exerciseId,String user_id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println(exerciseId);
        db.collection("treino_exercicios_plano")
                .whereEqualTo("treino_id", treinoPlanoId)
                .whereEqualTo("id", exerciseId)
                .whereEqualTo("user_id", user_id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> remainingDocuments = new ArrayList<>();


                        queryDocumentSnapshots.getDocuments().forEach(document -> {

                            db.collection("treino_exercicios_plano").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("DatabaseSuccess", "Exercício apagado com sucesso")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("DatabaseError", "Erro ao deletar exercício", e)
                                    );
                        });


                        db.collection("treino_exercicios_plano")
                                .whereEqualTo("treino_id", treinoPlanoId)
                                .whereEqualTo("user_id", user_id)
                                .get()
                                .addOnSuccessListener(updatedQuerySnapshot -> {
                                    if (!updatedQuerySnapshot.isEmpty()) {

                                        List<DocumentSnapshot> sortedDocuments = updatedQuerySnapshot.getDocuments();
                                        Collections.sort(sortedDocuments, (doc1, doc2) -> {
                                            return Integer.compare(doc1.getLong("order_id").intValue(), doc2.getLong("order_id").intValue());
                                        });


                                        for (int i = 0; i < sortedDocuments.size(); i++) {
                                            DocumentSnapshot doc = sortedDocuments.get(i);
                                            int finalI = i;
                                            db.collection("treino_exercicios_plano").document(doc.getId())
                                                    .update("order_id", i + 1)
                                                    .addOnSuccessListener(aVoid ->
                                                            Log.d("DatabaseSuccess", "Order ID atualizado para " + (finalI + 1))
                                                    )
                                                    .addOnFailureListener(e ->
                                                            Log.e("DatabaseError", "Erro ao atualizar order_id", e)
                                                    );
                                        }
                                    } else {
                                        Log.d("DatabaseInfo", "Nenhum exercício encontrado após a exclusão.");
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("DatabaseError", "Erro ao buscar documentos restantes", e));
                    } else {
                        Log.d("DatabaseInfo", "Nenhum exercício encontrado com os IDs especificados.");
                    }
                })
                .addOnFailureListener(e -> Log.e("DatabaseError", "Erro ao buscar exercício", e));
    }

    /**
     * Method that update all exercises orders from an plan
     * @param treinoId - number of set
     * @param exerciseList - Exercise List
     */
    public void updateExerciseOrdersInPlan(int treinoId, List<Exercise> exerciseList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int counter = 1;
        for (Exercise exercise : exerciseList) {

            int finalCounter = counter;

            db.collection("treino_exercicios_plano")
                    .whereEqualTo("treino_id", treinoId)
                    .whereEqualTo("id", exercise.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            queryDocumentSnapshots.getDocuments().forEach(document -> {

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("order_id", finalCounter);
                                db.collection("treino_exercicios_plano")
                                        .document(document.getId())
                                        .update(updates)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("DatabaseSuccess", "Exercício atualizado com sucesso")
                                        )
                                        .addOnFailureListener(e ->
                                                Log.e("DatabaseError", "Erro ao atualizar exercício", e)
                                        );
                            });
                        } else {
                            Log.d("DatabaseInfo", "Nenhum exercício encontrado para atualizar.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("DatabaseError", "Erro ao buscar exercício", e));
            counter++;
        }
    }
    /**
     * Method that update all exercises info from an plan
     * @param treinoId - number of set
     * @param exerciseList - Exercise List
     */
    public void updateExerciseDetailsInPlan(int treinoId, List<Exercise> exerciseList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (Exercise exercise : exerciseList) {
            System.out.println(exerciseList.size() + " DETAILS " +  treinoId + " " + exercise.getId());

            // Consulta para encontrar o documento do exercício específico no plano
            db.collection("treino_exercicios_plano")
                    .whereEqualTo("treino_id", treinoId)
                    .whereEqualTo("id", exercise.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            queryDocumentSnapshots.getDocuments().forEach(document -> {
                                // Preparar os dados para atualizar
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("series", exercise.getSeries());       // Atualiza as séries
                                updates.put("repeticoes", exercise.getRepetitions()); // Atualiza as repetições

                                // Atualizar o documento no Firestore
                                db.collection("treino_exercicios_plano")
                                        .document(document.getId())
                                        .update(updates)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("DatabaseSuccess", "Exercício atualizado com sucesso")
                                        )
                                        .addOnFailureListener(e ->
                                                Log.e("DatabaseError", "Erro ao atualizar exercício", e)
                                        );
                            });
                        } else {
                            Log.d("DatabaseInfo", "Nenhum exercício encontrado para atualizar.");
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("DatabaseError", "Erro ao buscar exercício no Firestore", e)
                    );
        }
    }


    /**
     * Method that inserts one Treino done to firebase
     * @param treinoId - number of set
     * @param data - exercise ID
     * @param treinoId - plan ID
     * @param exec - number of execution
     * @param userId - user id
     */
    public void insertTreinoDone(int treinoId, String data, int exec, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> treinoDoneData = new HashMap<>();
        treinoDoneData.put("treino_id", treinoId);
        treinoDoneData.put("data", data);
        treinoDoneData.put("exec", exec);
        treinoDoneData.put("user_id", userId);

        db.collection("treino_done")
                .add(treinoDoneData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("DatabaseSuccess", "Treino feito inserido com sucesso, ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseError", "Erro ao inserir treino feito na coleção treino_done", e);
                });
    }

    /**
     * Method that inserts Series to firebase
     * @param peso - number of weight executed
     * @param numeroSerie - number of set
     * @param treinoExercicioId - exercise ID
     * @param treinoId - plan ID
     * @param exec - number of execution
     * @param user_id - user id
     * @param oxigenacao  - user oxygenation value
     * @param batimentos - Heart rate value
     */
    public void insertSeries(int peso, int numeroSerie, int treinoExercicioId, int treinoId, int exec,String user_id,int oxigenacao,int batimentos) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        Map<String, Object> seriesData = new HashMap<>();
        seriesData.put("peso", peso);
        seriesData.put("numero_serie", numeroSerie);
        seriesData.put("treino_exercicio_id", treinoExercicioId);
        seriesData.put("plano_id", treinoId);
        seriesData.put("exec", exec);
        seriesData.put("user_id",user_id);
        seriesData.put("oxigenacao",oxigenacao);
        seriesData.put("batimentos",batimentos);


        db.collection("series")
                .add(seriesData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("DatabaseSuccess", "Série inserida com sucesso, ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseError", "Erro ao inserir série na coleção series", e);
                });
    }


    /**
     * Method that get all data from the treino_planos table - firebase
     * @param id - user id
     * @param dblocal - databasehelper
     * @param callback - Callback with Hash Map
     */
    public void getAllPlansFromFirebase(String id, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<TreinoPlano> treinoPlanos = new ArrayList<>();

        db.collection("treino_planos")
                .whereEqualTo("user_id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            String nome = document.getString("nome");
                            String user_id = document.getString("user_id");

                            int valid = Math.toIntExact(document.getLong("valid"));
                            dblocal.createPlan(nome,user_id,valid);
                        });

                        callback.onComplete();
                    } else {
                        Log.d("DatabaseInfo", "Nenhum exercício encontrado para atualizar.");
                        callback.onComplete();
                    }
                })
                .addOnFailureListener(e -> {Log.e("DatabaseError", "Erro ao buscar exercício", e);callback.onComplete();});
    }

    /**
     * Method that get all data from the treino_exercicios_plano table - firebase
     * @param id - user id
     * @param dblocal - databasehelper
     * @param callback - Callback with Hash Map
     */
    public void getAllPlansExerciseFromFirebase(String id, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("treino_exercicios_plano")
                .whereEqualTo("user_id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            Integer exercicio_id = document.getLong("exercicio_id").intValue();
                            Integer treino_id = document.getLong("treino_id").intValue();
                            Integer series = document.getLong("series").intValue();
                            Integer repeticoes = document.getLong("repeticoes").intValue();
                            Integer order_id = document.getLong("order_id").intValue();
                            dblocal.insertExercicioFromPlano(treino_id,exercicio_id,series,repeticoes,order_id);
                            System.out.println(treino_id + " " + exercicio_id + " " + series);
                        });
                        callback.onComplete();

                    } else {
                        callback.onComplete();
                        Log.d("DatabaseInfo", "Nenhum plano de exercício encontrado na Firebase.");
                    }
                })
                .addOnFailureListener(e ->{ Log.e("DatabaseError", "Erro ao buscar planos de exercício na Firebase", e);callback.onComplete();});

    }

    /**
     * Method that get all data from the treino_done table - firebase
     * @param id - user id
     * @param dblocal - databasehelper
     * @param callback - Callback with Hash Map
     */
    public void getAllTreinoDoneFromFirebase(String id, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("treino_done")
                .whereEqualTo("user_id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        queryDocumentSnapshots.getDocuments().forEach(document -> {

                            String data = document.getString("data");
                            Long treino_id = document.getLong("treino_id");
                            Long exec = document.getLong("exec");
                            dblocal.inserttreinodone(Math.toIntExact(treino_id),data, Math.toIntExact(exec),id);
                          });
                        callback.onComplete();
                    } else {
                        callback.onComplete();
                        Log.d("DatabaseInfo", "Nenhum plano de exercício encontrado na Firebase.");
                    }
                })
                .addOnFailureListener(e ->{Log.e("DatabaseError", "Erro ao buscar planos de exercício na Firebase", e);callback.onComplete();});

    }

    /**
     * Method that get all data from the treino_planos table - firebase
     * @param id - user id
     * @param dblocal - databasehelper
     * @param callback - Callback with Hash Map
     */
    public void getAllSeriesFromFirebase(String id, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("series")
                .whereEqualTo("user_id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().forEach(document -> {

                            Long batimentos = document.getLong("batimentos");
                            Long exec = document.getLong("exec");
                            Long numero_serie = document.getLong("numero_serie");
                            Long oxigenacao = document.getLong("oxigenacao");
                            Long peso = document.getLong("peso");
                            Long exercicio_id = document.getLong("treino_exercicio_id");
                            Long plano_id = document.getLong("plano_id");
                            dblocal.insertSeries(Math.toIntExact(peso), Math.toIntExact(numero_serie), Math.toIntExact(exercicio_id), Math.toIntExact(plano_id), Math.toIntExact(exec), Math.toIntExact(oxigenacao), Math.toIntExact(batimentos));


                        });
                        callback.onComplete();

                    } else {
                        Log.d("DatabaseInfo", "Nenhum plano de exercício encontrado na Firebase.");
                        callback.onComplete();
                    }
                })
                .addOnFailureListener(e -> {Log.e("DatabaseError", "Erro ao buscar planos de exercício na Firebase", e);callback.onComplete();});

    }

    /**
     * Method that synchronizes data from the treino_planos table with firebase
     * @param userId - user id
     * @param dblocal - databasehelper
     * @param callback - Callback with Hash Map
     */
    public void syncLocalDataToFirebasePLANOS(String userId, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        List<TreinoPlano> localPlans = dblocal.getAllTreinoPlanos_sync(userId);
        Set<String> localPlanNames = new HashSet<>();
        localPlans.forEach(plan -> localPlanNames.add(plan.getNome()));


        db.collection("treino_planos")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> firebasePlanIDS = new HashMap<>();
                    Map<Integer,String> firebasePlanDocumentIDS = new HashMap<>();

                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        Integer id_plano = Math.toIntExact(document.getLong("id"));
                        String nome = document.getString("nome");


                        firebasePlanIDS.put(nome,id_plano);
                        firebasePlanDocumentIDS.put(id_plano, document.getId());
                    });


                    for (TreinoPlano localPlan : localPlans) {
                        System.out.println(localPlan + "  " + localPlan.getUserId());
                        System.out.println(firebasePlanIDS);
                        if (firebasePlanIDS.containsKey(localPlan.getNome()) && firebasePlanIDS.containsValue(localPlan.getId())) {

                            db.collection("treino_planos")
                                        .document(firebasePlanDocumentIDS.get(localPlan.getId()))
                                        .set(localPlan.toMap())
                                        .addOnSuccessListener(aVoid -> Log.d("FirebaseSync", "Plano atualizado: " + localPlan.getNome()))
                                        .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao atualizar plano: " + localPlan.getNome(), e));

                        } else {

                            db.collection("treino_planos")
                                    .add(localPlan.toMap())
                                    .addOnSuccessListener(documentReference -> Log.d("FirebaseSync", "Plano adicionado: " + localPlan.getNome()))
                                    .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao adicionar plano: " + localPlan.getNome(), e));
                        }
                    }


                    for (Map.Entry<Integer, String> entry : firebasePlanDocumentIDS.entrySet()) {
                        Integer idPlano = entry.getKey();
                        String firebaseId = entry.getValue();
                        boolean existsLocally = localPlans.stream()
                                .anyMatch(plan -> plan.getId() == idPlano);

                        if (!existsLocally) {

                            db.collection("treino_planos")
                                    .document(String.valueOf(firebaseId))
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d("FirebaseSync", "Plano removido: " + idPlano))
                                    .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao remover plano: " + idPlano, e));
                        }
                    }
                    callback.onComplete();



                })
                .addOnFailureListener(e -> {
                    Log.e("SyncError", "Erro ao buscar dados do Firebase", e);
                    callback.onComplete();
                });

    }


    /**
     * Method that synchronizes data from the Treinos_exercicios table with firebase
     * @param userId - user id
     * @param dblocal - databasehelper
     * @param callback - Callback with Hash Map
     */
    public void syncLocalDataToFirebaseExercicios(String userId, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obter dados locais
        List<TreinoExercicioPlano> localExercicios = dblocal.getAllTreinoExercicioPlanos_sync(userId);
        Set<Integer> localExercicioNames = new HashSet<>();
        localExercicios.forEach(exercicio -> localExercicioNames.add(exercicio.getExercicio_id()));


        db.collection("treino_exercicios_plano")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    Map<Integer, Integer> firebaseExercicioIDS = new HashMap<>();
                    Map<Integer, String> firebaseExercicioDocumentIDS = new HashMap<>();

                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        Integer id_exercicio = Math.toIntExact(document.getLong("exercicio_id"));
                        Integer id_plan_exercicio = Math.toIntExact(document.getLong("id"));


                        firebaseExercicioIDS.put(id_plan_exercicio, id_exercicio);
                        firebaseExercicioDocumentIDS.put(id_plan_exercicio, document.getId());
                    });


                    for (TreinoExercicioPlano localExercicio : localExercicios) {
                        System.out.println(localExercicio + "  " + localExercicio.getId());
                        System.out.println(firebaseExercicioIDS);


                        if (firebaseExercicioIDS.containsKey(localExercicio.getId()) &&
                                firebaseExercicioIDS.containsValue(localExercicio.getExercicio_id())) {


                            db.collection("treino_exercicios_plano")
                                    .document(firebaseExercicioDocumentIDS.get(localExercicio.getId()))
                                    .set(localExercicio.toMap())
                                    .addOnSuccessListener(aVoid -> Log.d("FirebaseSync", "Exercício atualizado: " + localExercicio.getId()))
                                    .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao atualizar exercício: " + localExercicio.getId(), e));

                        } else {

                            db.collection("treino_exercicios_plano")
                                    .add(localExercicio.toMap())
                                    .addOnSuccessListener(documentReference -> Log.d("FirebaseSync", "Exercício adicionado: " + localExercicio.getId()))
                                    .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao adicionar exercício: " + localExercicio.getId(), e));
                        }
                    }


                    for (Map.Entry<Integer, String> entry : firebaseExercicioDocumentIDS.entrySet()) {
                        Integer idExercicio = entry.getKey();
                        String firebaseId = entry.getValue();
                        boolean existsLocally = localExercicios.stream()
                                .anyMatch(exercicio -> exercicio.getId() == idExercicio);

                        if (!existsLocally) {

                            db.collection("treino_exercicios_plano")
                                    .document(firebaseId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d("FirebaseSync", "Exercício removido: " + idExercicio))
                                    .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao remover exercício: " + idExercicio, e));
                        }
                    }

                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    Log.e("SyncError", "Erro ao buscar dados do Firebase", e);
                    callback.onComplete();
                });
    }


    /**
     * Method that synchronizes data from the TreinoDone table with firebase
     * @param userId - user id
     * @param dblocal - databasehelper
     * @param callback - Callback with Hash Map
     */
    public void syncLocalDataToFirebaseTreinoDone(String userId, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<TreinosDone> localTreinosDone = dblocal.getAllTreinosDoneForUser(userId);


        Set<String> localTreinosDoneKeys = new HashSet<>();
        localTreinosDone.forEach(treinoDone ->
                localTreinosDoneKeys.add(treinoDone.getExec() + "_" + treinoDone.getData() + "_" + treinoDone.getTreino_id())
        );


        db.collection("treino_done")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    Map<String, String> firebaseTreinosDoneKeys = new HashMap<>();
                    Map<String, String> firebaseTreinoDocumentIds = new HashMap<>();

                    queryDocumentSnapshots.getDocuments().forEach(document -> {

                        String data = document.getString("data");
                        Integer treinoId = Math.toIntExact(document.getLong("treino_id"));


                        String key = data + "_" + treinoId;


                        firebaseTreinosDoneKeys.put(key, key);
                        firebaseTreinoDocumentIds.put(key, document.getId());
                    });

                    for (TreinosDone localTreinoDone : localTreinosDone) {
                        String localKey = localTreinoDone.getData() + "_" + localTreinoDone.getTreino_id();


                        if (!firebaseTreinosDoneKeys.containsKey(localKey)) {

                            db.collection("treino_done")
                                    .add(localTreinoDone.toMap(userId))
                                    .addOnSuccessListener(documentReference ->
                                            Log.d("FirebaseSync", "Treino realizado adicionado: " + localTreinoDone.getId()))
                                    .addOnFailureListener(e ->
                                            Log.e("FirebaseError", "Erro ao adicionar treino realizado: " + localTreinoDone.getId(), e));
                        }
                    }

                    for (Map.Entry<String, String> entry : firebaseTreinoDocumentIds.entrySet()) {
                        String firebaseKey = entry.getKey();
                        String firebaseId = entry.getValue();


                        boolean existsLocally = localTreinosDone.stream()
                                .anyMatch(treinoDone ->
                                        (treinoDone.getData() + "_" + treinoDone.getTreino_id()).equals(firebaseKey)
                                );

                        if (!existsLocally) {
                            // Remove do Firebase se não existir localmente
                            db.collection("treino_done")
                                    .document(firebaseId)
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("FirebaseSync", "Treino realizado removido: " + firebaseKey))
                                    .addOnFailureListener(e ->
                                            Log.e("FirebaseError", "Erro ao remover treino realizado: " + firebaseKey, e));
                        }
                    }

                    callback.onComplete();

                })
                .addOnFailureListener(e -> {
                    Log.e("SyncError", "Erro ao buscar dados do Firebase", e);
                    callback.onComplete();
                });
    }


    /**
     * Method that synchronizes data from the series table with firebase
     * @param userId - user id
     * @param dblocal - databasehelper
     * @param callback - Callback with Hash Map
     */

    public void syncLocalDataToFirebaseSerie(String userId, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        List<SeriesInfo> localSeries = dblocal.getSeriesByUserId(userId);


        Set<String> localSerieKeys = new HashSet<>();
        localSeries.forEach(serie ->
                localSerieKeys.add(serie.getTreinoId() + "_" + serie.getSeries() + "_" + serie.getExercicioId() +"_" + serie.getExec())
        );


        db.collection("series")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    Map<String, String> firebaseSerieKeys = new HashMap<>();
                    Map<String, String> firebaseSerieDocumentIds = new HashMap<>();

                    queryDocumentSnapshots.getDocuments().forEach(document -> {

                        Integer treinoExercicioId = Math.toIntExact(document.getLong("treino_exercicio_id"));
                        Integer numeroSerie = Math.toIntExact(document.getLong("numero_serie"));
                        Integer planId = Math.toIntExact(document.getLong("plano_id"));
                        Integer exec = Math.toIntExact(document.getLong("exec"));


                        String key = planId + "_" + numeroSerie + "_" + treinoExercicioId + "_" + exec;


                        firebaseSerieKeys.put(key, key);
                        firebaseSerieDocumentIds.put(key, document.getId());
                    });


                    for (SeriesInfo localSerie : localSeries) {
                        String localKey = localSerie.getTreinoId() + "_" + localSerie.getSeries() + "_" + localSerie.getExercicioId() + "_" + localSerie.getExec();


                        if (!firebaseSerieKeys.containsKey(localKey)) {

                            db.collection("series")
                                    .add(localSerie.toMap(userId))
                                    .addOnSuccessListener(documentReference ->
                                            Log.d("FirebaseSync", "Série adicionada: " + localSerie.getTreinoId() + " " + localSerie.getExercicioId() ))
                                    .addOnFailureListener(e ->
                                            Log.e("FirebaseError", "Erro ao adicionar série: " + localSerie.getTreinoId()+ " " + localSerie.getExercicioId(), e));
                        }

                    }


                    for (Map.Entry<String, String> entry : firebaseSerieDocumentIds.entrySet()) {
                        String firebaseKey = entry.getKey();
                        String firebaseId = entry.getValue();


                        boolean existsLocally = localSeries.stream()
                                .anyMatch(serie ->
                                        (serie.getTreinoId() + "_" + serie.getSeries() + "_" + serie.getExercicioId() + "_" + serie.getExec()).equals(firebaseKey)
                                );

                        if (!existsLocally) {

                            db.collection("series")
                                    .document(firebaseId)
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("FirebaseSync", "Série removida: " + firebaseKey))
                                    .addOnFailureListener(e ->
                                            Log.e("FirebaseError", "Erro ao remover série: " + firebaseKey, e));

                        }
                    }
                    callback.onComplete();



                })
                .addOnFailureListener(e -> {
                    callback.onComplete();
                    Log.e("SyncError", "Erro ao buscar dados do Firebase", e);
                });
    }

    /**
     * Method that gets all friends from an especific user
     * @param userID - user id
     * @param callback - Callback with Hash Map
     */

    public void getAllFriends(String userID, exercise_detail.FriendsCallback callback) {
        List<String> amigosIDs = new ArrayList<>();
        List<Utilizador> amigos = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("amigos")
                .whereEqualTo("user1", userID)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        // Processar resultados da primeira consulta
                        if (!task1.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                amigosIDs.add(document.getString("user2"));
                            }
                        }

                        db.collection("amigos")
                                .whereEqualTo("user2", userID)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {

                                        if (!task2.getResult().isEmpty()) {
                                            for (QueryDocumentSnapshot document : task2.getResult()) {
                                                amigosIDs.add(document.getString("user1"));
                                            }
                                        }


                                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                                        for (String amigoID : amigosIDs) {
                                            tasks.add(db.collection("users").document(amigoID).get());
                                        }

                                        Tasks.whenAllSuccess(tasks).addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()) {
                                                for (Task<DocumentSnapshot> t : tasks) {
                                                    DocumentSnapshot document = t.getResult();
                                                    Utilizador amigo = new Utilizador(document.getString("username"), document.getId());
                                                    amigos.add(amigo);
                                                }


                                                callback.onFriendsFetched(amigos);
                                            }
                                        });
                                    }
                                });
                    }
                });
    }

    /**
     * Method that gets all executions from an especific user
     * @param userId - user id
     * @param exercicioId - exercise id
     * @param callback - Callback with Hash Map
     */

    public void getExecucoesPorExercicio(String userId, int exercicioId, exercise_detail.DetalhesTrainFriend callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, List<String>> execucoesMap = new HashMap<>();

        db.collection("treino_done")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> treinosDoneDocs = task.getResult().getDocuments();
                        for (DocumentSnapshot treinoDoc : treinosDoneDocs) {
                            int treinoId = treinoDoc.getLong("treino_id").intValue();
                            String data = treinoDoc.getString("data");
                            db.collection("series")
                                    .whereEqualTo("user_id",userId)
                                    .whereEqualTo("plano_id", treinoId)
                                    .get()
                                    .addOnCompleteListener(taskExercises -> {
                                        if (taskExercises.isSuccessful()) {
                                            List<DocumentSnapshot> exerciseDocs = taskExercises.getResult().getDocuments();
                                            if(exerciseDocs.isEmpty()){
                                                callback.onDetalhes(execucoesMap);
                                            }
                                            for (DocumentSnapshot exerciseDoc : exerciseDocs) {


                                                if (exerciseDoc.getLong("treino_exercicio_id").intValue() == exercicioId) {

                                                    int peso = exerciseDoc.getLong("peso").intValue();
                                                    int batimentos = exerciseDoc.getLong("batimentos").intValue();
                                                    int oxigenacao = exerciseDoc.getLong("oxigenacao").intValue();

                                                    if (!execucoesMap.containsKey(data)) {
                                                        execucoesMap.put(data, new ArrayList<>());
                                                    }
                                                    String info = peso + "|" + batimentos + "|" + oxigenacao;
                                                    System.out.println(info);
                                                    execucoesMap.get(data).add(info);

                                                }
                                            }
                                            callback.onDetalhes(execucoesMap);

                                        }
                                        callback.onDetalhes(execucoesMap);
                                    });

                        }
                        callback.onDetalhes(execucoesMap);
                    }
                });
    }





}
