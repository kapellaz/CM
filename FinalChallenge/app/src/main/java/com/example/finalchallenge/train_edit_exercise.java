package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.finalchallenge.classes.Exercicio;
import com.example.finalchallenge.classes.Exercise;
import com.example.finalchallenge.classes.TreinoExec;
import com.example.finalchallenge.classes.TreinoPlano;
import com.example.finalchallenge.classes.viewModel;
public class train_edit_exercise extends Fragment {
    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;
    private ImageButton helpbutton;


    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;
    private List<Exercise> exerciseList;
    private viewModel modelview;
    private ImageButton addExerciseButton;
    private List<Exercicio> exercicios;
    private DatabaseHelper databaseHelper;
    private TreinoPlano treinoPlano;
    public train_edit_exercise() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
        // Inicialize a lista de exercícios a partir do ViewModel
        databaseHelper = new DatabaseHelper(getContext());
        exerciseList = modelview.getExercises().getValue();
        treinoPlano = modelview.getSelectedPlan().getValue();
        exercicios = databaseHelper.getAllExercicios();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar o layout para o fragment
        View view = inflater.inflate(R.layout.fragment_train_edit_exercise, container, false);
        TextView exerciseName = view.findViewById(R.id.exerciseName);
        exerciseName.setText("Edição: " + Objects.requireNonNull(treinoPlano).getNome());

        // Inicializa os botões
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);
        // Inicializando o RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configurar o Adapter
        adapter = new ExerciseAdapter(exerciseList);
        recyclerView.setAdapter(adapter);

        // Adicionar ItemDecoration (opcional)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configuração do ItemTouchHelper
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Evitar mover para a mesma posição
                if (fromPosition == toPosition) {
                    return false;
                }

                // Trocar os itens na lista
                Collections.swap(exerciseList, fromPosition, toPosition);

                // Atualizar a lista no ViewModel
                modelview.getExercises().setValue(exerciseList);

                adapter.notifyItemMoved(fromPosition, toPosition);
                updateExerciseOrderInDB();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // Remover o exercício da lista e do banco de dados
                databaseHelper.deleteExercicioFromPlano(treinoPlano.getId(), exerciseList.get(position).getId());
                exerciseList.remove(position);

                // Atualizar a lista no ViewModel
                modelview.getExercises().setValue(exerciseList);

                // Notificar o adaptador sobre a remoção do item
                adapter.notifyItemRemoved(position);
            }

        };

