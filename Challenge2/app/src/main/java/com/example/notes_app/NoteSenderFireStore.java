package com.example.notes_app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteSenderFireStore {

    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Executor para operações de rede
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler para a thread principal

    // Método para verificar se o dispositivo está conectado à internet
    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    // Método para enviar a nota apenas se houver conexão com a internet
    public void sendNoteToFireStoreIfConnected(Context context, String id, String title, String description) {
        if (isConnectedToInternet(context)) {
            SendNoteToFireStore(context,id,title, description);
        } else {
            System.out.println("Sem conexão com a internet. A nota não foi enviada.");
        }
    }

    public void updateNoteToFireStoreIfConnected(Context context,String title, String newTitle,String newDescription) {
        if (isConnectedToInternet(context)) {
            updateNoteInFirestore(context,title,newTitle,newDescription);
        } else {
            System.out.println("Sem conexão com a internet. A nota não foi enviada.");
        }
    }

    // Método original para enviar a nota ao Firestore
    public void SendNoteToFireStore(Context context,String id, String title, String description) {
        if (isConnectedToInternet(context)) {
            executorService.submit(() -> {
                // Verifica se o ID já existe na coleção
                db.collection("notes")
                        .whereEqualTo("id", id) // Filtra as notas pelo ID
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    // O ID não existe, podemos adicionar a nota
                                    Map<String, Object> nota = new HashMap<>();
                                    nota.put("id", id);
                                    nota.put("title", title);
                                    nota.put("description", description);
                                    db.collection("notes")
                                            .add(nota)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    System.out.println("Nota salva com sucesso! ID: " + documentReference.getId());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    System.err.println("Erro ao salvar a nota: " + e.getMessage());
                                                }
                                            });
                                } else {

                                  
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String documentId = document.getId();


                                        Map<String, Object> updatedNota = new HashMap<>();
                                        updatedNota.put("id", id);
                                        updatedNota.put("title", title);
                                        updatedNota.put("description", description);

                                        db.collection("notes").document(documentId)
                                                .set(updatedNota, SetOptions.merge())
                                                .addOnSuccessListener(aVoid -> {
                                                    System.out.println("Nota atualizada com sucesso! ID: " + documentId);
                                                })
                                                .addOnFailureListener(e -> {
                                                    System.err.println("Erro ao atualizar a nota: " + e.getMessage());
                                                });
                                    }
                                }
                            } else {
                                // Erro ao verificar a existência do ID
                                System.err.println("Erro ao verificar ID no Firestore: " + task.getException().getMessage());
                            }
                        });
            });
        }
    }




    public void updateNoteInFirestore(Context context,String id,String newTitle, String newDescription) {
        executorService.submit(() -> {
            // Buscar documento com o título especificado
            db.collection("notes")
                    .whereEqualTo("id", id)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();


                            Map<String, Object> updates = new HashMap<>();
                            updates.put("title",newTitle);
                            updates.put("description", newDescription);


                            db.collection("notes").document(documentId).update(updates)
                                    .addOnSuccessListener(aVoid -> Log.d("UPDATE", "Nota atualizada com sucesso!"))
                                    .addOnFailureListener(e -> Log.e("UPDATE", "Erro ao atualizar a nota: " + e.getMessage()));
                        } else {
                            Log.v("UPDATE", "Nota com ID '" + id + "' não encontrada.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("UPDATE", "Erro ao buscar nota: " + e.getMessage()));
        });

    }

    public void deleteNotesNotInFile(Context context,List<String> ids) {
        if (isConnectedToInternet(context)) {
            executorService.submit(() -> {
                db.collection("notes").get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String firestoreNoteId = document.getString("id");

                                    System.out.println(firestoreNoteId);
                                    System.out.println(ids);
                                    if (!ids.contains(firestoreNoteId)) {
                                        System.out.println("ENTROU");
                                        // Deletar a nota do Firestore
                                        assert firestoreNoteId != null;
                                        db.collection("notes").document(firestoreNoteId).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("DELETE_NOTE", "Nota deletada com sucesso: " + firestoreNoteId);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("DELETE_NOTE", "Erro ao deletar nota: " + e.getMessage());
                                                });
                                    }
                                }
                            } else {
                                Log.e("DELETE_NOTE", "Erro ao obter notas do Firestore: " + task.getException().getMessage());
                            }
                        });
            });
        }
    }





    public void deleteNote(Context context, String id) {
        if (isConnectedToInternet(context)) {
            executorService.submit(() -> {
                db.collection("notes")
                        .whereEqualTo("id", id)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {

                                    QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);


                                    document.getReference().delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    System.out.println("Nota com ID \"" + id + "\" apagada com sucesso!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    System.err.println("Erro ao apagar a nota: " + e.getMessage());
                                                }
                                            });
                                } else {
                                    System.out.println("Nenhuma nota encontrada com o ID: " + id);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                System.err.println("Erro ao buscar a nota: " + e.getMessage());
                            }
                        });
            });
        }
    }



    public void syncNotesToFireStore(Context context,String id,String title,String description) {
        if (isConnectedToInternet(context)) {
            executorService.submit(() -> {
                Query query = db.collection("notes").whereEqualTo("id", id);
                query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {

                        sendNoteToFireStoreIfConnected(context, id, title, description);
                    } else {
                        updateNoteInFirestore(context, id, title, description);
                        Log.v("SYNC", "Nota com ID '" + id + "' foi atualizada");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("SYNC", "Erro ao verificar nota existente: " + e.getMessage());

                });
            });
        }

    }





}
