package com.example.finalchallenge;

import android.app.Dialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.finalchallenge.classes.Exercise;
import com.example.finalchallenge.classes.MQTThelper;
import com.example.finalchallenge.classes.SeriesInfo;
import com.example.finalchallenge.classes.TreinoPlano;
import com.example.finalchallenge.classes.viewModel;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import org.eclipse.paho.android.service.MqttAndroidClient;

public class train_detail extends Fragment {
    private Dialog dialogWeight;
    private Boolean InternetOn;
    private LinearLayout stopFinishLayout;
    private ImageButton startButton;
    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;
    private ImageButton finishButton;
    private viewModel modelview;
    private TreinoPlano treinoExec;
    private DatabaseHelper databaseHelper;
    private ProgressBar progressBar; // ProgressBar
    private List<Exercise> treinosExec = new ArrayList<>();
    private ImageButton deleteImageButton;
    private ImageButton helpbutton;
    private FirebaseFirestorehelper firebaseFirestorehelper;
    public MqttAndroidClient client;
    private String clientId;
    private String username;
    private int oxigenacao;
    private int batimentos;
    private ArrayList<SeriesInfo> listofexecutionsseries = new ArrayList<>();
    private TextView oxygenInfo;
    private TextView heartbeatInfo;
    final String server = "tcp://test.mosquitto.org:1883";
    private MQTThelper mqttHelper;
    private ArrayAdapter<Exercise> adapter;
    private int exec;
    private ImageButton editImageButton;
    private TextView buttonDelete;


    public train_detail() {
        // Required empty public constructor
    }

    /**
     * Initializes the fragment and sets up essential components such as the ViewModel, database, Firebase.
     * Establishes MQTT connection.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
        databaseHelper = new DatabaseHelper(getContext());
        firebaseFirestorehelper = new FirebaseFirestorehelper();

        treinoExec = modelview.getSelectedPlan().getValue();
        mqttHelper = new MQTThelper();
        clientId = modelview.getUser().getValue().getUsername();
        InternetOn = isNetworkConnected();


        connectToMqtt();

    }

    /**
     * method checks whether the device is connected to a network.
     * @return true if are connect, otherwise false
     */

    private boolean isNetworkConnected() {
        // Getting the ConnectivityManager with the correct Context
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            // Get active network information
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    /**
     * Method that searches for all exercises in a given plan
     * @param id - Plan ID
     */

    private void getExercises(Integer id) {

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

                List<Exercise> treinos = databaseHelper.getExercisesForTraining(id);

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (treinos != null) {
                            treinosExec = treinos;
                            updateListView();
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
     * custom adapter to display exercises in a ListView.
     */

    private ArrayAdapter<Exercise> createAdapter() {
        return new ArrayAdapter<Exercise>(requireActivity(), R.layout.item_exercise_start, R.id.exerciseName, treinosExec) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Check if the view can be reused
                if (convertView == null) {
                    // Inflate the layout if the view cannot be reused
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_exercise_start, parent, false);
                }


                ImageView checkMarkIcon = convertView.findViewById(R.id.checkMarkIcon);
                TextView exerciseName = convertView.findViewById(R.id.exerciseName);


                Exercise exercise = treinosExec.get(position);
                exerciseName.setText(exercise.toString());


                if (exercise.getSeries() == 0) {

                    checkMarkIcon.setVisibility(View.VISIBLE);
                } else {

                    checkMarkIcon.setVisibility(View.GONE);
                }

                return convertView;
            }
        };
    }


    /**
     * Updates the ListView by associating a new adapter with the updated data.
     */
    private void updateListView() {

        ListView listView = getView().findViewById(R.id.listView);
        ArrayAdapter<Exercise> adapter = createAdapter();
        listView.setAdapter(adapter);

    }
    /**
     * Method to check if all exercises from an plan are executed
     * @return true if all are executed, otherwise returns false
     */

