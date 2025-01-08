package com.example.finalchallenge.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.finalchallenge.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OthersAdapter extends RecyclerView.Adapter<OthersAdapter.ViewHolder>{

    private List<Utilizador> utilizadores;
    private List<Utilizador> filteredList;

    public OthersAdapter(List<Utilizador> users) {
        this.utilizadores = users;
        this.filteredList = new ArrayList<>(utilizadores); // Initialize filteredList with the full list
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
    @Override
    public OthersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_other, parent, false);
        return new OthersAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OthersAdapter.ViewHolder holder, int position) {
        Utilizador utilizador = filteredList.get(position); // Use filtered list here
        holder.tvName.setText(utilizador.getUsername());

        //button de ADD!
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String newText) {
        filteredList.clear();

        if (newText.isEmpty()) {
            filteredList.addAll(utilizadores);
        } else {
            for (Utilizador utilizador : utilizadores) {
                if (utilizador.getUsername().toLowerCase().startsWith(newText.toLowerCase())) {
                    filteredList.add(utilizador);
                }
            }
        }
        notifyDataSetChanged();
    }

}
