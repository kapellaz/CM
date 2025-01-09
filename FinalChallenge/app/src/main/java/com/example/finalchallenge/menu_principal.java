package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.finalchallenge.classes.ExerciseDetailed;
import com.example.finalchallenge.classes.TreinosDetails;
import com.example.finalchallenge.classes.TreinosDone;
import com.example.finalchallenge.classes.viewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class menu_principal extends Fragment {

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
    private FirebaseFirestorehelper firebaseFirestorehelper;
    private Boolean InternetOn;
    private Executor executorService = Executors.newSingleThreadExecutor();
    public menu_principal() {
        // Required empty public constructor
    }

    /**
     * Initializes the fragment and sets up essential components such as the ViewModel and database.
     * If the table of exercises is empty, an API call will be made to get all exercises
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
        firebaseFirestorehelper = new FirebaseFirestorehelper();

        InternetOn = isNetworkConnected();
        if(InternetOn && modelview.getUser().getValue().getFirstTimeFragment()){
            executorService.execute(new Runnable() {
                @Override
                public void run() {

                    if (databaseHelper.isExercisesTableEmpty()) {
                        getCategoriesAndExercises();
                    } else {
                        Log.d("Database", "Exercícios já estão na base de dados.");
                    }
                }
            });
        }

    }
    /**
     * method checks whether the device is connected to a network.
     * @return true if are connect, otherwise false
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    /**
     * Method that allows the class to perform specific actions when a Firebase-related asynchronous operation completes.
     */
    public interface FirebaseSyncCallback {
        void onComplete();
    }

    /**
     * Method to synchronize database data with firebase.
     * If it is a new user on the local cell phone, but already exists in firebase. This will get all data via firebase.
     * If it already exists on your phone, all data from your phone will be transferred to Firebase.
     * @param treinos_completos - Textview to update info
     */

    private void synchronizeFirebaseDataAndUpdateList(TextView treinos_completos) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        System.out.println(modelview.getFirstTIMEinFrag().getValue());
        if(Boolean.TRUE.equals(modelview.getIsFirstTime().getValue()) && modelview.getUser().getValue().getFirstTimeFragment() && InternetOn) {

            String userId = modelview.getUser().getValue().getId();

            AtomicInteger pendingTasks = new AtomicInteger(4);
            FirebaseSyncCallback onTaskComplete = () -> {
                if (pendingTasks.decrementAndGet() == 0) {

                    getTreinos(treinos_completos);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            };
            firebaseFirestorehelper.getAllPlansFromFirebase(userId, databaseHelper, onTaskComplete);
            firebaseFirestorehelper.getAllPlansExerciseFromFirebase(userId, databaseHelper, onTaskComplete);
            firebaseFirestorehelper.getAllTreinoDoneFromFirebase(userId, databaseHelper, onTaskComplete);
            firebaseFirestorehelper.getAllSeriesFromFirebase(userId, databaseHelper, onTaskComplete);
            modelview.getUser().getValue().setFirstTimeFragment(false);
        } else if (modelview.getUser().getValue().getFirstTimeFragment() && InternetOn) {

            AtomicInteger pendingTasks = new AtomicInteger(4);
            FirebaseSyncCallback onTaskComplete = () -> {
                if (pendingTasks.decrementAndGet() == 0) {

                    getTreinos(treinos_completos);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            };
            firebaseFirestorehelper.syncLocalDataToFirebasePLANOS(modelview.getUser().getValue().getId(),databaseHelper, onTaskComplete);
            firebaseFirestorehelper.syncLocalDataToFirebaseExercicios(modelview.getUser().getValue().getId(),databaseHelper, onTaskComplete);
            firebaseFirestorehelper.syncLocalDataToFirebaseTreinoDone(modelview.getUser().getValue().getId(),databaseHelper, onTaskComplete);
            firebaseFirestorehelper.syncLocalDataToFirebaseSerie(modelview.getUser().getValue().getId(),databaseHelper, onTaskComplete);
            modelview.getUser().getValue().setFirstTimeFragment(false);
        } else {
            getTreinos(treinos_completos);
            modelview.getUser().getValue().setFirstTimeFragment(false);
        }
    }


    private void getTreinos(TextView treinos_completos) {

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

                List<TreinosDone> treinos = databaseHelper.getAllTreinosDoneByUserId(modelview.getUser().getValue().getId());

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (treinos != null) {
                            treinosExec = treinos;
                            updateListView(treinos_completos);
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
     * Updates the ListView by associating a new adapter with the updated data(All training dones).
     */

    private void updateListView(TextView treinos_completos) {
        int treinos = treinosExec.size();
        ListView listView = getView().findViewById(R.id.listView);
        ArrayAdapter<TreinosDone> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, treinosExec);
        listView.setAdapter(adapter);
        treinos_completos.setText("Treinos Completos: " + treinos);
    }


    /**
     * Called when the fragment is first created.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu_principal, container, false);


        progressBar = view.findViewById(R.id.progressBar);


        ListView listView = view.findViewById(R.id.listView);
        ArrayAdapter<TreinosDone> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, treinosExec);
        listView.setAdapter(adapter);
        Username = view.findViewById(R.id.textView2);
        treinos_completos = view.findViewById(R.id.textView3);
        Username.setText("Username: " + modelview.getUser().getValue().getUsername());

        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);
        friendsButton = view.findViewById(R.id.friend_list);


        logoutButton.setOnClickListener(v -> handleLogoutClick());
        halterButton.setOnClickListener(v -> handleHalterClick());
        perfilButton.setOnClickListener(v -> handlePerfilClick());
        statsButton.setOnClickListener(v -> handleStatsClick());
        friendsButton.setOnClickListener(v -> handleFriendsClick());

        synchronizeFirebaseDataAndUpdateList(treinos_completos);
        modelview.setIsFirstTime(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TreinosDone selectedTraining = treinosExec.get(position);

                TreinosDetails selectedTrainingDetails = databaseHelper.getTreinoDetails(selectedTraining);

                TreinosDetails details = selectedTrainingDetails;

                StringBuilder detailsText = new StringBuilder();
                for (ExerciseDetailed exercise : details.getExercise()) {
                    detailsText.append(exercise);
                    detailsText.append("Series Information:\n");


                    for (Map.Entry<Integer, Integer> entry : exercise.getSeriesMap().entrySet()) {
                        detailsText.append("Set ").append(entry.getKey())
                                .append(": ").append(entry.getValue()).append(" kilos\n");
                    }
                }


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
     * Switches the current fragment to the Friends fragment, only if internet connection exists.
     */
    private void handleFriendsClick() {
        if(InternetOn){
            ((MainActivity) requireActivity()).switchtoFriends();
        }else{
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCategoriesAndExercises() {
        try {
            URL url = new URL("https://api.algobook.info/v1/gym/categories");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("API Error", "Erro ao obter categorias. Response Code: " + responseCode);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            List<String> categories = parseCategories(response.toString());
            List<String> allExercises = new ArrayList<>();
            for (String category : categories) {
                List<String> exercises = getExercisesForCategory(category);
                allExercises.addAll(exercises);
            }
            databaseHelper.AddExerciseAPIintoBD((ArrayList<String>) allExercises);

        } catch (IOException e) {
            Log.e("Network Error", "Erro de rede: " + e.getMessage());
        }
    }



    private List<String> parseCategories(String jsonResponse) {
        List<String> categories = new ArrayList<>();
        try {
            JSONArray categoriesArray = new JSONArray(jsonResponse);
            for (int i = 0; i < categoriesArray.length(); i++) {
                String category = categoriesArray.getString(i);
                categories.add(category);
            }
        } catch (Exception e) {
            Log.e("JSON Parsing Error", "Erro ao parsear a resposta JSON: " + e.getMessage());
        }

        return categories;
    }

    /**
     * // Method to fetch exercises for a given category
     * @param category - category in question
     * @return - all exercises from this category
     */
    private List<String> getExercisesForCategory(String category) {
        List<String> exercises = new ArrayList<>();
        try {

            URL url = new URL("https://api.algobook.info/v1/gym/categories/" + category);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("API Error", "Erro ao obter exercícios para a categoria " + category + ". Response Code: " + responseCode);
                return exercises;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();



            exercises = parseExercises(response.toString());

        } catch (IOException e) {
            Log.e("Network Error", "Erro ao obter exercícios para a categoria " + category + ": " + e.getMessage());
        }
        return exercises;
    }

    /**
     * Method to parse exercises from the JSON response
     * @param jsonResponse - All exercises
     * @return - Return in right format
     */

    private List<String> parseExercises(String jsonResponse) {
        List<String> exercises = new ArrayList<>();
        try {
            JSONObject responseObject = new JSONObject(jsonResponse);

            JSONArray exercisesArray = responseObject.getJSONArray("exercises");


            for (int i = 0; i < exercisesArray.length(); i++) {

                JSONObject exerciseObject = exercisesArray.getJSONObject(i);

                String name = exerciseObject.getString("name");
                String muscle = exerciseObject.getString("muscle");


                String exerciseInfo = name + " - " + muscle;
                exercises.add(exerciseInfo);
            }
        } catch (Exception e) {
            Log.e("JSON Parsing Error", "Erro ao parsear a resposta JSON: " + e.getMessage());
        }
        return exercises;
    }


}
