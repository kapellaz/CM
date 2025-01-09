package com.example.finalchallenge.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.finalchallenge.MainActivity;
import com.example.finalchallenge.R;

import java.util.ArrayList;
import java.util.List;

public class UtilizadorAdapter extends RecyclerView.Adapter<UtilizadorAdapter.ViewHolder> {

    private List<Utilizador> utilizadores;
    private List<Utilizador> filteredList;
    private Context context;

    // Constructor
    public UtilizadorAdapter(Context context, List<Utilizador> utilizadores) {
        this.context = context; // Initialize context
        this.utilizadores = utilizadores;
        this.filteredList = new ArrayList<>(utilizadores); // Initialize filteredList with the full list
    }

    // ViewHolder to hold reference to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public Button btnVisit;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            btnVisit = itemView.findViewById(R.id.btnAction);
        }
    }

    // onCreateViewHolder: Inflate the item layout
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Utilizador utilizador = filteredList.get(position); // Use filtered list here
        holder.tvName.setText(utilizador.getUsername());

        holder.btnVisit.setOnClickListener(v -> {
            handleVisit(utilizador.getId());
        });

    }

    private void handleVisit(String userID) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).switchtoFriendProfile(userID);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String newText) {
        if(utilizadores.isEmpty()){
            return;
        }
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
