package com.example.finalchallenge;
import android.icu.number.IntegerWidth;
import android.util.Log;

import com.example.finalchallenge.classes.Exercise;
import com.example.finalchallenge.classes.SeriesInfo;
import com.example.finalchallenge.classes.TreinoExercicioPlano;
import com.example.finalchallenge.classes.TreinoPlano;
import com.example.finalchallenge.classes.TreinosDone;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirebaseFirestorehelper {
    private DatabaseHelper databaseHelper;

    public void createPlan(Integer planID,String planName, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the data to be added
        Map<String, Object> planData = new HashMap<>();
        planData.put("id",planID);
        planData.put("nome", planName);
        planData.put("user_id", userId);
        planData.put("valid", 1);

        // Add the document to the treino_planos collection
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

    public void insertExercicioFromPlano(Integer treinoPlanoId, int exerciseId, int series, int repeticoes, int order,String user_id,int id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the data to be added
        Map<String, Object> exerciseData = new HashMap<>();
        exerciseData.put("id",id);
        exerciseData.put("exercicio_id", exerciseId);
        exerciseData.put("treino_id", treinoPlanoId);  // Use the treino_plano ID
        exerciseData.put("series", series);           // Number of series
        exerciseData.put("repeticoes", repeticoes);   // Number of repetitions
        exerciseData.put("order_id", order);          // Order of the exercise
        exerciseData.put("user_id",user_id);

        // Add the document to the treino_exercicios_plano collection
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


    public void deleteExercicioFromPlano(int treinoPlanoId, int exerciseId,String user_id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println(exerciseId);
        // Query the treino_exercicios_plano collection to find the document to delete
        db.collection("treino_exercicios_plano")
                .whereEqualTo("treino_id", treinoPlanoId)
                .whereEqualTo("id", exerciseId)
                .whereEqualTo("user_id", user_id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // List to store the documents to update
                        List<DocumentSnapshot> remainingDocuments = new ArrayList<>();

                        // First, delete the documents
                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            // Delete the document
                            db.collection("treino_exercicios_plano").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("DatabaseSuccess", "Exercício apagado com sucesso")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("DatabaseError", "Erro ao deletar exercício", e)
                                    );
                        });

                        // After deletion, query the remaining documents
                        db.collection("treino_exercicios_plano")
                                .whereEqualTo("treino_id", treinoPlanoId)
                                .whereEqualTo("user_id", user_id)
                                .get()
                                .addOnSuccessListener(updatedQuerySnapshot -> {
                                    if (!updatedQuerySnapshot.isEmpty()) {
                                        // Sort the remaining documents by some criteria to get the correct order (e.g., by "order_id" or any other field)
                                        List<DocumentSnapshot> sortedDocuments = updatedQuerySnapshot.getDocuments();
                                        // Here, you might want to sort by "order_id" or any other field as needed
                                        Collections.sort(sortedDocuments, (doc1, doc2) -> {
                                            // Adjust sorting logic based on your needs
                                            return Integer.compare(doc1.getLong("order_id").intValue(), doc2.getLong("order_id").intValue());
                                        });

                                        // Now update the "order_id" of the remaining documents
                                        for (int i = 0; i < sortedDocuments.size(); i++) {
                                            DocumentSnapshot doc = sortedDocuments.get(i);
                                            int finalI = i;
                                            db.collection("treino_exercicios_plano").document(doc.getId())
                                                    .update("order_id", i + 1) // Adjust the order_id as per your logic
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



    public void updateExerciseOrdersInPlan(int treinoId, List<Exercise> exerciseList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int counter = 1;
        for (Exercise exercise : exerciseList) {
            // Query to find the document for the specific exercise in the plan
            int finalCounter = counter;
            System.out.println(finalCounter);
            System.out.println(exerciseList.size() + "TAMANHOOOO " +  treinoId + " " + exercise.getId());

            db.collection("treino_exercicios_plano")
                    .whereEqualTo("treino_id", treinoId)
                    .whereEqualTo("id", exercise.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            queryDocumentSnapshots.getDocuments().forEach(document -> {
                                // Prepare the data to update
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("order_id", finalCounter); // Update order

                                // Update the document
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



    public void insertTreinoDone(int treinoId, String data, int exec, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Dados do treino feito
        Map<String, Object> treinoDoneData = new HashMap<>();
        treinoDoneData.put("treino_id", treinoId);
        treinoDoneData.put("data", data);
        treinoDoneData.put("exec", exec);
        treinoDoneData.put("user_id", userId);

        // Inserir o treino feito na coleção "treino_done"
        db.collection("treino_done")
                .add(treinoDoneData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("DatabaseSuccess", "Treino feito inserido com sucesso, ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseError", "Erro ao inserir treino feito na coleção treino_done", e);
                });
    }


    public void insertSeries(int peso, int numeroSerie, int treinoExercicioId, int treinoId, int exec,String user_id,int oxigenacao,int batimentos) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Dados da série
        Map<String, Object> seriesData = new HashMap<>();
        seriesData.put("peso", peso);
        seriesData.put("numero_serie", numeroSerie);
        seriesData.put("treino_exercicio_id", treinoExercicioId);
        seriesData.put("plano_id", treinoId);
        seriesData.put("exec", exec);
        seriesData.put("user_id",user_id);
        seriesData.put("oxigenacao",oxigenacao);
        seriesData.put("batimentos",batimentos);

        // Inserir na coleção "series"
        db.collection("series")
                .add(seriesData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("DatabaseSuccess", "Série inserida com sucesso, ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseError", "Erro ao inserir série na coleção series", e);
                });
    }


    public void syncTreinoPlanosFromFirebase(String id,DatabaseHelper dblocal) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<TreinoPlano> treinoPlanos = dblocal.getAllTreinoPlanos_sync(id);

        db.collection("treino_planos")
                .whereEqualTo("user_id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Set<Integer> firebaseIds = new HashSet<>();
                        queryDocumentSnapshots.getDocuments().forEach(document -> {

                            Long id_doc = document.getLong("id");
                            String nome = document.getString("nome");
                            String user_id = document.getString("user_id");

                            boolean existsInLocalList = false;
                            for (TreinoPlano plano : treinoPlanos) {
                                if (plano.getId() == Math.toIntExact(id_doc)) {
                                    existsInLocalList = true;
                                    treinoPlanos.remove(plano); // Remove o plano do ArrayList
                                    break; // Não há necessidade de continuar a busca, já encontrou
                                }
                            }

                            // Se o documento não existir no ArrayList, apaga da Firebase
                            if (!existsInLocalList) {
                                db.collection("treino_planos")
                                        .document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("Sync", "Plano removido da Firebase: " + id_doc)
                                        )
                                        .addOnFailureListener(e ->
                                                Log.e("Sync", "Erro ao remover plano da Firebase", e)
                                        );
                            }



                        });
                        // No final, qualquer item restante no ArrayList é um plano que não está na Firebase
                        for (TreinoPlano plano : treinoPlanos) {
                            // Adiciona os planos restantes da lista local para a Firebase
                            db.collection("treino_planos")
                                    .add(plano)
                                    .addOnSuccessListener(documentReference ->
                                            Log.d("Sync", "Plano adicionado à Firebase: " + plano.getId())
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("Sync", "Erro ao adicionar plano à Firebase", e)
                                    );
                        }
                    } else {
                        Log.d("DatabaseInfo", "Nenhum exercício encontrado para atualizar.");
                    }
                })
                .addOnFailureListener(e -> Log.e("DatabaseError", "Erro ao buscar exercício", e));

    }

    public void syncTreinoPlanosExercicioFromFirebase(String id, DatabaseHelper dblocal){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<TreinoExercicioPlano> treinoExercicioPlanos = dblocal.getAllTreinoExercicioPlanos_sync(id);

        db.collection("treino_exercicios_plano")
                .whereEqualTo("user_id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Set<Integer> firebaseIds = new HashSet<>();

                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            Long id_doc = document.getLong("id");
                            Integer exercicio_id = document.getLong("exercicio_id").intValue();
                            Integer treino_id = document.getLong("treino_id").intValue();
                            Integer series = document.getLong("series").intValue();
                            Integer repeticoes = document.getLong("repeticoes").intValue();
                            Integer order_id = document.getLong("order_id").intValue();
                            String user_id = document.getString("user_id");

                            // Verifica se o plano de treino existe na lista local
                            boolean existsInLocalList = false;
                            for (TreinoExercicioPlano plano : treinoExercicioPlanos) {
                                if (plano.getId() == Math.toIntExact(id_doc)) {
                                    existsInLocalList = true;
                                    treinoExercicioPlanos.remove(plano); // Remove o plano do ArrayList
                                    break; // Não há necessidade de continuar a busca, já encontrou
                                }
                            }

                            // Se o documento não existir no ArrayList, apaga da Firebase
                            if (!existsInLocalList) {
                                db.collection("treino_exercicios_plano")
                                        .document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("Sync", "Plano de exercício removido da Firebase: " + id_doc)
                                        )
                                        .addOnFailureListener(e ->
                                                Log.e("Sync", "Erro ao remover plano de exercício da Firebase", e)
                                        );
                            }

                            // Adiciona o id ao Set para manter controle
                            firebaseIds.add(Math.toIntExact(id_doc));
                        });

                        // No final, qualquer item restante no ArrayList é um plano que não está na Firebase
                        for (TreinoExercicioPlano plano : treinoExercicioPlanos) {
                            // Adiciona os planos restantes da lista local para a Firebase
                            db.collection("treino_exercicios_plano")
                                    .add(plano)
                                    .addOnSuccessListener(documentReference ->
                                            Log.d("Sync", "Plano de exercício adicionado à Firebase: " + plano.getId())
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("Sync", "Erro ao adicionar plano de exercício à Firebase", e)
                                    );
                        }
                    } else {
                        Log.d("DatabaseInfo", "Nenhum plano de exercício encontrado na Firebase.");
                    }
                })
                .addOnFailureListener(e -> Log.e("DatabaseError", "Erro ao buscar planos de exercício na Firebase", e));

    }

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

                        // Agora imprime os dados após o sucesso da operação
                        System.out.println("PLANOS: " + treinoPlanos);
                        callback.onComplete();
                    } else {
                        Log.d("DatabaseInfo", "Nenhum exercício encontrado para atualizar.");
                        callback.onComplete();
                    }
                })
                .addOnFailureListener(e -> {Log.e("DatabaseError", "Erro ao buscar exercício", e);callback.onComplete();});
    }

    public void getAllPlansExerciseFromFirebase(String id, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<TreinoExercicioPlano> treinoExercicioPlanos = new ArrayList<>();

        db.collection("treino_exercicios_plano")
                .whereEqualTo("user_id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Set<Integer> firebaseIds = new HashSet<>();

                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            Integer exercicio_id = document.getLong("exercicio_id").intValue();
                            Integer treino_id = document.getLong("treino_id").intValue();
                            Integer series = document.getLong("series").intValue();
                            Integer repeticoes = document.getLong("repeticoes").intValue();
                            Integer order_id = document.getLong("order_id").intValue();
                            String user_id = document.getString("user_id");
                          //  dblocal.insertExercicioFromPlano(treinoPlano.getId(),id,series,rep,order);
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


    public void getAllTreinoDoneFromFirebase(String id, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<TreinosDone> treinosDones = new ArrayList<>();

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


    public void syncLocalDataToFirebasePLANOS(String userId, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obter dados locais
        List<TreinoPlano> localPlans = dblocal.getAllTreinoPlanos_sync(userId);
        Set<String> localPlanNames = new HashSet<>();
        localPlans.forEach(plan -> localPlanNames.add(plan.getNome()));

        // Obter dados do Firebase
        db.collection("treino_planos")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Criar conjuntos para os nomes do Firebase e IDs de documentos
                 //   Set<Integer> firebasePlanNames = new HashSet<>();
                    Map<String, Integer> firebasePlanIDS = new HashMap<>();
                    Map<Integer,String> firebasePlanDocumentIDS = new HashMap<>();

                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        Integer id_plano = Math.toIntExact(document.getLong("id"));
                        String nome = document.getString("nome");
                        System.out.println(nome + " FIREDOCU " + id_plano);

                        firebasePlanIDS.put(nome,id_plano);
                        firebasePlanDocumentIDS.put(id_plano, document.getId());
                    });

                    // Atualizar ou adicionar os dados locais ao Firebase
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
                            // Adiciona ao Firebase se não existir
                            db.collection("treino_planos")
                                    .add(localPlan.toMap())
                                    .addOnSuccessListener(documentReference -> Log.d("FirebaseSync", "Plano adicionado: " + localPlan.getNome()))
                                    .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao adicionar plano: " + localPlan.getNome(), e));
                        }
                    }

                    // Identifica os planos no Firebase que não estão localmente
                    for (Map.Entry<Integer, String> entry : firebasePlanDocumentIDS.entrySet()) {
                        Integer idPlano = entry.getKey();
                        String firebaseId = entry.getValue();
                        boolean existsLocally = localPlans.stream()
                                .anyMatch(plan -> plan.getId() == idPlano);

                        if (!existsLocally) {
                            // Remove do Firebase
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



    public void syncLocalDataToFirebaseExercicios(String userId, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obter dados locais
        List<TreinoExercicioPlano> localExercicios = dblocal.getAllTreinoExercicioPlanos_sync(userId);
        Set<Integer> localExercicioNames = new HashSet<>();
        localExercicios.forEach(exercicio -> localExercicioNames.add(exercicio.getExercicio_id()));

        // Obter dados do Firebase
        db.collection("treino_exercicios_plano")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Criar conjuntos para os nomes do Firebase e IDs de documentos
                    Map<Integer, Integer> firebaseExercicioIDS = new HashMap<>();
                    Map<Integer, String> firebaseExercicioDocumentIDS = new HashMap<>();

                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        Integer id_exercicio = Math.toIntExact(document.getLong("exercicio_id"));
                        Integer id_plan_exercicio = Math.toIntExact(document.getLong("id"));

                        // Mapear o nome para ID do treino e o ID do exercício para o documento
                        firebaseExercicioIDS.put(id_plan_exercicio, id_exercicio);
                        firebaseExercicioDocumentIDS.put(id_plan_exercicio, document.getId());
                    });

                    // Atualizar ou adicionar os dados locais ao Firebase
                    for (TreinoExercicioPlano localExercicio : localExercicios) {
                        System.out.println(localExercicio + "  " + localExercicio.getId());
                        System.out.println(firebaseExercicioIDS);

                        // Verifica se o nome e o ID do exercício já existem no Firebase
                        if (firebaseExercicioIDS.containsKey(localExercicio.getId()) &&
                                firebaseExercicioIDS.containsValue(localExercicio.getExercicio_id())) {

                            // Atualiza se já existe no Firebase
                            db.collection("treino_exercicios_plano")
                                    .document(firebaseExercicioDocumentIDS.get(localExercicio.getId()))
                                    .set(localExercicio.toMap())
                                    .addOnSuccessListener(aVoid -> Log.d("FirebaseSync", "Exercício atualizado: " + localExercicio.getId()))
                                    .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao atualizar exercício: " + localExercicio.getId(), e));

                        } else {
                            // Adiciona ao Firebase se não existir
                            db.collection("treino_exercicios_plano")
                                    .add(localExercicio.toMap())
                                    .addOnSuccessListener(documentReference -> Log.d("FirebaseSync", "Exercício adicionado: " + localExercicio.getId()))
                                    .addOnFailureListener(e -> Log.e("FirebaseError", "Erro ao adicionar exercício: " + localExercicio.getId(), e));
                        }
                    }

                    // Identifica os exercícios no Firebase que não estão localmente
                    for (Map.Entry<Integer, String> entry : firebaseExercicioDocumentIDS.entrySet()) {
                        Integer idExercicio = entry.getKey();
                        String firebaseId = entry.getValue();
                        boolean existsLocally = localExercicios.stream()
                                .anyMatch(exercicio -> exercicio.getId() == idExercicio);

                        if (!existsLocally) {
                            // Remove do Firebase se não existir localmente
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

    public void syncLocalDataToFirebaseTreinoDone(String userId, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obter dados locais da tabela 'treino_done'
        List<TreinosDone> localTreinosDone = dblocal.getAllTreinosDoneForUser(userId);

        // Criar conjuntos para os IDs dos treinos realizados locais
        Set<String> localTreinosDoneKeys = new HashSet<>();
        localTreinosDone.forEach(treinoDone ->
                localTreinosDoneKeys.add(treinoDone.getExec() + "_" + treinoDone.getData() + "_" + treinoDone.getTreino_id())
        );

        // Obter dados do Firebase
        db.collection("treino_done")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Criar mapas para os dados do Firebase
                    Map<String, String> firebaseTreinosDoneKeys = new HashMap<>();
                    Map<String, String> firebaseTreinoDocumentIds = new HashMap<>();

                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        String exercicioId = document.getString("exercicio_id");
                        String data = document.getString("data");
                        Integer treinoId = Math.toIntExact(document.getLong("treino_id"));

                        // Gerar a chave única para identificar o treino realizado
                        String key = data + "_" + treinoId;

                        // Mapear a chave única para o ID do documento do Firebase
                        firebaseTreinosDoneKeys.put(key, key);
                        firebaseTreinoDocumentIds.put(key, document.getId());
                    });

                    // Adicionar os dados locais ao Firebase, se ainda não existirem
                    for (TreinosDone localTreinoDone : localTreinosDone) {
                        String localKey = localTreinoDone.getData() + "_" + localTreinoDone.getTreino_id();

                        // Verifica se a chave existe no Firebase
                        if (!firebaseTreinosDoneKeys.containsKey(localKey)) {
                            // Adiciona ao Firebase se não existir
                            db.collection("treino_done")
                                    .add(localTreinoDone.toMap(userId))
                                    .addOnSuccessListener(documentReference ->
                                            Log.d("FirebaseSync", "Treino realizado adicionado: " + localTreinoDone.getId()))
                                    .addOnFailureListener(e ->
                                            Log.e("FirebaseError", "Erro ao adicionar treino realizado: " + localTreinoDone.getId(), e));
                        }
                    }

                    // Identifica os treinos realizados no Firebase que não estão localmente
                    for (Map.Entry<String, String> entry : firebaseTreinoDocumentIds.entrySet()) {
                        String firebaseKey = entry.getKey();
                        String firebaseId = entry.getValue();

                        // Verifica se o treino realizado existe localmente
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


    public void syncLocalDataToFirebaseSerie(String userId, DatabaseHelper dblocal, menu_principal.FirebaseSyncCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obter dados locais da tabela 'serie'
        List<SeriesInfo> localSeries = dblocal.getSeriesByUserId(userId);

        // Criar conjuntos para os dados locais
        Set<String> localSerieKeys = new HashSet<>();
        localSeries.forEach(serie ->
                localSerieKeys.add(serie.getTreinoId() + "_" + serie.getSeries() + "_" + serie.getExercicioId() +"_" + serie.getExec())
        );

        // Obter dados do Firebase
        db.collection("series")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Criar mapas para os dados do Firebase
                    Map<String, String> firebaseSerieKeys = new HashMap<>();
                    Map<String, String> firebaseSerieDocumentIds = new HashMap<>();

                    queryDocumentSnapshots.getDocuments().forEach(document -> {

                        Integer treinoExercicioId = Math.toIntExact(document.getLong("treino_exercicio_id"));
                        Integer numeroSerie = Math.toIntExact(document.getLong("numero_serie"));
                        Integer planId = Math.toIntExact(document.getLong("plano_id"));
                        Integer exec = Math.toIntExact(document.getLong("exec"));

                        // Gerar a chave única para identificar a série
                        String key = planId + "_" + numeroSerie + "_" + treinoExercicioId + "_" + exec;

                        // Mapear a chave única para o ID do documento do Firebase
                        firebaseSerieKeys.put(key, key);
                        firebaseSerieDocumentIds.put(key, document.getId());
                    });

                    // Adicionar os dados locais ao Firebase, se ainda não existirem
                    for (SeriesInfo localSerie : localSeries) {
                        String localKey = localSerie.getTreinoId() + "_" + localSerie.getSeries() + "_" + localSerie.getExercicioId() + "_" + localSerie.getExec();

                        // Verifica se a chave existe no Firebase
                        if (!firebaseSerieKeys.containsKey(localKey)) {
                            // Adiciona ao Firebase se não existir
                            db.collection("series")
                                    .add(localSerie.toMap(userId))
                                    .addOnSuccessListener(documentReference ->
                                            Log.d("FirebaseSync", "Série adicionada: " + localSerie.getTreinoId() + " " + localSerie.getExercicioId() ))
                                    .addOnFailureListener(e ->
                                            Log.e("FirebaseError", "Erro ao adicionar série: " + localSerie.getTreinoId()+ " " + localSerie.getExercicioId(), e));
                        }

                    }

                    // Identifica as séries no Firebase que não estão localmente
                    for (Map.Entry<String, String> entry : firebaseSerieDocumentIds.entrySet()) {
                        String firebaseKey = entry.getKey();
                        String firebaseId = entry.getValue();

                        // Verifica se a série existe localmente
                        boolean existsLocally = localSeries.stream()
                                .anyMatch(serie ->
                                        (serie.getTreinoId() + "_" + serie.getSeries() + "_" + serie.getExercicioId() + "_" + serie.getExec()).equals(firebaseKey)
                                );

                        if (!existsLocally) {
                            // Remove do Firebase se não existir localmente
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




}
