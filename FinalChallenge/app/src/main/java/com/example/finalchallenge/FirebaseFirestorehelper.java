package com.example.finalchallenge;
import android.util.Log;

import com.example.finalchallenge.classes.Exercise;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseFirestorehelper {

    public void createPlan(Integer planID,String planName, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the data to be added
        Map<String, Object> planData = new HashMap<>();
        planData.put("id",planID);
        planData.put("nome", planName);
        planData.put("user_id", userId);

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

    public void insertExercicioFromPlano(Integer treinoPlanoId, int exerciseId, int series, int repeticoes, int order,String user_id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the data to be added
        Map<String, Object> exerciseData = new HashMap<>();
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

        // Step 1: Delete the plan from the treino_planos collection
        db.collection("treino_planos")
                .whereEqualTo("id", planId)
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            db.collection("treino_planos").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("DatabaseSuccess", "Plano deletado com sucesso")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("DatabaseError", "Erro ao deletar plano", e)
                                    );
                        });
                    } else {
                        Log.d("DatabaseInfo", "Nenhum plano encontrado com o ID especificado.");
                    }
                })
                .addOnFailureListener(e -> Log.e("DatabaseError", "Erro ao buscar plano", e));

        // Step 2: Delete associated exercises from the treino_exercicios_plano collection
        db.collection("treino_exercicios_plano")
                .whereEqualTo("treino_id", planId)
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            db.collection("treino_exercicios_plano").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("DatabaseSuccess", "Exercício deletado com sucesso")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("DatabaseError", "Erro ao deletar exercício", e)
                                    );
                        });
                    } else {
                        Log.d("DatabaseInfo", "Nenhum exercício encontrado para o plano especificado.");
                    }
                })
                .addOnFailureListener(e -> Log.e("DatabaseError", "Erro ao buscar exercícios", e));
    }


    public void deleteExercicioFromPlano(int treinoPlanoId, int exerciseId,String user_id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the treino_exercicios_plano collection to find the document to delete
        db.collection("treino_exercicios_plano")
                .whereEqualTo("treino_id", treinoPlanoId)
                .whereEqualTo("exercicio_id", exerciseId)
                .whereEqualTo("user_id", user_id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().forEach(document -> {
                            // Delete the document
                            db.collection("treino_exercicios_plano").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("DatabaseSuccess", "Exercício deletado com sucesso")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e("DatabaseError", "Erro ao deletar exercício", e)
                                    );
                        });
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
                    .whereEqualTo("exercicio_id", exercise.getId())
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
            // Consulta para encontrar o documento do exercício específico no plano
            db.collection("treino_exercicios_plano")
                    .whereEqualTo("treino_id", treinoId)
                    .whereEqualTo("exercicio_id", exercise.getId())
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


    public void insertSeries(int peso, int numeroSerie, int treinoExercicioId, int treinoId, int exec,String user_id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Dados da série
        Map<String, Object> seriesData = new HashMap<>();
        seriesData.put("peso", peso);
        seriesData.put("numero_serie", numeroSerie);
        seriesData.put("treino_exercicio_id", treinoExercicioId);
        seriesData.put("plano_id", treinoId);
        seriesData.put("exec", exec);
        seriesData.put("user_id",user_id);

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




}
