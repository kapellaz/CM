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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.finalchallenge.classes.Request;
import com.example.finalchallenge.classes.RequestAdapter;
import com.example.finalchallenge.classes.Utilizador;
import com.example.finalchallenge.classes.UtilizadorAdapter;
import com.example.finalchallenge.classes.viewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

        //Set-up the Requests
        setupRequestList(modelview.getUser().getValue().getId());

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

    private void setupRequestList(String userID) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            List<String> senderIDs = new ArrayList<>();
            List<Request> requests = new ArrayList<>();
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                //coletar os pedidos de amizade
                db.collection("pedido_amizade")
                    .whereEqualTo("recebeu", userID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    senderIDs.add(document.getString("enviou"));
                                }

                                List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                                for (String senderID : senderIDs) {
                                    tasks.add(db.collection("users").document(senderID).get());
                                }

                                //conseguir os users que enviaram os pedidos
                                Tasks.whenAllSuccess(tasks).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {

                                        for (Task<DocumentSnapshot> t : tasks) {
                                            DocumentSnapshot document = t.getResult();
                                            Request request = new Request(document.getString("username"), document.getId());
                                            requests.add(request);
                                        }

                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                listRequests.setLayoutManager(new LinearLayoutManager(getContext()));
                                                RequestAdapter requestAdapter = new RequestAdapter(requests,userID);
                                                listRequests.setAdapter(requestAdapter);
                                            }
                                        });

                                    }
                                });
                            } else {
                                // User not found in Firebase
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Nothing was found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            // Error retrieving data from Firebase
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Error in Requests", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
            }
        });
    }
}