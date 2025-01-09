package com.example.finalchallenge;


import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.finalchallenge.classes.TreinoPlano;
import com.example.finalchallenge.classes.viewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class train_list extends Fragment {
    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;

    private ImageButton add_new_training;
    private DatabaseHelper databaseHelper;
    private viewModel modelview;
    private List<TreinoPlano> treinosExec = new ArrayList<>();
    private ProgressBar progressBar; // ProgressBar
    private FirebaseFirestorehelper firebaseFirestorehelper;
    public train_list() {

    }

    /**
     * Initializes the fragment and sets up essential components such as the ViewModel and database.
    */

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        firebaseFirestorehelper = new FirebaseFirestorehelper();
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
    }
    /**
     * Method that returns all trains by user id
     */

    private void getTreinos(String id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TreinoPlano> treinos = databaseHelper.getAllTreinosPlanoByUserId(id);
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (treinos != null) {
                            treinosExec = treinos;
                            updateListView(treinosExec);
                        }
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
    /**
     * Updates the ListView by associating a new adapter with the updated data(All plans).
     */
    private void updateListView(List<TreinoPlano> treinos) {

        ListView listView = getView().findViewById(R.id.list_view);
        ArrayAdapter<TreinoPlano> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, treinos);
        listView.setAdapter(adapter);
    }

    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_train_list, container, false);

        ListView listView = view.findViewById(R.id.list_view);
        progressBar = view.findViewById(R.id.progressBar);
        ArrayAdapter<TreinoPlano> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_checked, treinosExec);


        listView.setAdapter(adapter);
        add_new_training = view.findViewById(R.id.addnew);



        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);

        add_new_training.setOnClickListener(v -> showAddExerciseDialog());


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogoutClick();
            }
        });

        halterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleHalterClick();
            }
        });

        perfilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePerfilClick();
            }
        });

        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStatsClick();
            }
        });
        String id = modelview.getUser().getValue().getId();
        getTreinos(id);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                  TreinoPlano selectedItem = (TreinoPlano) parent.getItemAtPosition(position);
                  handleItemClick(selectedItem);
            }
        });

        return view;

    }


    /**
     * Method to display the add exercise dialog box
     */
    private void showAddExerciseDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_create_workout);


        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);


        final EditText inputWorkoutName = dialog.findViewById(R.id.workout_name);
        Button criar = dialog.findViewById(R.id.create_button);
        Button cancelar = dialog.findViewById(R.id.cancel_button);


        criar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String workoutName = inputWorkoutName.getText().toString().trim();
                if (!workoutName.isEmpty()) {

                    createWorkout(workoutName);
                    dialog.dismiss();
                } else {

                    inputWorkoutName.setError("Nome do treino é obrigatório");
                }

            }
        });


        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    /**
     * Method to create a Plan
     * @param workoutName - Name of Plan
     */
    private void createWorkout(String workoutName) {
        long id = databaseHelper.createPlan(workoutName,modelview.getUser().getValue().getId(),1);
        TreinoPlano plan = new TreinoPlano((int) id,workoutName,modelview.getUser().getValue().getId(),1);
        firebaseFirestorehelper.createPlan((int)id,workoutName,modelview.getUser().getValue().getId());
        handleItemClick(plan);
    }




    /**
     * Switches the current fragment to the Logout fragment.
     */
    private void handleLogoutClick() {
        ((MainActivity) requireActivity()).switchLogin();
    }

    /**
     * Switches the current fragment to the Halter fragment.
     */
    private void handleHalterClick() {
        ((MainActivity) requireActivity()).switchTrain();
    }

    /**
     * Switches the current fragment to the Perfil fragment.
     */
    private void handlePerfilClick() {
        ((MainActivity) requireActivity()).switchMenu();
    }


    /**
     * Switches the current fragment to the Stats fragment.
     */
    private void handleStatsClick() {
        ((MainActivity) requireActivity()).switchtoStats();
    }


    /**
     * Switches the current fragment to the Details Train fragment.
     */
    private void handleItemClick(TreinoPlano item) {
        ((MainActivity) requireActivity()).switchDetailsTrain();
        modelview.setSelectedPlan(item);
      }

}