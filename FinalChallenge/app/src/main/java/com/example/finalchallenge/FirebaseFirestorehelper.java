package com.example.finalchallenge;
import android.util.Log;

import com.example.finalchallenge.classes.Exercise;
import com.example.finalchallenge.classes.TreinoExercicioPlano;
import com.example.finalchallenge.classes.TreinoPlano;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
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
        ArrayList<TreinoPlano> treinoPlanos = dblocal.getAllTreinoPlanos_sync();

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
        ArrayList<TreinoExercicioPlano> treinoExercicioPlanos = dblocal.getAllTreinoExercicioPlanos_sync();

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





}
