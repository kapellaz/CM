package com.example.finalchallenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.finalchallenge.classes.TreinoExec;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;public class menu_principal extends Fragment {

    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;
    private TextView Username;
    private TextView treinos_completos;
    private DatabaseHelper databaseHelper;
    private List<TreinoExec> treinosExec = new ArrayList<>();
    private ProgressBar progressBar; // ProgressBar
    public menu_principal() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());

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

                List<TreinoExec> treinos = databaseHelper.getAllTreinosByUserId(id);


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
        ArrayAdapter<TreinoExec> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, treinosExec);
        listView.setAdapter(adapter);

        treinos_completos.setText("Treinos Completos: " + treinos);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_menu_principal, container, false);

        // Inicializa o ProgressBar
        progressBar = view.findViewById(R.id.progressBar);

        // Inicializa o ListView e outros botões
        ListView listView = view.findViewById(R.id.listView);
        ArrayAdapter<TreinoExec> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, treinosExec);
        listView.setAdapter(adapter);
        Username = view.findViewById(R.id.textView2);
        treinos_completos = view.findViewById(R.id.textView3);
        Username.setText("Username: Bruno");
        // Inicializa os botões
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);

        // Configura os listeners para os botões
        logoutButton.setOnClickListener(v -> handleLogoutClick());
        halterButton.setOnClickListener(v -> handleHalterClick());
        perfilButton.setOnClickListener(v -> handlePerfilClick());
        statsButton.setOnClickListener(v -> handleStatsClick());


        Integer id = 2;
        getTreinos(id,treinos_completos);



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
