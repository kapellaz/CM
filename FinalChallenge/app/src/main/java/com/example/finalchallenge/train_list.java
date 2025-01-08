package com.example.finalchallenge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalchallenge.classes.Exercicio;
import com.example.finalchallenge.classes.TreinoExec;
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
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        firebaseFirestorehelper = new FirebaseFirestorehelper();
        System.out.println(databaseHelper.getAllExercicios());

        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
    }


    private void getTreinos(String id) {
        // Cria um ExecutorService para rodar a consulta em uma thread separada
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
                // Chama o método do databaseHelper para pegar os treinos
                List<TreinoPlano> treinos = databaseHelper.getAllTreinosPlanoByUserId(id);

                // Atualiza a UI com os dados (precisa rodar na thread principal)
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (treinos != null) {
                            treinosExec = treinos;
                            updateListView(treinosExec); // Atualiza o ListView
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

    private void updateListView(List<TreinoPlano> treinos) {
        // Atualiza o ListView com os dados obtidos
        ListView listView = getView().findViewById(R.id.list_view);
        ArrayAdapter<TreinoPlano> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, treinos);
        listView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_train_list, container, false);

        // Inicializando o ListView
        ListView listView = view.findViewById(R.id.list_view);
        progressBar = view.findViewById(R.id.progressBar);
        ArrayAdapter<TreinoPlano> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_checked, treinosExec);

        // Definindo o Adapter para o ListView
        listView.setAdapter(adapter);
        add_new_training = view.findViewById(R.id.addnew);


        // Inicializa os botões
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);

        add_new_training.setOnClickListener(v -> showAddExerciseDialog());

        // Configura os listeners para os botões
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ação ao clicar no botão de logout
                handleLogoutClick();
            }
        });

        halterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ação ao clicar no botão de halter
                handleHalterClick();
            }
        });

        perfilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ação ao clicar no botão de perfil
                handlePerfilClick();
            }
        });

        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ação ao clicar no botão de estatísticas
                handleStatsClick();
            }
        });
        String id = modelview.getUser().getValue().getId();
        getTreinos(id);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ação ao clicar em um item da lista
                  TreinoPlano selectedItem = (TreinoPlano) parent.getItemAtPosition(position);
                    System.out.println("ID:_" + selectedItem.getId());
                  handleItemClick(selectedItem);
            }
        });

        return view;

    }



    // Método para exibir o diálogo de adicionar exercício
    private void showAddExerciseDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_create_workout);

        // Personaliza o fundo do diálogo (usando o drawable com bordas arredondadas)
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);


        final EditText inputWorkoutName = dialog.findViewById(R.id.workout_name);
        Button criar = dialog.findViewById(R.id.create_button);
        Button cancelar = dialog.findViewById(R.id.cancel_button);

        // Configurar o botão "Criar"
        criar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String workoutName = inputWorkoutName.getText().toString().trim();
                if (!workoutName.isEmpty()) {

                    createWorkout(workoutName);
                    dialog.dismiss(); // Fecha o diálogo
                } else {
                    // Exibir erro caso o nome esteja vazio
                    inputWorkoutName.setError("Nome do treino é obrigatório");
                }

            }
        });

        // Configurar o botão "Cancelar"
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Fecha o diálogo
            }
        });

        dialog.show();
    }

    // Método para criar o treino
    private void createWorkout(String workoutName) {
        long id = databaseHelper.createPlan(workoutName,modelview.getUser().getValue().getId(),1);
        TreinoPlano plan = new TreinoPlano((int) id,workoutName,modelview.getUser().getValue().getId());
        firebaseFirestorehelper.createPlan((int)id,workoutName,modelview.getUser().getValue().getId());
        handleItemClick(plan);
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

    private void handleItemClick(TreinoPlano item) {
        ((MainActivity) requireActivity()).switchDetailsTrain();
        modelview.setSelectedPlan(item);
      }

}