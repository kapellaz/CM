package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.graphics.Color;
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
import java.util.concurrent.atomic.AtomicInteger;

import com.example.finalchallenge.classes.OthersAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalchallenge.classes.Request;
import com.example.finalchallenge.classes.RequestAdapter;
import com.example.finalchallenge.classes.Utilizador;
import com.example.finalchallenge.classes.UtilizadorAdapter;
import com.example.finalchallenge.classes.viewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FriendsList extends Fragment {
    private DatabaseHelper databaseHelper;
    private viewModel modelview;
    private RecyclerView listFriends, listRequests, listOthers;
    private UtilizadorAdapter adapterFriends;
    private RequestAdapter requestAdapter;
    private OthersAdapter othersAdapter;

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
        listOthers = rootView.findViewById(R.id.rvOthers);

        // Set up amigos
        setupFriendsList(modelview.getUser().getValue().getId());
        //Set-up the pedidos
        setupRequestList(modelview.getUser().getValue().getId());
        //Set-up Others
        setupOthers(modelview.getUser().getValue().getId());

        // menu de pesquisa
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.list_friends, menu);
                requireActivity().setTitle("Pesquisar");

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchItem.getActionView();
                @SuppressLint("DiscouragedApi") int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
                TextView searchText = searchView.findViewById(id);
                if (searchText != null) {
                    searchText.setTextColor(Color.WHITE); // Define a cor do texto
                    searchText.setHintTextColor(Color.LTGRAY); // Define a cor do placeholder
                }
                assert searchView != null;
                searchView.setQueryHint("Type Here");


                // Set up the search view listener
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }


                    //reagir aos inputs do user
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adapterFriends.filter(newText);
                        requestAdapter.filter(newText);
                        othersAdapter.filter(newText);
                        return true;
                    }
                });

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

    private void setupFriendsList(String userID){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            List<String> amigosIDs = new ArrayList<>();
            List<Utilizador> amigos = new ArrayList<>();
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                //coletar os amigos
                db.collection("amigos")
                        .whereEqualTo("user1", userID)
                        .get()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {

                                // Process results from the first query
                                if (!task1.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task1.getResult()) {
                                        amigosIDs.add(document.getString("user2"));
                                    }
                                }

                                // Perform the second query
                                db.collection("amigos")
                                        .whereEqualTo("user2", userID)
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                // Process results from the second query
                                                if (!task2.getResult().isEmpty()) {
                                                    for (QueryDocumentSnapshot document : task2.getResult()) {
                                                        amigosIDs.add(document.getString("user1"));
                                                    }
                                                }

                                                // Fetch user documents for all found `amigosIDs`
                                                List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                                                for (String amigoID : amigosIDs) {
                                                    tasks.add(db.collection("users").document(amigoID).get());
                                                }

                                                Tasks.whenAllSuccess(tasks).addOnCompleteListener(task3 -> {
                                                    if (task3.isSuccessful()) {

                                                        for (Task<DocumentSnapshot> t : tasks) {
                                                            DocumentSnapshot document = t.getResult();
                                                            Utilizador amigo = new Utilizador( document.getString("username"),document.getId());
                                                            amigos.add(amigo);
                                                        }

                                                        // Update UI with the friends list
                                                        requireActivity().runOnUiThread(() -> {
                                                            listFriends.setLayoutManager(new LinearLayoutManager(getContext()));
                                                            adapterFriends = new UtilizadorAdapter(requireActivity(),amigos);
                                                            listFriends.setAdapter(adapterFriends);
                                                        });
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
            }
        });
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
                                                requestAdapter = new RequestAdapter(requests,userID);
                                                listRequests.setAdapter(requestAdapter);
                                            }
                                        });

                                    }
                                });
                            }else{
                                requestAdapter = new RequestAdapter();
                            }
                        }
                    });
            }
        });
    }

    private void setupOthers(String userID) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            List<Utilizador> users = new ArrayList<>();

            db.collection("users")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Task<?>> tasks = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (userID.equals(document.getId())) {
                                    continue; // Skip the current user
                                }

                                // Create tasks for all checks
                                Task<QuerySnapshot> friendRequestSent = db.collection("pedido_amizade")
                                        .whereEqualTo("recebeu", document.getId())
                                        .whereEqualTo("enviou", userID)
                                        .get();
                                Task<QuerySnapshot> friendRequestReceived = db.collection("pedido_amizade")
                                        .whereEqualTo("enviou", document.getId())
                                        .whereEqualTo("recebeu", userID)
                                        .get();
                                Task<QuerySnapshot> friends1 = db.collection("amigos")
                                        .whereEqualTo("user1", document.getId())
                                        .whereEqualTo("user2", userID)
                                        .get();
                                Task<QuerySnapshot> friends2 = db.collection("amigos")
                                        .whereEqualTo("user2", document.getId())
                                        .whereEqualTo("user1", userID)
                                        .get();

                                // Combine tasks into a single logic block
                                Task<List<Task<?>>> combinedTask = Tasks.whenAllComplete(friendRequestSent, friendRequestReceived, friends1, friends2)
                                        .addOnCompleteListener(allTasks -> {
                                            boolean novo = true;
                                            // Check results of individual tasks
                                            if (friendRequestSent.isComplete() && friendRequestSent.getResult() != null && !friendRequestSent.getResult().isEmpty()) {
                                                novo = false;
                                            }
                                            if (friendRequestReceived.isComplete() && friendRequestReceived.getResult() != null && !friendRequestReceived.getResult().isEmpty()) {
                                                novo = false;
                                            }
                                            if (friends1.isComplete() && friends1.getResult() != null && !friends1.getResult().isEmpty()) {
                                                novo = false;
                                            }
                                            if (friends2.isComplete() && friends2.getResult() != null && !friends2.getResult().isEmpty()) {
                                                novo = false;
                                            }
                                            // Add user if conditions are met
                                            if (novo) {
                                                Utilizador user = new Utilizador(document.getString("username"), document.getId());
                                                synchronized (users) {
                                                    users.add(user);
                                                }
                                            }
                                        });
                                tasks.add(combinedTask);
                            }
                            // Finalize when all tasks are complete
                            Tasks.whenAllComplete(tasks).addOnCompleteListener(finalTask -> {
                                requireActivity().runOnUiThread(() -> {
                                    listOthers.setLayoutManager(new LinearLayoutManager(getContext()));
                                    othersAdapter = new OthersAdapter(users,userID);
                                    listOthers.setAdapter(othersAdapter);
                                });
                            });
                        }
                    });
        });
    }


}