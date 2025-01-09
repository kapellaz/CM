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


    /**
     * Initializes the fragment and sets up essential components such as the database and modelview.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        list_of_exercises = databaseHelper.getAllExercicios();
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);

    }

    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_exercise__list, container, false);


        ListView listView = view.findViewById(R.id.listView);


        ArrayAdapter<Exercicio> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, list_of_exercises);


        listView.setAdapter(adapter);

        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Exercicio selectedItem = (Exercicio) parent.getItemAtPosition(position);

                modelview.setExercicio(selectedItem);
                handleItemClick(selectedItem.getNome());
            }
        });


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

        return view;

    }

    /**
     * Switches the current fragment to the Logout fragment.
     */
    private void handleLogoutClick() {
        ((MainActivity) requireActivity()).switchLogin();
    }
    /**
     * Switches the current fragment to the Train fragment.
     */

    private void handleHalterClick() {
        ((MainActivity) requireActivity()).switchTrain();
    }

    /**
     * Switches the current fragment to the Peerfil fragment.
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
     * Switches the current fragment to the Details Exercise fragment.
     */
    private void handleItemClick(String item) {
        ((MainActivity) requireActivity()).switchDetailsExercise();
    }


}