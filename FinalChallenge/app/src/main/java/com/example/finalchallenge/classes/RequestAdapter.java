package com.example.finalchallenge.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalchallenge.R;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private List<Request> requests;

    // Constructor
    public RequestAdapter( List<Request> requests) {
        this.requests = requests;
    }

    // ViewHolder to hold references to views in the layout
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


        Request request = requests.get(position);

        // Bind the data to the views
        holder.tvName.setText(request.getSenderName());

        // Set button click listeners
        holder.btnAccept.setOnClickListener(v -> {
            // Handle Accept action
            // For example: remove the item from the list and notify the adapter
            requests.remove(position);
            notifyItemRemoved(position);
        });

        holder.btnReject.setOnClickListener(v -> {
            // Handle Reject action
            // For example: remove the item from the list and notify the adapter
            requests.remove(position);
            notifyItemRemoved(position);
        });

    }

    @Override
    public int getItemCount() {
        return requests.size();
    }
}
