package com.example.finalchallenge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputType;
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
import com.example.finalchallenge.classes.TreinoExec;
import com.example.finalchallenge.classes.TreinoPlano;
import com.example.finalchallenge.classes.viewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class train_detail extends Fragment {
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

        treinoExec = modelview.getSelectedPlan().getValue();

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
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_exercise_start, parent, false);
                }

                // Obtém o ícone de check e o nome do exercício
                ImageView checkMarkIcon = convertView.findViewById(R.id.checkMarkIcon);
                TextView exerciseName = convertView.findViewById(R.id.exerciseName);

                // Obtém o exercício correspondente
                Exercise exercise = treinosExec.get(position);

                // Configura o nome do exercício
                exerciseName.setText(exercise.toString());

                // Se o exercício foi completado (séries = 0), exibe o ícone de check
                if (exercise.getSeries() == 0) {
                    checkMarkIcon.setVisibility(View.VISIBLE);  // Exibe o ícone de check
                } else {
                    checkMarkIcon.setVisibility(View.GONE);  // Esconde o ícone de check
                }

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


        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);

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

        System.out.println(treinoExec.getId());
        getExercises(treinoExec.getId());

        deleteImageButton = view.findViewById(R.id.delete_image);
        buttonDelete = view.findViewById(R.id.buttondelete);

        startButton = view.findViewById(R.id.startbutton);

        editImageButton = view.findViewById(R.id.editButton);
        stopFinishLayout = view.findViewById(R.id.linearLayoutStopFinish);

        finishButton = view.findViewById(R.id.finish);





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
                    databaseHelper.inserttreinodone(treinoExec.getId(), currentDate, exec,modelview.getUser().getValue().getId());
                } else {
                    Toast.makeText(getContext(), "Complete todos os exercícios antes de finalizar.", Toast.LENGTH_SHORT).show();
                }
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

        });

        helpbutton = view.findViewById(R.id.helpButton);
        helpbutton.setOnClickListener(v -> showHelpDialog());



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
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_weight);

        // Personaliza o fundo do diálogo (usando o drawable com bordas arredondadas)
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        // Referência ao EditText para captura do peso
        EditText input = dialog.findViewById(R.id.weightInput);

        // Botões OK e Cancel
        Button btnOk = dialog.findViewById(R.id.btnOk);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);


        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weight = input.getText().toString();
                if (!weight.isEmpty()) {
                    // Atualiza a quantidade de séries
                    try {
                        exec = databaseHelper.get_training_execs(treinoExec.getId()) + 1;
                    } catch (Exception e) {
                        exec = 1;
                        System.out.println("Erro");
                    }
                    int series = treinosExec.get(position).getSeries();
                    treinosExec.get(position).setSeries(series - 1);
                    updateListView(treinosExec); // Atualiza a lista
                    databaseHelper.insertSeries(Integer.parseInt(weight), series,treinosExec.get(position).getId(), treinoExec.getId(), exec);

                    adapter.notifyDataSetChanged(); // Notifica o adaptador de que os dados mudaram
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Weight for " + treinosExec.get(position).getName() + " set to " + weight, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Ação para cancelar a exclusão
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();  // Fecha o diálogo
            }
        });
        dialog.show();

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


}