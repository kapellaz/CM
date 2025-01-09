package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.finalchallenge.classes.ExerciseDetailed;
import com.example.finalchallenge.classes.TreinosDetails;
import com.example.finalchallenge.classes.TreinosDone;
import com.example.finalchallenge.classes.Utilizador;
import com.example.finalchallenge.classes.viewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
        ListView listView = view.findViewById(R.id.listView);
        Username = view.findViewById(R.id.textView2);
        treinos_completos = view.findViewById(R.id.textView3);
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);

        //set-up perfil
        Username.setText("Username: " + friend.getUsername());

        // set-up botões
        logoutButton.setOnClickListener(v -> handleLogoutClick());
        halterButton.setOnClickListener(v -> handleHalterClick());
        perfilButton.setOnClickListener(v -> handlePerfilClick());
        statsButton.setOnClickListener(v -> handleStatsClick());
        /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the selected training
                TreinosDone selectedTraining = treinosExec.get(position);

                TreinosDetails selectedTrainingDetails = databaseHelper.getTreinoDetails(selectedTraining);
                // Get the details (assuming ExerciseDetailed is a field in TreinosDone)
                TreinosDetails details = selectedTrainingDetails;

                // Prepare details to show in the dialog
                StringBuilder detailsText = new StringBuilder();
                for (ExerciseDetailed exercise : details.getExercise()) {
                    detailsText.append(exercise);
                    detailsText.append("Series Information:\n");

                    // Iterate over the series map to display the data
                    for (Map.Entry<Integer, Integer> entry : exercise.getSeriesMap().entrySet()) {
                        detailsText.append("Set ").append(entry.getKey())
                                .append(": ").append(entry.getValue()).append(" kilos\n");
                    }
                }

                // Show details in an AlertDialog
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                dialogBuilder.setTitle("Training Details");
                dialogBuilder.setMessage(detailsText.toString());
                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.create().show();
            }
        });*/
        return view;
    }

    private void handleLogoutClick() {
        ((MainActivity) requireActivity()).switchLogin();
    }

    private void handleHalterClick() {
        ((MainActivity) requireActivity()).switchTrain();
    }

    private void handlePerfilClick() {
        ((MainActivity) requireActivity()).switchMenu();
    }

    private void handleStatsClick() {
        ((MainActivity) requireActivity()).switchtoStats();
    }

}