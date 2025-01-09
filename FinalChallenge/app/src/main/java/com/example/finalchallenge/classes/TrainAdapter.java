package com.example.finalchallenge.classes;

import android.content.Context;
import android.text.TextUtils;
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

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.ViewHolder> {

    private List<TreinosDone> treinos;

    // Constructor
    public TrainAdapter( List<TreinosDone> treinos) {
        this.treinos = treinos;

    }

    // ViewHolder to hold reference to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView trainName, trainExec, trainDate;
        public Button btnDetails, btnCopy;

        public ViewHolder(View itemView) {
            super(itemView);
            trainName = itemView.findViewById(R.id.tvName);
            trainExec = itemView.findViewById(R.id.tvExecutions);
            trainDate = itemView.findViewById(R.id.tvDate);
            btnDetails = itemView.findViewById(R.id.btnView);
            btnCopy = itemView.findViewById(R.id.btnCopy);
        }
    }

    // onCreateViewHolder: Inflate the item layout
    @Override
    public TrainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_train, parent, false);
        return new ViewHolder(itemView); // Corrected this line
    }

    // onBindViewHolder: Bind data to the views
    @Override
    public void onBindViewHolder(TrainAdapter.ViewHolder holder, int position) { // Corrected this line
        TreinosDone treino = treinos.get(position);

        holder.trainName.setText("Nome:" + treino.getTrainName());
        holder.trainExec.setText("Execuções:" + String.valueOf(treino.getExec()));
        holder.trainDate.setText("Data:" + treino.getData());


    }

    @Override
    public int getItemCount() {
        return treinos.size();
    }
}