// Vincular o ItemTouchHelper ao RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        // Configurar o botão de adicionar exercício
        addExerciseButton = view.findViewById(R.id.addExerciseButton);
        addExerciseButton.setOnClickListener(v -> showAddExerciseDialog());

        // Configurar o botão de adicionar exercício
        helpbutton = view.findViewById(R.id.helpButton);
        helpbutton.setOnClickListener(v -> showHelpDialog());

        logoutButton.setOnClickListener(v -> handleLogoutClick());
        halterButton.setOnClickListener(v -> handleHalterClick());
        perfilButton.setOnClickListener(v -> handlePerfilClick());
        statsButton.setOnClickListener(v -> handleStatsClick());

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

    // Método para exibir o diálogo de adicionar exercício
    private void showAddExerciseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Exercício");

        // Layout para adicionar um exercício
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_exercise, null);
        builder.setView(customLayout);

        final Spinner spinner = customLayout.findViewById(R.id.exerciseNameSpinner);
        // Criar o ArrayAdapter
        ArrayAdapter<Exercicio> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, exercicios);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Configurar o Spinner com o adaptador
        spinner.setAdapter(adapter);
        final AlertDialog dialog3 = builder.create();
        Objects.requireNonNull(dialog3.getWindow()).setBackgroundDrawableResource(R.drawable.customdialog); // Aplica o fundo arredondado
        final String[] selected = {""};

        // Opcional: lidar com a seleção do item
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selected[0] = parentView.getItemAtPosition(position).toString();
                // Faça algo com o nome do exercício selecionado
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Não faça nada se nada for selecionado
            }
        });
        final EditText inputSeries = customLayout.findViewById(R.id.seriesEditText);
        final EditText inputRepetitions = customLayout.findViewById(R.id.repetitionsEditText);

        builder.setPositiveButton("Adicionar", (dialog, which) -> {

            String nome = selected[0];
            String seriesString = inputSeries.getText().toString();
            String repetitionsString = inputRepetitions.getText().toString();
            if (!nome.isEmpty() && !seriesString.isEmpty() && !repetitionsString.isEmpty()) {
                int series = Integer.parseInt(seriesString);
                int repetitions = Integer.parseInt(repetitionsString);
                addExercise(nome, series, repetitions);
            }

        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void showHelpDialog() {

        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.layou_help);

        // Personaliza o fundo do diálogo (usando o drawable com bordas arredondadas)
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        Button helpOkButton = dialog.findViewById(R.id.helpOkButton);



        // Configurar o botão "Cancelar"
        helpOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Fecha o diálogo
            }
        });

        dialog.show();

    }



    // Método para adicionar exercício à lista
    private void addExercise(String exerciseName, int series, int repetitions) {
        // Adicionando um novo exercício com um ID gerado
        int newId = exerciseList.size() + 1; // Exemplo de ID simples
        Exercise newExercise = new Exercise(newId, exerciseName, series, repetitions,exerciseList.size() + 1);
        exerciseList.add(newExercise);

        // Atualizar a lista no ViewModel
        modelview.getExercises().setValue(exerciseList);

        // Notificar o adaptador para inserir o novo item
        adapter.notifyItemInserted(exerciseList.size() - 1);
        addExerciseInPlanIntoDB(exerciseName,series,repetitions,exerciseList.size());
    }

    // Adapter para o RecyclerView
    private static class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
        private List<Exercise> exerciseList;

        public ExerciseAdapter(List<Exercise> exerciseList) {
            this.exerciseList = exerciseList;
        }

        @Override
        public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflar o layout personalizado
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_edit, parent, false);
            return new ExerciseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ExerciseViewHolder holder, int position) {
            // Obter o exercício na posição
            Exercise exercise = exerciseList.get(position);

            // Configurar os TextViews com os dados do exercício
            holder.exerciseName.setText(exercise.getName());
            holder.exerciseDetails.setText("Séries: " + exercise.getSeries() + ", Repetições: " + exercise.getRepetitions());
        }

        @Override
        public int getItemCount() {
            return exerciseList.size();
        }

        // ViewHolder para o item de exercício
        public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
            public TextView exerciseName;
            public TextView exerciseDetails;

            public ExerciseViewHolder(View itemView) {
                super(itemView);
                exerciseName = itemView.findViewById(R.id.exercise_name);
                exerciseDetails = itemView.findViewById(R.id.exercise_details);
            }
        }


    }
    public void addExerciseInPlanIntoDB(String name, int series, int rep,int order){
        ExecutorService executor = Executors.newSingleThreadExecutor();


        // Executa a tarefa de busca dos treinos em segundo plano
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Chama o método do databaseHelper para pegar os treinos
                int id = databaseHelper.getExerciseIdByName(name);
                databaseHelper.insertExercicioFromPlano(treinoPlano.getId(),id,series,rep,order);


            }
        });
    }

    private void updateExerciseOrderInDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();


        // Executa a tarefa de busca dos treinos em segundo plano
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Obtém o id do treino atual
                int treinoId = treinoPlano.getId();
                System.out.println("A ATUALIZAR ORDERS");
                System.out.println(exerciseList);

                // Atualiza a ordem dos exercícios no banco de dados
                databaseHelper.updateExerciseOrderInPlan(treinoId, exerciseList);
            }
        });
    }

}
