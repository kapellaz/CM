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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class NoteSenderFireStore {

    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Check if exists any connection with Internet
     */
    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    /**
     * Send new note to Fire Store (checking if exists connection)
     */
    public void sendNoteToFireStoreIfConnected(Context context, String id, String title, String description) {
        if (isConnectedToInternet(context)) {
            SendNoteToFireStore(context,id,title, description);
        } else {
            Log.v("FireStoreError","No Connection! ");
        }
    }


    /**
     * Update the title or Description of a note (checking if exists connection)
     */

    public void updateNoteToFireStoreIfConnected(Context context,String id, String title,String description) {
        if (isConnectedToInternet(context)) {
            updateNoteInFirestore(context,id,title,description);
        } else {
            Log.v("FireStoreError","No Connection! ");
        }
    }

    /**
     * Create a new note or update a note  (checking if exists connection)
     */

    public void SendNoteToFireStore(Context context,String id, String title, String description) {
        if (isConnectedToInternet(context)) {
                db.collection("notes")
                        .whereEqualTo("id", id)  // check ID (if exists)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    // id is new, so we can add a new note
                                    Map<String, Object> nota = new HashMap<>();
                                    nota.put("id", id);
                                    nota.put("title", title);
                                    nota.put("description", description);
                                    db.collection("notes")
                                            .add(nota)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d("CREATE_NOTE", "Note saving note with ID: " + documentReference.getId());

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    Log.e("CREATE_NOTE", "Error Saving Note: " + e.getMessage());

                                                }
                                            });
                                } else { // if note exists its because we have to update the info

                                  
                                    for (QueryDocumentSnapshot document : task.getResult()) { // update note
                                        String documentId = document.getId();

                                        Map<String, Object> updatedNota = new HashMap<>();
                                        updatedNota.put("id", id);
                                        updatedNota.put("title", title);
                                        updatedNota.put("description", description);

                                        db.collection("notes").document(documentId)
                                                .set(updatedNota, SetOptions.merge())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("UPDATE_NOTE", "Note Updated with ID: " + documentId);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("UPDATE_NOTE", "Error: " + e.getMessage());
                                                });
                                    }
                                }
                            } else {
                                Log.d("UPDATE_NOTE", "Note with ID not found");
                            }

            });
        }else{
            Log.v("FireStoreError","No Connection! ");
        }
    }




    public void updateNoteInFirestore(Context context,String id,String newTitle, String newDescription) {
        if (isConnectedToInternet(context)){
            db.collection("notes")
                    .whereEqualTo("id", id)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("title", newTitle);
                            updates.put("description", newDescription);
                            db.collection("notes").document(documentId).update(updates)
                                    .addOnSuccessListener(aVoid -> Log.d("UPDATE", "Note Updated!"))
                                    .addOnFailureListener(e -> Log.e("UPDATE", "Error updating a note: " + e.getMessage()));
                        } else {
                            Log.v("UPDATE", "Note with ID '" + id + "' not found.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("UPDATE", "Error: " + e.getMessage()));

    }else{
            Log.v("FireStoreError","No Connection! ");
        }
    }

    public void deleteNotesNotInFile(Context context, ArrayList<String> ids) {
        if (isConnectedToInternet(context)) {
                db.collection("notes").get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String firestoreNoteId = document.getString("id");

                                    // check if does not exists notes at Internal storage, (remove all documents)
                                    if (ids.isEmpty()) {
                                        assert firestoreNoteId != null;

                                        db.collection("notes").document(document.getId()).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("DELETE_NOTE", "Note Deleted: " + firestoreNoteId);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("DELETE_NOTE", "Error: " + e.getMessage());
                                                });

                                    } else {
                                        if (!ids.contains(firestoreNoteId)) {


                                            assert firestoreNoteId != null;
                                            db.collection("notes").document(document.getId()).delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("DELETE_NOTE", "Note Deleted (2) : " + firestoreNoteId);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("DELETE_NOTE", "Error : " + e.getMessage());
                                                    });
                                        }
                                    }
                                }
                                } else{
                                    Log.e("DELETE_NOTE", "Error: " + task.getException().getMessage());
                                }

                        });

        }else{
            Log.v("FireStoreError","No Connection! ");
        }
    }

    /***
     * Method to delete a Note using the id
     */

    public void deleteNote(Context context, String id) {
        if (isConnectedToInternet(context)) {
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
                                                    Log.d("DELETE_NOTE", "Note Deleted: " + id);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    Log.e("DELETE_NOTE", "Error "+ e.getMessage());
                                                }
                                            });
                                } else {
                                    Log.d("DELETE_NOTE", "There is no note with ID: " + id);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Log.e("DELETE_NOTE",  "Error "+ e.getMessage());
                            }
                        });

        }else{
            Log.v("FireStoreError","No Connection! ");
        }
    }


    /**
     * Method to syncronize all information between internal storage and Firestore
     */


    public void syncNotesToFireStore(Context context,String id,String title,String description) {
        if (isConnectedToInternet(context)) {
                Query query = db.collection("notes").whereEqualTo("id", id);
                query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) { // New Note
                        sendNoteToFireStoreIfConnected(context, id, title, description);
                    } else {
                        updateNoteInFirestore(context, id, title, description);
                        Log.v("SYNC", "Note with ID '" + id + "' was updated");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("SYNC", "Error: " + e.getMessage());

                });

        }else{
            Log.v("FireStoreError","No Connection! ");
        }

    }





}
