package com.example.finalchallenge;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.finalchallenge.classes.viewModel;

public class FriendsList extends Fragment {
    private DatabaseHelper databaseHelper;
    private viewModel modelview;

    private ProgressBar progressBar;
    private ListView listView;
    private ImageButton logoutButton, halterButton, perfilButton, statsButton;
    private Button requestButton;
    public FriendsList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        listView = rootView.findViewById(R.id.list_view);
        logoutButton = rootView.findViewById(R.id.logout);
        halterButton = rootView.findViewById(R.id.halter);
        perfilButton = rootView.findViewById(R.id.perfil);
        statsButton = rootView.findViewById(R.id.stats);
        requestButton = rootView.findViewById(R.id.requests);

        logoutButton.setOnClickListener(v -> handleLogoutClick());
        halterButton.setOnClickListener(v -> handleHalterClick());
        perfilButton.setOnClickListener(v -> handlePerfilClick());
        statsButton.setOnClickListener(v -> handleStatsClick());
        requestButton.setOnClickListener(v -> handleRequestsClick());
        // Additional setup for ListView can go here

        return rootView;
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

    private void handleRequestsClick() {
        ((MainActivity) requireActivity()).switchtoRequests();
    }
}