package com.example.finalchallenge.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.finalchallenge.DatabaseHelper;
import com.example.finalchallenge.MainActivity;
import com.example.finalchallenge.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.ViewHolder> {

    private List<TreinosDone> treinos;
    private Context context;
    private DatabaseHelper databaseHelper;
    private String FriendID;
    private String userID;
    // Constructor
    public TrainAdapter(Context context, List<TreinosDone> treinos, String FriendID, String userID) {
        this.treinos = treinos;
        this.context = context; // Initialize context
        this.FriendID = FriendID;
        databaseHelper = new DatabaseHelper(context);
        this.userID = userID;
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

        //programar os dois buttons
        holder.btnDetails.setOnClickListener(v -> {
            handleDetailsTrain(treino);
        });
        holder.btnCopy.setOnClickListener(v -> {
            handleCopyTrain(treino);
        });

    }

    private void handleCopyTrain(TreinosDone treino) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("treino_exercicios_plano")
                .whereEqualTo("user_id", FriendID)
                .whereEqualTo("treino_id", treino.getTreino_id())
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        if (!task1.getResult().isEmpty()) {
                            long treinoID = databaseHelper.createPlan("Copy " + treino.getTrainName(),userID,1 );
                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                TreinoExercicioPlano exercicio = new TreinoExercicioPlano(
                                        document.getLong("id").intValue(),
                                        document.getLong("exercicio_id").intValue(),
                                        document.getLong("treino_id").intValue(),
                                        document.getLong("series").intValue(),
                                        document.getLong("repeticoes").intValue(),
                                        document.getLong("order_id").intValue(),
                                        FriendID
                                );
                                databaseHelper.insertExercicioFromPlano((int)treinoID,exercicio.getExercicio_id(),
                                        exercicio.getSeries(),exercicio.getRepeticoes(),exercicio.getOrder_id());
                            }

                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            mainHandler.post(() ->
                                    Toast.makeText(context, "Treino Copiado!", Toast.LENGTH_SHORT).show()
                            );
                        }


                    }
                });
    }

    private void handleDetailsTrain(TreinosDone treino) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<TreinoExercicioPlano> exercicios = new ArrayList<>();

        db.collection("treino_exercicios_plano")
                .whereEqualTo("user_id", FriendID)
                .whereEqualTo("treino_id", treino.getTreino_id())
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        if (!task1.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                TreinoExercicioPlano exercicio = new TreinoExercicioPlano(
                                        document.getLong("id").intValue(),
                                        document.getLong("exercicio_id").intValue(),
                                        document.getLong("treino_id").intValue(),
                                        document.getLong("series").intValue(),
                                        document.getLong("repeticoes").intValue(),
                                        document.getLong("order_id").intValue(),
                                        FriendID
                                );
                                exercicios.add(exercicio);
                            }
                        }

                        StringBuilder detailsText = new StringBuilder();
                        for (TreinoExercicioPlano exercise : exercicios) {
                            String nome = databaseHelper.getExerciseNameById(exercise.getExercicio_id());
                            detailsText.append(nome + "\n");
                            detailsText.append(exercise.toString()).append("\n").append("\n");
                        }

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                        dialogBuilder.setTitle("Training Details");
                        dialogBuilder.setMessage(detailsText.toString());
                        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialogBuilder.create().show();
                    } else {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                        dialogBuilder.setTitle("Error");
                        dialogBuilder.setMessage("Failed to load training details.");
                        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialogBuilder.create().show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return treinos.size();
    }
}
