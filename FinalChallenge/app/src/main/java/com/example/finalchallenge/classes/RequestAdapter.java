package com.example.finalchallenge.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalchallenge.R;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private List<Request> requests;
    private List<Request> filteredRequests;
    private String userID;

    // Constructor
    public RequestAdapter(List<Request> requests, String userID) {
        this.requests = requests;
        this.filteredRequests = new ArrayList<>(requests);
        this.userID = userID;
    }

    public  RequestAdapter(){
        this.requests = new ArrayList<>();
        this.filteredRequests = new ArrayList<>(requests);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public Button btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            btnAccept = itemView.findViewById(R.id.btnAction1);
            btnReject = itemView.findViewById(R.id.btnAction2);
        }
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Request request = filteredRequests.get(position);

        holder.tvName.setText(request.getSenderName());

        holder.btnAccept.setOnClickListener(v -> {
            acceptRequest(request.getSenderID());
            filteredRequests.remove(position);
            removeRequestFromList(request);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, filteredRequests.size());
        });

        holder.btnReject.setOnClickListener(v -> {
            rejectRequest(request.getSenderID());
            filteredRequests.remove(position);
            removeRequestFromList(request);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, filteredRequests.size());
        });
    }

    public void acceptRequest(String senderID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("pedido_amizade")
                .whereEqualTo("recebeu", this.userID)
                .whereEqualTo("enviou", senderID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        // Delete the friendship request from 'pedido_amizade'
                        db.collection("pedido_amizade").document(document.getId())
                                .delete();
                        // Add a new document to the 'amigos' collection
                        db.collection("amigos").add(
                                        new HashMap<String, Object>() {{
                                            put("user1", senderID);
                                            put("user2", userID);
                                        }}
                                )
                                .addOnSuccessListener(docRef -> {
                                    System.out.println("Friendship added successfully with ID: " + docRef.getId());
                                })
                                .addOnFailureListener(e -> {
                                    System.err.println("Error adding friendship: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error retrieving friendship request: " + e.getMessage());
                });
    }

    public void rejectRequest(String senderID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pedido_amizade")
                .whereEqualTo("recebeu", this.userID)
                .whereEqualTo("enviou", senderID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        db.collection("pedido_amizade").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("Deleted the thing");
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return filteredRequests.size();
    }

    // Finds the request in the original list (requests) and remove it
    private void removeRequestFromList(Request request) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getSenderID().equals(request.getSenderID())) {
                requests.remove(i);
                break;
            }
        }
    }

    public void filter(String newText) {
        System.out.println("Vou morrer XD");
        if(requests.isEmpty()){
            return;
        }
        filteredRequests.clear();

        if (newText.isEmpty()) {
            filteredRequests.addAll(requests);
        } else {
            for (Request request : requests) {
                if (request.getSenderName().toLowerCase().startsWith(newText.toLowerCase())) {
                    filteredRequests.add(request);
                }
            }
        }
        notifyDataSetChanged();
    }
}