    private boolean areAllExercisesCompleted() {
        for (Exercise exercise : treinosExec) {
            if (exercise.getSeries() != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called when the fragment is first created.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_train_detail, container, false);
        TextView trainingName = view.findViewById(R.id.exerciseName);
        trainingName.setText(Objects.requireNonNull(modelview.getSelectedPlan().getValue()).getNome());

        ListView listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);

        adapter = createAdapter();



        listView.setAdapter(adapter);
        listView.setEnabled(false);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            if(treinosExec.get(position).getSeries() != 0) {
                showWeightDialog(position); // Show the dialog box to enter the weight
            }
        });

        startButton = view.findViewById(R.id.startbutton);

        editImageButton = view.findViewById(R.id.editButton);
        stopFinishLayout = view.findViewById(R.id.linearLayoutStopFinish);

        finishButton = view.findViewById(R.id.finish);
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);

        // Set up listeners for buttons
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getVisibility()==View.GONE) { // if the user is executing one plan at this moment

                    showCancelTraining(new Runnable() {
                        @Override
                        public void run() {

                            handleLogoutClick();
                        }
                    });
                    return;
                }
                handleLogoutClick();
            }
        });

        halterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getVisibility()==View.GONE) { // if the user is executing one plan at this moment
                    showCancelTraining(new Runnable() {
                        @Override
                        public void run() {
                            handleHalterClick();
                        }
                    });
                    return;
                }
                handleHalterClick();
            }
        });

        perfilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getVisibility()==View.GONE) {// if the user is executing one plan at this moment

                    showCancelTraining(new Runnable() {
                        @Override
                        public void run() {

                            handlePerfilClick();
                        }
                    });
                    return;
                }
                handlePerfilClick();
            }
        });

        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  if (startButton.getVisibility()==View.GONE) { // if the user is executing one plan at this moment
                    showCancelTraining(new Runnable() {
                        @Override
                        public void run() {
                            handleStatsClick();
                        }
                    });
                    return;
                }
                handleStatsClick();
            }
        });
        getExercises(treinoExec.getId());

        deleteImageButton = view.findViewById(R.id.delete_image);
        buttonDelete = view.findViewById(R.id.buttondelete);


        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areAllExercisesCompleted()) {
                    try {
                        exec = databaseHelper.get_training_execs(treinoExec.getId()) + 1;
                    } catch (Exception e) {
                        exec = 1;

                    }
                    Toast.makeText(getContext(), "Treino concluído com sucesso!", Toast.LENGTH_SHORT).show();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String currentDate = sdf.format(new Date());
                    System.out.println(listofexecutionsseries.size());
                    for(SeriesInfo serie :listofexecutionsseries){
                        databaseHelper.insertSeries(serie.getPeso(),serie.getSeries(),serie.getExercicioId(),serie.getTreinoId(),serie.getExec(),serie.getOxigenacao(), serie.getBatimentos());
                        firebaseFirestorehelper.insertSeries(serie.getPeso(),serie.getSeries(),serie.getExercicioId(),serie.getTreinoId(),serie.getExec(),modelview.getUser().getValue().getId(),serie.getOxigenacao(), serie.getBatimentos());
                    }
                    databaseHelper.inserttreinodone(treinoExec.getId(), currentDate, exec,modelview.getUser().getValue().getId());
                    firebaseFirestorehelper.insertTreinoDone(treinoExec.getId(),currentDate,exec,modelview.getUser().getValue().getId());
                    handleHalterClick();
                } else {
                    Toast.makeText(getContext(), "Complete todos os exercícios antes de finalizar.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
                adapter.notifyDataSetChanged();            }
        });

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setEnabled(true);
                modelview.setExercises(treinosExec);
                handleEditClick();
                adapter.notifyDataSetChanged();
            }
        });

        startButton.setOnClickListener(v -> {
            // if there are no exercises in the list
            if (treinosExec.size() == 0) {
                Toast.makeText(getContext(), "Não há exercícios para este treino.", Toast.LENGTH_SHORT).show();

            }else {

                startButton.setVisibility(View.GONE);
                editImageButton.setVisibility(View.GONE);
                deleteImageButton.setVisibility(View.GONE);
                stopFinishLayout.setVisibility(View.VISIBLE);

                listView.setEnabled(true);
                stopFinishLayout.setVisibility(View.VISIBLE);
                startButton.setEnabled(false);
            }
        });

        helpbutton = view.findViewById(R.id.helpButton);
        helpbutton.setOnClickListener(v -> showHelpDialog());
        return view;
    }

    /**
     *  Method to display the cancel Training dialog
     */

    private void showCancelTraining(final Runnable onConfirmCancel) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_custom);


        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        Button confirmDeleteButton = dialog.findViewById(R.id.confirm_delete);
        Button cancelDeleteButton = dialog.findViewById(R.id.cancel_delete);
        TextView conf = dialog.findViewById(R.id.confirmation_message);
        conf.setText("Tem a certeza que deseja cancelar o treino?\n(Perderá todo o progresso)");


        confirmDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmCancel.run();
                dialog.dismiss();
            }
        });


        cancelDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
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
     * Switches the current fragment to the Edit fragment.
     */
    private void handleEditClick() {
        ((MainActivity) requireActivity()).switchtoEdit();
    }

    /**
     * Switches the current fragment to the Stats fragment.
     */
    private void handleStatsClick() {
        ((MainActivity) requireActivity()).switchtoStats();
    }


    /**
     *  Method to display the help dialog
     */

    private void showHelpDialog() {

        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.layou_help);


        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        Button helpOkButton = dialog.findViewById(R.id.helpOkButton);

        TextView helpMessage = dialog.findViewById(R.id.helpMessage);
        if(startButton.getVisibility() == View.VISIBLE) {
            helpMessage.setText("NOTA: \n\n1. Clique em Start para começar o seu treno.\n2. Clique em editar para ordenar/adicionar/apagar exercicios do plano de treino.\n");
        }else{
            helpMessage.setText("NOTA: \n\n1. Clique em finish para concluir o seu treino.\n2. Clique no exercicio e coloque o peso que fez durante a série\n");
        }

        helpOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    /**
     *  Method to display the delete confirmation dialog
     */

    private void showDeleteConfirmationDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_custom);


        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        Button confirmDeleteButton = dialog.findViewById(R.id.confirm_delete);
        Button cancelDeleteButton = dialog.findViewById(R.id.cancel_delete);



        confirmDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeleteTrainig(treinoExec.getId());
                firebaseFirestorehelper.deletePlanAndExercises(treinoExec.getId(), Objects.requireNonNull(modelview.getUser().getValue()).getId());
                dialog.dismiss();
            }
        });


        cancelDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }


    /**
     *  Method to display the weight input dialog
     * @param position - position of the exercise that the user clicked on
     */
    private void showWeightDialog(int position) {
        dialogWeight = new Dialog(requireContext());
        dialogWeight.setContentView(R.layout.dialog_weight);


        dialogWeight.getWindow().setBackgroundDrawableResource(android.R.color.transparent);



        EditText input = dialogWeight.findViewById(R.id.weightInput);
        LinearLayout text = dialogWeight.findViewById(R.id.linear);


        Button btnOk = dialogWeight.findViewById(R.id.btnOk);
        Button btnCancel = dialogWeight.findViewById(R.id.btnCancel);
        Button btnTriggerArduino = dialogWeight.findViewById(R.id.btnTriggerArduino);
        if(InternetOn) {
            btnTriggerArduino.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mqttHelper.publish("cmGymTrackerSend/" + Objects.requireNonNull(modelview.getUser().getValue()).getUsername(), "INFO"); // Publica no tópico
                    System.out.println("Mensagem enviada para o tópico cmGymTrackerSend/bruno" + ": " + "INFO");
                    btnTriggerArduino.setVisibility(View.INVISIBLE);

                }
            });
        }else{
            text.setVisibility(View.INVISIBLE);
        }


        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weight = input.getText().toString();
                if(!InternetOn){
                    if (!weight.isEmpty()){
                        try {
                            exec = databaseHelper.get_training_execs(treinoExec.getId()) + 1;
                        } catch (Exception e) {
                            exec = 1;

                        }
                        int series = treinosExec.get(position).getSeries();
                        treinosExec.get(position).setSeries(series - 1);
                        updateListView();
                        SeriesInfo seriesInfo = new SeriesInfo(Integer.parseInt(weight), series, treinosExec.get(position).getId_exercicio(), treinoExec.getId(), exec, oxigenacao, batimentos);
                        listofexecutionsseries.add(seriesInfo);
                        adapter.notifyDataSetChanged();
                        dialogWeight.dismiss();

                    }
                }else {

                    if (!weight.isEmpty() && btnTriggerArduino.getVisibility() != View.VISIBLE) {
                        try {
                            exec = databaseHelper.get_training_execs(treinoExec.getId()) + 1;
                        } catch (Exception e) {
                            exec = 1;
                        }
                        int series = treinosExec.get(position).getSeries();
                        treinosExec.get(position).setSeries(series - 1);
                        updateListView();

                        SeriesInfo seriesInfo = new SeriesInfo(Integer.parseInt(weight), series, treinosExec.get(position).getId_exercicio(), treinoExec.getId(), exec, oxigenacao, batimentos);
                        listofexecutionsseries.add(seriesInfo);
                        adapter.notifyDataSetChanged();
                        dialogWeight.dismiss();

                        Toast.makeText(getContext(), "Weight for " + treinosExec.get(position).getName() + " set to " + weight, Toast.LENGTH_SHORT).show();
                    } else if (weight.isEmpty()) {
                        Toast.makeText(getContext(), "Coloque o peso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Obtenha o rastreamento de saúde!", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogWeight.dismiss();
            }
        });
        dialogWeight.show();

    }

    /**
     * Method that deletes a training plan from the local database (using thread)
     * @param id - Plan ID
     */

    private void DeleteTrainig(Integer id) {

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
                databaseHelper.deleteTreinoPlanoById(id);
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(requireActivity(), "Treino apagado com sucesso!", Toast.LENGTH_SHORT).show();
                        handleHalterClick();
                    }
                });
            }
        });
    }


    /**
     * Connects to the MQTT broker and sets up message handling callbacks.
     */

    private void connectToMqtt() {
        new Thread(() -> {
            mqttHelper.connect(server, clientId, username, getContext(), new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    // Connection successful callback
                    Log.d("MQTT", "Connected to: " + serverURI);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    // Connection lost callback
                    Log.d("MQTT", "Connection lost: " + cause.getMessage());


                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Topic:" + topic);
                    // Handle incoming messages
                    String[] topicParts = topic.split("/");
                    if(topicParts[0].equals("cmGymTrackerReceive") && topicParts[1].equals(modelview.getUser().getValue().getUsername())){
                           String messageContent = message.toString();
                        String[] data = messageContent.split(",");

                        String heartbeat = data[0];
                        String oxygen = data[1];

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                oxygenInfo = dialogWeight.findViewById(R.id.oxygenInfo);
                                heartbeatInfo = dialogWeight.findViewById(R.id.heartbeatInfo);
                                batimentos = Integer.parseInt(heartbeat);
                                oxigenacao = Integer.parseInt(oxygen);


                                oxygenInfo.setText("Oxigenação: " + oxygen + " %");
                                heartbeatInfo.setText("Batimentos Cardíacos: " + heartbeat + " bpm");

                                oxygenInfo.setVisibility(View.VISIBLE);
                                heartbeatInfo.setVisibility(View.VISIBLE);
                                Toast.makeText(getContext(), "Dados de saúde recebidos!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }





                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Handle delivery confirmation
                    Log.d("MQTT", "Message delivered: " + token.getMessageId());
                }
            });

            requireActivity().runOnUiThread(this::subscribeToTopic);
        }).start();
    }



    /**
     * Method that subscribes the user to 2 topics. Topic that sends a request, and another that receives the response
     */
    private void subscribeToTopic(){
        String chatSend = "cmGymTrackerSend/"+ Objects.requireNonNull(modelview.getUser().getValue()).getUsername();
        System.out.println("Subscribing to topic " + chatSend);

        String chatReceive = "cmGymTrackerReceive/"+ Objects.requireNonNull(modelview.getUser().getValue()).getUsername();
        System.out.println("Subscribing to topic " + chatReceive);

        mqttHelper.subscribe(chatSend);
        mqttHelper.subscribe(chatReceive);

    }




}