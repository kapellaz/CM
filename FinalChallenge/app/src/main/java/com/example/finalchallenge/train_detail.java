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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
        databaseHelper = new DatabaseHelper(getContext());
        firebaseFirestorehelper = new FirebaseFirestorehelper();

        treinoExec = modelview.getSelectedPlan().getValue();
        mqttHelper = new MQTThelper();
        clientId = modelview.getUser().getValue().getUsername();

        // Verificar a conexão com a Internet
        InternetOn = isNetworkConnected();


        connectToMqtt();

    }

    private boolean isNetworkConnected() {
        // Obtenção do ConnectivityManager com o Context correto
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            // Obter a informação da rede ativa
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }


    private void getExercises(Integer id) {
        // Cria um ExecutorService para rodar a consulta em uma thread separada
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
                // Chama o método do databaseHelper para pegar os treinos
                List<Exercise> treinos = databaseHelper.getExercisesForTraining(id);
                System.out.println(treinos);
                // Atualiza a UI com os dados (precisa rodar na thread principal)
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (treinos != null) {
                            treinosExec = treinos;
                            updateListView(treinos); // Atualiza o ListView
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

    private ArrayAdapter<Exercise> createAdapter() {
        return new ArrayAdapter<Exercise>(requireActivity(), R.layout.item_exercise_start, R.id.exerciseName, treinosExec) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Verifica se a view pode ser reutilizada
                if (convertView == null) {
                    // Infla o layout se não for possível reutilizar a view
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_exercise_start, parent, false);
                }

                // Obtém as referências dos elementos da view
                ImageView checkMarkIcon = convertView.findViewById(R.id.checkMarkIcon);
                TextView exerciseName = convertView.findViewById(R.id.exerciseName);

                // Obtém o exercício correspondente
                Exercise exercise = treinosExec.get(position);

                // Configura o nome do exercício
                exerciseName.setText(exercise.toString());

                // Verifica se o exercício foi completado (séries = 0)
                if (exercise.getSeries() == 0) {
                    // Se o exercício foi completado, exibe o ícone de check
                    checkMarkIcon.setVisibility(View.VISIBLE);  // Exibe o ícone de check
                } else {
                    // Se não foi completado, esconde o ícone de check
                    checkMarkIcon.setVisibility(View.GONE);  // Esconde o ícone de check
                }

                // Retorna a view configurada
                return convertView;
            }
        };
    }



    private void updateListView(List<Exercise> treinos) {
        // Atualiza o ListView com os dados obtidos
        ListView listView = getView().findViewById(R.id.listView);
        ArrayAdapter<Exercise> adapter = createAdapter();
        listView.setAdapter(adapter);



    }

    private boolean areAllExercisesCompleted() {
        for (Exercise exercise : treinosExec) {
            if (exercise.getSeries() != 0) {
                return false;
            }
        }
        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_train_detail, container, false);
        TextView trainingName = view.findViewById(R.id.exerciseName);
        trainingName.setText(Objects.requireNonNull(modelview.getSelectedPlan().getValue()).getNome());
        // Inicializando o ListView
        ListView listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
      // Criando o Adapter para a lista (pode ser um ArrayAdapter ou CustomAdapter)
        // Inicializando o adapter
        adapter = createAdapter();


        // Definindo o Adapter para o ListView
        listView.setAdapter(adapter);
        listView.setEnabled(false);
        // Adicionar evento de clique para cada item da lista
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            if(treinosExec.get(position).getSeries() != 0) {
                showWeightDialog(position); // Mostrar o diálogo para inserir o peso
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

        // Configura os listeners para os botões
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getVisibility()==View.GONE) {
                    // Passa a função de logout para o showCancelTraining
                    showCancelTraining(new Runnable() {
                        @Override
                        public void run() {
                            // Lógica a ser executada quando o usuário confirmar o cancelamento do treino
                            handleLogoutClick();  // Chama a função de logout
                        }
                    });
                    return;
                }

                // Caso não esteja visível o finishButton, executa diretamente o logout
                handleLogoutClick();
            }
        });

        halterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getVisibility()==View.GONE) {
                    // Passa a função de logout para o showCancelTraining
                    showCancelTraining(new Runnable() {
                        @Override
                        public void run() {
                            // Lógica a ser executada quando o usuário confirmar o cancelamento do treino
                            handleHalterClick();  // Chama a função de logout
                        }
                    });
                    return;
                }

                // Caso não esteja visível o finishButton, executa diretamente o logout
                handleHalterClick();
            }
        });

        perfilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButton.getVisibility()==View.GONE) {
                    // Passa a função de logout para o showCancelTraining
                    showCancelTraining(new Runnable() {
                        @Override
                        public void run() {
                            // Lógica a ser executada quando o usuário confirmar o cancelamento do treino
                            handlePerfilClick();  // Chama a função de logout
                        }
                    });
                    return;
                }

                // Caso não esteja visível o finishButton, executa diretamente o logout
                handlePerfilClick();
            }
        });

        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  if (startButton.getVisibility()==View.GONE) {
                    // Passa a função de logout para o showCancelTraining
                    showCancelTraining(new Runnable() {
                        @Override
                        public void run() {
                            // Lógica a ser executada quando o usuário confirmar o cancelamento do treino
                            handleLogoutClick();  // Chama a função de logout
                        }
                    });
                    return;
                }

                // Caso não esteja visível o finishButton, executa diretamente o logout
                handleStatsClick();
            }
        });
        System.out.println(treinoExec.getId());
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
                        System.out.println("Erro");
                    }
                    Toast.makeText(getContext(), "Treino concluído com sucesso!", Toast.LENGTH_SHORT).show();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String currentDate = sdf.format(new Date());
                    System.out.println(listofexecutionsseries.size());
                    for(SeriesInfo serie :listofexecutionsseries){
                        System.out.println("ENTROUUU" + serie.getExercicioId() + " " + serie.getTreinoId());
                        databaseHelper.insertSeries(serie.getPeso(),serie.getSeries(),serie.getExercicioId(),serie.getTreinoId(),serie.getExec(),serie.getOxigenacao(), serie.getBatimentos());
                        firebaseFirestorehelper.insertSeries(serie.getPeso(),serie.getSeries(),serie.getExercicioId(),serie.getTreinoId(),serie.getExec(),modelview.getUser().getValue().getId(),serie.getOxigenacao(), serie.getBatimentos());
                    }

                    databaseHelper.inserttreinodone(treinoExec.getId(), currentDate, exec,modelview.getUser().getValue().getId());
                    firebaseFirestorehelper.insertTreinoDone(treinoExec.getId(),currentDate,exec,modelview.getUser().getValue().getId());
                    handleHalterClick();
                } else {
                    Toast.makeText(getContext(), "Complete todos os exercícios antes de finalizar.", Toast.LENGTH_SHORT).show();
                }
                //after the finish click redirect to menu_principal

            }
        });

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println();
                // Exibir o alerta de confirmação ao clicar no botão
                showDeleteConfirmationDialog();
                adapter.notifyDataSetChanged();            }
        });

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setEnabled(true);
                // Exibir o alerta de confirmação ao clicar no botão
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

                startButton.setVisibility(View.GONE);  // Esconde o Start
                editImageButton.setVisibility(View.GONE);   // Esconde o Edit
                deleteImageButton.setVisibility(View.GONE); // Esconde o Delete
                stopFinishLayout.setVisibility(View.VISIBLE); // Exibe o Stop e Finish

                // Habilitar a interação com a lista
                listView.setEnabled(true);
                // Mostrar os botões Stop e Finish
                stopFinishLayout.setVisibility(View.VISIBLE);
                // Desabilitar o botão Start
                startButton.setEnabled(false);
            }
        });

        helpbutton = view.findViewById(R.id.helpButton);
        helpbutton.setOnClickListener(v -> showHelpDialog());



        return view;
    }


    private void showCancelTraining(final Runnable onConfirmCancel) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_custom);

        // Personaliza o fundo do diálogo (usando o drawable com bordas arredondadas)
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Define os botões do diálogo
        Button confirmDeleteButton = dialog.findViewById(R.id.confirm_delete);
        Button cancelDeleteButton = dialog.findViewById(R.id.cancel_delete);
        TextView conf = dialog.findViewById(R.id.confirmation_message);
        conf.setText("Tem a certeza que deseja cancelar o treino?\n(Perderá todo o progresso)");

        // Ação para confirmar a exclusão
        confirmDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para deletar o item
                onConfirmCancel.run();
                dialog.dismiss();  // Fecha o diálogo
            }
        });

        // Ação para cancelar a exclusão
        cancelDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();  // Fecha o diálogo
            }
        });

        // Exibe o diálogo
        dialog.show();
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

    private void handleEditClick() {
        ((MainActivity) requireActivity()).switchtoEdit();
    }

    private void handleStatsClick() {
        ((MainActivity) requireActivity()).switchtoStats();
    }

    private void handleItemClick(String item) {
        ((MainActivity) requireActivity()).switchDetailsTrain();
    }

    private void showHelpDialog() {

        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.layou_help);

        // Personaliza o fundo do diálogo (usando o drawable com bordas arredondadas)
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        Button helpOkButton = dialog.findViewById(R.id.helpOkButton);

        TextView helpMessage = dialog.findViewById(R.id.helpMessage);
        if(startButton.getVisibility() == View.VISIBLE) {
            helpMessage.setText("NOTA: \n\n1. Clique em Start para começar o seu treno.\n2. Clique em editar para ordenar/adicionar/apagar exercicios do plano de treino.\n");
        }else{
            helpMessage.setText("NOTA: \n\n1. Clique em finish para concluir o seu treino.\n2. Clique no exercicio e coloque o peso que fez durante a série\n");
        }
        // Configurar o botão "Cancelar"
        helpOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Fecha o diálogo
            }
        });

        dialog.show();

    }


    private void showDeleteConfirmationDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_custom);

        // Personaliza o fundo do diálogo (usando o drawable com bordas arredondadas)
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Define os botões do diálogo
        Button confirmDeleteButton = dialog.findViewById(R.id.confirm_delete);
        Button cancelDeleteButton = dialog.findViewById(R.id.cancel_delete);


        // Ação para confirmar a exclusão
        confirmDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para deletar o item
                DeleteTrainig(treinoExec.getId());
                firebaseFirestorehelper.deletePlanAndExercises(treinoExec.getId(), Objects.requireNonNull(modelview.getUser().getValue()).getId());
                dialog.dismiss();  // Fecha o diálogo
            }
        });

        // Ação para cancelar a exclusão
        cancelDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();  // Fecha o diálogo
            }
        });

        // Exibe o diálogo
        dialog.show();
    }


    // Método para exibir o dialog de inserir peso
    private void showWeightDialog(int position) {
        dialogWeight = new Dialog(requireContext());
        dialogWeight.setContentView(R.layout.dialog_weight);

        // Personaliza o fundo do diálogo (usando o drawable com bordas arredondadas)
        dialogWeight.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        // Referência ao EditText para captura do peso
        EditText input = dialogWeight.findViewById(R.id.weightInput);
        LinearLayout text = dialogWeight.findViewById(R.id.linear);

        // Botões OK e Cancel
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
                        updateListView(treinosExec); // Atualiza a lista
                        System.out.println(treinosExec.get(position).getId_exercicio() + " e o plano id é " + treinoExec.getId());
                        SeriesInfo seriesInfo = new SeriesInfo(Integer.parseInt(weight), series, treinosExec.get(position).getId_exercicio(), treinoExec.getId(), exec, oxigenacao, batimentos);
                        listofexecutionsseries.add(seriesInfo);
                        adapter.notifyDataSetChanged(); // Notifica o adaptador de que os dados mudaram
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
                        updateListView(treinosExec); // Atualiza a lista
                        System.out.println(treinosExec.get(position).getId() + " e o plano id é " + treinoExec.getId());
                        SeriesInfo seriesInfo = new SeriesInfo(Integer.parseInt(weight), series, treinosExec.get(position).getId_exercicio(), treinoExec.getId(), exec, oxigenacao, batimentos);
                        listofexecutionsseries.add(seriesInfo);
                        adapter.notifyDataSetChanged(); // Notifica o adaptador de que os dados mudaram
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

        // Ação para cancelar a exclusão
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogWeight.dismiss();  // Fecha o diálogo
            }
        });
        dialogWeight.show();

    }

    private void DeleteTrainig(Integer id) {
        // Cria um ExecutorService para rodar a consulta em uma thread separada
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
                // Chama o método do databaseHelper para pegar os treinos
                databaseHelper.deleteTreinoPlanoById(id);

                // Atualiza a UI com os dados (precisa rodar na thread principal)
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Esconde o ProgressBar quando os dados forem carregados
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        // Exibe um Toast informando que o treino foi apagado
                        Toast.makeText(requireActivity(), "Treino apagado com sucesso!", Toast.LENGTH_SHORT).show();

                        handleHalterClick();
                    }
                });
            }
        });
    }






    private void connectToMqtt() {
        new Thread(() -> {

            // Set the callback inside the fragment itself
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
                        // Agora, você pode processar a mensagem e atualizar a UI
                        // Vamos supor que a mensagem seja uma string com valores separados por vírgulas
                        String messageContent = message.toString();
                        String[] data = messageContent.split(",");

                        String heartbeat = data[0]; // Exemplo de como você pode tratar os dados recebidos
                        String oxygen = data[1];


                        // Agora você quer atualizar a UI com esses dados, mas como estamos em uma thread diferente,
                        // precisamos usar runOnUiThread para fazer isso na thread principal.

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                oxygenInfo = dialogWeight.findViewById(R.id.oxygenInfo);
                                heartbeatInfo = dialogWeight.findViewById(R.id.heartbeatInfo);
                                batimentos = Integer.parseInt(heartbeat);
                                oxigenacao = Integer.parseInt(oxygen);
                                // Atualizar os TextViews com os dados recebidos

                                oxygenInfo.setText("Oxigenação: " + oxygen + " %");
                                heartbeatInfo.setText("Batimentos Cardíacos: " + heartbeat + " bpm");

                                // Tornar os TextViews visíveis

                                oxygenInfo.setVisibility(View.VISIBLE);
                                heartbeatInfo.setVisibility(View.VISIBLE);

                                // Opcionalmente, você pode mostrar um Toast para confirmar que os dados foram recebidos
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

    private void subscribeToTopic(){
        String chatSend = "cmGymTrackerSend/"+ Objects.requireNonNull(modelview.getUser().getValue()).getUsername();
        System.out.println("Subscribing to topic " + chatSend);

        String chatReceive = "cmGymTrackerReceive/"+ Objects.requireNonNull(modelview.getUser().getValue()).getUsername();
        System.out.println("Subscribing to topic " + chatReceive);

        mqttHelper.subscribe(chatSend);
        mqttHelper.subscribe(chatReceive);

    }




}