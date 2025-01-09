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
import com.example.finalchallenge.classes.TreinoExec;
import com.example.finalchallenge.classes.TreinosDetails;
import com.example.finalchallenge.classes.TreinosDone;
import com.example.finalchallenge.classes.Utilizador;
import com.example.finalchallenge.classes.viewModel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    public interface FirebaseSyncCallback {
        void onComplete();
    }

    private void synchronizeFirebaseDataAndUpdateList(TextView treinos_completos) {
        System.out.println(modelview.getFirstTIMEinFrag().getValue());
        if(Boolean.TRUE.equals(modelview.getIsFirstTime().getValue()) && modelview.getUser().getValue().getFirstTimeFragment() && InternetOn) {
            System.out.println("No account");
            String userId = modelview.getUser().getValue().getId();
            // Contador para rastrear operações concluídas
            AtomicInteger pendingTasks = new AtomicInteger(4); // Número de operações do Firebase
            FirebaseSyncCallback onTaskComplete = () -> {
                if (pendingTasks.decrementAndGet() == 0) {
                    // Todas as operações foram concluídas, chama getTreinos
                    getTreinos(2, treinos_completos);
                }
            };
            firebaseFirestorehelper.getAllPlansFromFirebase(userId, databaseHelper, onTaskComplete);
            firebaseFirestorehelper.getAllPlansExerciseFromFirebase(userId, databaseHelper, onTaskComplete);
            firebaseFirestorehelper.getAllTreinoDoneFromFirebase(userId, databaseHelper, onTaskComplete);
            firebaseFirestorehelper.getAllSeriesFromFirebase(userId, databaseHelper, onTaskComplete);
            modelview.getUser().getValue().setFirstTimeFragment(false);
        } else if (modelview.getUser().getValue().getFirstTimeFragment() && InternetOn) {
            System.out.println("Syncronized");
            AtomicInteger pendingTasks = new AtomicInteger(4); // Número de operações do Firebase
            FirebaseSyncCallback onTaskComplete = () -> {
                if (pendingTasks.decrementAndGet() == 0) {

                    getTreinos(2, treinos_completos);
                }
            };
            firebaseFirestorehelper.syncLocalDataToFirebasePLANOS(modelview.getUser().getValue().getId(),databaseHelper, onTaskComplete);
            firebaseFirestorehelper.syncLocalDataToFirebaseExercicios(modelview.getUser().getValue().getId(),databaseHelper, onTaskComplete);
            firebaseFirestorehelper.syncLocalDataToFirebaseTreinoDone(modelview.getUser().getValue().getId(),databaseHelper, onTaskComplete);
            firebaseFirestorehelper.syncLocalDataToFirebaseSerie(modelview.getUser().getValue().getId(),databaseHelper, onTaskComplete);
            modelview.getUser().getValue().setFirstTimeFragment(false);
        } else {
            System.out.println("ALL CHECK");


            getTreinos(2, treinos_completos);
            modelview.getUser().getValue().setFirstTimeFragment(false);
        }
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

                System.out.println("TREINOS " + treinos);
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
        synchronizeFirebaseDataAndUpdateList(treinos_completos);
        modelview.setIsFirstTime(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the selected training
                TreinosDone selectedTraining = treinosExec.get(position);

                TreinosDetails selectedTrainingDetails = databaseHelper.getTreinoDetails(selectedTraining);
                // Get the details (assuming ExerciseDetailed is a field in TreinosDone)
                TreinosDetails details = selectedTrainingDetails;
                System.out.println("Details: " + details);

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
        if(InternetOn){
            ((MainActivity) requireActivity()).switchtoFriends();
        }else{
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCategoriesAndExercises() {

        try {
            // Obter todas as categorias
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

            // Processar categorias obtidas
            List<String> categories = parseCategories(response.toString());

            // Obter os exercícios de cada categoria
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
            // Criar o JSONArray a partir da resposta JSON
            JSONArray categoriesArray = new JSONArray(jsonResponse);

            // Iterar sobre o array de categorias e adicionar cada categoria à lista
            for (int i = 0; i < categoriesArray.length(); i++) {
                String category = categoriesArray.getString(i);

                // Adicionar a categoria à lista
                categories.add(category);
            }
        } catch (Exception e) {
            Log.e("JSON Parsing Error", "Erro ao parsear a resposta JSON: " + e.getMessage());
        }

        return categories;
    }


    private List<String> getExercisesForCategory(String category) {
        List<String> exercises = new ArrayList<>();
        try {
            System.out.println("ENTROU PARA PEGAR EM CA");
            // Fazer a requisição para obter os exercícios da categoria
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
            System.out.println("EXERCICI)OS " + response.toString());

            // Processar exercícios obtidos para a categoria
            exercises = parseExercises(response.toString());

        } catch (IOException e) {
            Log.e("Network Error", "Erro ao obter exercícios para a categoria " + category + ": " + e.getMessage());
        }
        return exercises;
    }

    private List<String> parseExercises(String jsonResponse) {
        List<String> exercises = new ArrayList<>();
        try {
            // Criar o JSONObject a partir da resposta JSON
            JSONObject responseObject = new JSONObject(jsonResponse);

            // Obter o array de exercícios dentro do campo "exercises"
            JSONArray exercisesArray = responseObject.getJSONArray("exercises");

            // Iterar sobre o array de exercícios
            for (int i = 0; i < exercisesArray.length(); i++) {
                // Obter o exercício atual
                JSONObject exerciseObject = exercisesArray.getJSONObject(i);

                // Extrair o nome e o músculo
                String name = exerciseObject.getString("name");
                String muscle = exerciseObject.getString("muscle");

                // Criar a string no formato "name - muscle" e adicionar à lista
                String exerciseInfo = name + " - " + muscle;
                exercises.add(exerciseInfo);
            }
        } catch (Exception e) {
            Log.e("JSON Parsing Error", "Erro ao parsear a resposta JSON: " + e.getMessage());
        }
        return exercises;
    }


}
