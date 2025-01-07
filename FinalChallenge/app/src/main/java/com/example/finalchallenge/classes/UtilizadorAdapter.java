package com.example.finalchallenge.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.finalchallenge.R;

import java.util.List;

public class UtilizadorAdapter extends RecyclerView.Adapter<UtilizadorAdapter.ViewHolder> {

    private List<Utilizador> utilizadores;

    // Constructor
    public UtilizadorAdapter(List<Utilizador> utilizadores) {
        this.utilizadores = utilizadores;
    }

    // ViewHolder to hold reference to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }

    // onCreateViewHolder: Inflate the item layout
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(itemView);
    }

    // onBindViewHolder: Bind data to views
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Utilizador utilizador = utilizadores.get(position);
        holder.tvName.setText(utilizador.getUsername());
    }

    // getItemCount: Return the size of the list
    @Override
    public int getItemCount() {
        return utilizadores.size();
    }
}
