package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.finalchallenge.classes.ExerciseDetailed;
import com.example.finalchallenge.classes.TreinoExec;
import com.example.finalchallenge.classes.TreinosDetails;
import com.example.finalchallenge.classes.TreinosDone;
import com.example.finalchallenge.classes.viewModel;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;public class menu_principal extends Fragment {

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

    public menu_principal() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);

        //databaseHelper.inserirPlanosTreino2(); // - SE FOR A PRIMEIRA VEZ A CORRER ESTA MERDA
        Integer id = 2;

    }

    private void getTreinos(Integer id, TextView treinos_completos) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Exibe o ProgressBar enquanto carrega os dados
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);  // Mostrar o ProgressBar
                }
            }
        });

        // Executa a tarefa de busca dos treinos em segundo plano
        executor.execute(new Runnable() {
            @Override
            public void run() {

                List<TreinosDone> treinos = databaseHelper.getAllTreinosDoneByUserId(modelview.getUser().getValue().getId());


                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (treinos != null) {
                            treinosExec = treinos;
                            updateListView(treinos_completos); // Atualiza o ListView
                        }

                        // Esconde o ProgressBar quando os dados forem carregados
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void updateListView(TextView treinos_completos) {
        // Atualiza o ListView com os dados obtidos
        int treinos = treinosExec.size();
        ListView listView = getView().findViewById(R.id.listView);
        ArrayAdapter<TreinosDone> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, treinosExec);
        listView.setAdapter(adapter);

        treinos_completos.setText("Treinos Completos: " + treinos);

    }



    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_menu_principal, container, false);

        // Inicializa o ProgressBar
        progressBar = view.findViewById(R.id.progressBar);

        // Inicializa o ListView e outros botões
        ListView listView = view.findViewById(R.id.listView);
        ArrayAdapter<TreinosDone> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, treinosExec);
        listView.setAdapter(adapter);
        Username = view.findViewById(R.id.textView2);
        treinos_completos = view.findViewById(R.id.textView3);
        Username.setText("Username: " + modelview.getUser().getValue().getUsername());
        // Inicializa os botões
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);
        friendsButton = view.findViewById(R.id.friend_list);

        // Configura os listeners para os botões
        logoutButton.setOnClickListener(v -> handleLogoutClick());
        halterButton.setOnClickListener(v -> handleHalterClick());
        perfilButton.setOnClickListener(v -> handlePerfilClick());
        statsButton.setOnClickListener(v -> handleStatsClick());
        friendsButton.setOnClickListener(v -> handleFriendsClick());
        Integer id = 2;
        getTreinos(id,treinos_completos);


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
        });
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

    private void handleFriendsClick() {
        ((MainActivity) requireActivity()).switchtoFriends();
    }
}
