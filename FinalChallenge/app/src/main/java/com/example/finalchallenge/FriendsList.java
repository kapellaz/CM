package com.example.finalchallenge;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.finalchallenge.classes.Request;
import com.example.finalchallenge.classes.RequestAdapter;
import com.example.finalchallenge.classes.Utilizador;
import com.example.finalchallenge.classes.UtilizadorAdapter;
import com.example.finalchallenge.classes.viewModel;

public class FriendsList extends Fragment {
    private DatabaseHelper databaseHelper;
    private viewModel modelview;

    private RecyclerView listFriends, listRequests;

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

        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        listFriends = rootView.findViewById(R.id.rvFriends);
        listRequests = rootView.findViewById(R.id.rvRequests);

        // Create a list of friends
        List<Utilizador> friends = new ArrayList<>();
        friends.add(new Utilizador("Alice", "aaaa"));
        friends.add(new Utilizador("Bob", "aaaa"));
        friends.add(new Utilizador("Charlie", "aaaa"));

        // Set up the adapter and ListView
        listFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        UtilizadorAdapter adapterFriends = new UtilizadorAdapter(friends);
        listFriends.setAdapter(adapterFriends);

        List<Request> reqs = new ArrayList<>();
        reqs.add(new Request("Alice", "aaaa"));
        reqs.add(new Request("Bob", "aaaa"));
        reqs.add(new Request("Charlie", "aaaa"));

        listRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        RequestAdapter adapterRequest = new RequestAdapter(reqs, "aaaa");
        listRequests.setAdapter(adapterRequest);

        // Additional setup for ListView can go here
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.list_friends, menu);
                requireActivity().setTitle("Pesquisar");

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchItem.getActionView();
                assert searchView != null;
                searchView.setQueryHint("Type Here");

                // Set up the search view listener
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return true;
                    }
                });
                // Set up the Create Note
                MenuItem goBack = menu.findItem(R.id.action_back);
                goBack.setOnMenuItemClickListener(item -> {
                    requireActivity().getSupportFragmentManager().popBackStack();
                    return true;
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return rootView;
    }
}