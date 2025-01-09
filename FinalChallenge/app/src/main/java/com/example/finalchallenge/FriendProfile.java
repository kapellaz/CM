package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.example.finalchallenge.classes.TrainAdapter;
import com.example.finalchallenge.classes.TreinosDone;
import com.example.finalchallenge.classes.Utilizador;
import com.example.finalchallenge.classes.UtilizadorAdapter;
import com.example.finalchallenge.classes.viewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendProfile extends Fragment {

    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;
    private androidx.appcompat.widget.AppCompatImageButton friendsButton;
    private TextView Username;
    private TextView treinos_completos;
    private DatabaseHelper databaseHelper;
    private List<TreinosDone> treinosExec = new ArrayList<>();
    private ProgressBar progressBar; // ProgressBar
    private viewModel modelview;
    private FirebaseFirestorehelper firebaseFirestorehelper;
    private Boolean InternetOn;
    private Utilizador friend;
    private RecyclerView listTrains;
    private TrainAdapter trainAdapter;

    public FriendProfile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
        firebaseFirestorehelper = new FirebaseFirestorehelper();
        if (getArguments() != null) {
            friend = getArguments().getParcelable("friend");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);
        progressBar = view.findViewById(R.id.progressBar);

        Username = view.findViewById(R.id.textView2);
        treinos_completos = view.findViewById(R.id.textView3);
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);
        listTrains = view.findViewById(R.id.listView);
        //set-up perfil
        Username.setText("Username: " + friend.getUsername());
        listTrain();
        // set-up botões
        logoutButton.setOnClickListener(v -> handleLogoutClick());
        halterButton.setOnClickListener(v -> handleHalterClick());
        perfilButton.setOnClickListener(v -> handlePerfilClick());
        statsButton.setOnClickListener(v -> handleStatsClick());
        return view;
    }

    private void listTrain() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<TreinosDone> treinos = new ArrayList<>();
        List<Task<?>> tasks = new ArrayList<>(); // List to hold all the tasks

        // Get all treinos for the user
        db.collection("treino_done")
                .whereEqualTo("user_id", friend.getId())
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        if (!task1.getResult().isEmpty()) {

                            // Loop through the documents returned in task1
                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                // Create a TreinosDone object with the data from Firestore
                                TreinosDone train = new TreinosDone(
                                        document.getLong("treino_id").intValue(),
                                        document.getString("data"),
                                        document.getLong("exec").intValue()
                                );

                                // For each treino, query treino_planos to get the name
                                Task<QuerySnapshot> treinoPlanosTask = db.collection("treino_planos")
                                        .whereEqualTo("user_id", friend.getId())
                                        .whereEqualTo("id", train.getTreino_id())
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                if (!task2.getResult().isEmpty()) {
                                                    // Set the train name if the query is successful
                                                    String nome = task2.getResult().getDocuments().get(0).getString("nome");
                                                    int trainID = task2.getResult().getDocuments().get(0).getLong("id").intValue();
                                                    train.setTrainName(nome);
                                                    train.setTreino_id(trainID);
                                                }
                                            }
                                            treinos.add(train); // Add the train to the list
                                        });

                                tasks.add(treinoPlanosTask); // Add this task to the list of tasks
                            }

                            // Combine all tasks and wait for them to complete
                            Tasks.whenAllComplete(tasks).addOnCompleteListener(finalTask -> {
                                // Once all tasks are complete, update the UI
                                requireActivity().runOnUiThread(() -> {
                                    listTrains.setLayoutManager(new LinearLayoutManager(getContext()));
                                    trainAdapter = new TrainAdapter(requireActivity(),treinos, friend.getId(),modelview.getUser().getValue().getId());
                                    listTrains.setAdapter(trainAdapter);
                                    treinos_completos.setText("Treinos Completos:" + String.valueOf(trainAdapter.getItemCount()));
                                });
                            });
                        }
                    }
                });
    }


    private void handleLogoutClick() {
        requireActivity().getSupportFragmentManager().popBackStack();
        ((MainActivity) requireActivity()).switchLogin();
    }

    private void handleHalterClick() {
        requireActivity().getSupportFragmentManager().popBackStack();
        ((MainActivity) requireActivity()).switchTrain();
    }

    private void handlePerfilClick() {
        requireActivity().getSupportFragmentManager().popBackStack();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void handleStatsClick() {
        requireActivity().getSupportFragmentManager().popBackStack();
        ((MainActivity) requireActivity()).switchtoStats();
    }

}