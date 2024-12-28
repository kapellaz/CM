package com.example.finalchallenge;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.finalchallenge.classes.Exercicio;
import com.example.finalchallenge.classes.viewModel;

import java.util.List;


public class Exercise_List extends Fragment {

    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;

    private List<Exercicio> list_of_exercises;
    private DatabaseHelper databaseHelper;
    private viewModel modelview;

    public Exercise_List() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        list_of_exercises = databaseHelper.getAllExercicios();
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_exercise__list, container, false);

        // Inicializando o ListView
        ListView listView = view.findViewById(R.id.listView);

        // Criando uma lista de itens (exemplo de treino ou lista de usuários)
        String[] items = {"Exercise 1", "Exercise 2", "Exercise 3", "Exercise 4", "Exercise 5"};

        // Criando o Adapter para a lista (pode ser um ArrayAdapter ou CustomAdapter)
        // Criando o Adapter para a lista (pode ser um ArrayAdapter ou CustomAdapter)
        ArrayAdapter<Exercicio> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, list_of_exercises);

        // Definindo o Adapter para o ListView
        listView.setAdapter(adapter);
        // Inicializa os botões
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ação ao clicar em um item da lista
                Exercicio selectedItem = (Exercicio) parent.getItemAtPosition(position);
                modelview.setExercicio(selectedItem);
                handleItemClick(selectedItem.getNome());
            }
        });

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
    private void handleItemClick(String item) {
        ((MainActivity) requireActivity()).switchDetailsExercise();
    }

}