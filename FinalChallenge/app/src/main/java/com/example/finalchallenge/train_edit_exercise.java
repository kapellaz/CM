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

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.Dialog;

import android.widget.Spinner;
import android.widget.TextView;

import com.example.finalchallenge.classes.Exercicio;
import com.example.finalchallenge.classes.Exercise;

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

    private FirebaseFirestorehelper firebaseFirestorehelper;
    public train_edit_exercise() {
        // Required empty public constructor
    }
    /**
     * Initializes the fragment and sets up essential components such as the ViewModel, firebase and database.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
        // Inicialize a lista de exercícios a partir do ViewModel
        databaseHelper = new DatabaseHelper(getContext());
        exerciseList = modelview.getExercises().getValue();
        treinoPlano = modelview.getSelectedPlan().getValue();
        firebaseFirestorehelper = new FirebaseFirestorehelper();
        exercicios = databaseHelper.getAllExercicios();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_train_edit_exercise, container, false);
        TextView exerciseName = view.findViewById(R.id.exerciseName);
        exerciseName.setText("Edição: " + Objects.requireNonNull(treinoPlano).getNome());


        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        adapter = new ExerciseAdapter(exerciseList);
        recyclerView.setAdapter(adapter);


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);


        // ItemTouchHelper Configuration

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();


                if (fromPosition == toPosition) {
                    return false;
                }

                Collections.swap(exerciseList, fromPosition, toPosition);


                modelview.getExercises().setValue(exerciseList);

                adapter.notifyItemMoved(fromPosition, toPosition);
                updateExerciseOrderInDB();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                databaseHelper.deleteExercicioFromPlano(treinoPlano.getId(), exerciseList.get(position),modelview.getUser().getValue().getId());
                firebaseFirestorehelper.deleteExercicioFromPlano(treinoPlano.getId(),exerciseList.get(position).getId(),modelview.getUser().getValue().getId());

                exerciseList.remove(position);
                modelview.getExercises().setValue(exerciseList);
                adapter.notifyItemRemoved(position);
            }
            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

        };



        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null) {
                        int position = recyclerView.getChildAdapterPosition(child);
                        showEditExerciseDialog(position);
                        return true;
                    }
                    return false;
                }
            });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return gestureDetector.onTouchEvent(e);
            }
        });



        addExerciseButton = view.findViewById(R.id.addExerciseButton);
        addExerciseButton.setOnClickListener(v -> showAddExerciseDialog());

        helpbutton = view.findViewById(R.id.helpButton);
        helpbutton.setOnClickListener(v -> showHelpDialog());

        logoutButton.setOnClickListener(v -> handleLogoutClick());
        halterButton.setOnClickListener(v -> handleHalterClick());
        perfilButton.setOnClickListener(v -> handlePerfilClick());
        statsButton.setOnClickListener(v -> handleStatsClick());

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
     *  Method to display the edit exercise dialog
     */
    private void showEditExerciseDialog(int position) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_edit_exercise);


        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        Exercise exercise = exerciseList.get(position);


        EditText inputRepetitions = dialog.findViewById(R.id.inputRepeticoes);
        EditText inputSeries = dialog.findViewById(R.id.inputSeries);

        Button create = dialog.findViewById(R.id.create_button);
        Button cancelar = dialog.findViewById(R.id.cancel_button);


        inputRepetitions.setText(String.valueOf(exercise.getRepetitions()));
        inputSeries.setText(String.valueOf(exercise.getSeries()));

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String repetitionsString = inputRepetitions.getText().toString();
                String seriesString = inputSeries.getText().toString();

                if (!repetitionsString.isEmpty() && !seriesString.isEmpty()) {
                    int newRepetitions = Integer.parseInt(repetitionsString);
                    int newSeries = Integer.parseInt(seriesString);


                    exercise.setRepetitions(newRepetitions);
                    exercise.setSeries(newSeries);

                    updateExerciseInfoInDB();
                    adapter.notifyItemChanged(position);
                }
                dialog.dismiss();
            }
        });


        dialog.show();
    }


    /**
     *  Method to display the add exercise dialog
     */
    private void showAddExerciseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Exercício");


        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_exercise, null);
        builder.setView(customLayout);

        final Spinner spinner = customLayout.findViewById(R.id.exerciseNameSpinner);

        ArrayAdapter<Exercicio> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, exercicios);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinner.setAdapter(adapter);
        final AlertDialog dialog3 = builder.create();
        Objects.requireNonNull(dialog3.getWindow()).setBackgroundDrawableResource(R.drawable.customdialog); // Aplica o fundo arredondado
        final String[] selected = {""};

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selected[0] = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
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
                int id_exe = 0;
                for(Exercicio i:exercicios){
                    if (Objects.equals(i.getNome(), nome)){
                        id_exe = i.getId();
                    }
                }
                addExercise(nome,id_exe, series, repetitions);
            }

        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    /**
     *  Method to display the help dialog
     */
    private void showHelpDialog() {

        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.layou_help);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        Button helpOkButton = dialog.findViewById(R.id.helpOkButton);



        helpOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    /**
     *   Method to add exercise to the list, and database call
     * @param exerciseName - Name of Exercise
     * @param id_exercicio - id of exercise
     * @param repetitions - repetitions info
     * @param series - series info
      */

    private void addExercise(String exerciseName, int id_exercicio,int series, int repetitions) {

        int newId = exerciseList.size() + 1;
        Exercise newExercise = new Exercise(newId, id_exercicio,exerciseName, series, repetitions,exerciseList.size() + 1);
        exerciseList.add(newExercise);
        modelview.getExercises().setValue(exerciseList);
        addExerciseInPlanIntoDB(exerciseName,series,repetitions,exerciseList.size());
        adapter.notifyItemInserted(exerciseList.size() - 1);

    }

    /**
     * Adapter for RecyclerView
      */
    private static class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
        private List<Exercise> exerciseList;


        public ExerciseAdapter(List<Exercise> exerciseList) {
            this.exerciseList = exerciseList;
        }

        @Override
        public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_edit, parent, false);
            return new ExerciseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ExerciseViewHolder holder, int position) {

            Exercise exercise = exerciseList.get(position);

            holder.exerciseName.setText(exercise.getName());
            holder.exerciseDetails.setText("Séries: " + exercise.getSeries() + ", Repetições: " + exercise.getRepetitions());
        }

        @Override
        public int getItemCount() {
            return exerciseList.size();
        }


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

    /**
     * Method that add an exercise in Plan into firebase and database local.
     */
    public void addExerciseInPlanIntoDB(String name, int series, int rep,int order){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int id = databaseHelper.getExerciseIdByName(name);
                long t = databaseHelper.insertExercicioFromPlano(treinoPlano.getId(),id,series,rep,order);
                firebaseFirestorehelper.insertExercicioFromPlano(treinoPlano.getId(),id,series,rep,order,modelview.getUser().getValue().getId(),(int) t);
            }
        });
    }

    /**
     * Method that update all exercises info (order)
     */
    private void updateExerciseOrderInDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {

                int treinoId = treinoPlano.getId();
                databaseHelper.updateExerciseOrderInPlan(treinoId, exerciseList);
                firebaseFirestorehelper.updateExerciseOrdersInPlan(treinoPlano.getId(),exerciseList);
            }
        });
    }

    /**
      * Method that update all exercises info (reps and sets)
     */
    private void updateExerciseInfoInDB() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        firebaseFirestorehelper.updateExerciseDetailsInPlan(treinoPlano.getId(),exerciseList);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                int treinoId = treinoPlano.getId();
                databaseHelper.updateExerciseDetailsInPlan(treinoId, exerciseList);
            }
        });
    }

}
