package com.example.finalchallenge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.finalchallenge.classes.Execution;
import com.example.finalchallenge.classes.viewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class exercise_detail extends Fragment {

    private Spinner exerciseSpinner;
    private LineChart lineChart;
    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;

    private DatabaseHelper databaseHelper;
    private List<Execution> executions;
    private Map<String, List<Integer>> execucoesPorDia;
    private viewModel modelview;
    public exercise_detail() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());

        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);

        // Obtém o mapa com as execuções agrupadas por data (de pesos por dia)
        execucoesPorDia = databaseHelper.getExecucoesPorExercicio(
                Objects.requireNonNull(modelview.getUser().getValue()).getId(),
                modelview.getExercicio().getValue().getId()
        );
        //executions = databaseHelper.getExerciseExecutionsOverTime(Objects.requireNonNull(modelview.getExercicio().getValue()).getId(), Objects.requireNonNull(modelview.getUser().getValue()).getId());

        // Localiza o Spinner
        exerciseSpinner = view.findViewById(R.id.spinner);
        lineChart = view.findViewById(R.id.activity_main_linechart);
        // Inicializa os botões
        logoutButton = view.findViewById(R.id.logout);
        halterButton = view.findViewById(R.id.halter);
        perfilButton = view.findViewById(R.id.perfil);
        statsButton = view.findViewById(R.id.stats);
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
        // Dados para exibir no Spinner
        List<String> exerciseOptions = new ArrayList<>();
        exerciseOptions.add("Média de Pesos");
        exerciseOptions.add("Peso Máximo");


        // Configura o Adapter para o Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item, // Layout padrão para itens no Spinner
                exerciseOptions // Lista de dados
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        exerciseSpinner.setAdapter(adapter);
        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    // Exibe a média de pesos no gráfico
                    showWeightAverageGraph();
                } else if (position == 1) {
                    // Exibe o peso máximo no gráfico
                    showMaxWeightGraph();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Nenhuma seleção
            }
        });

    }




    private void showWeightAverageGraph() {

        if (execucoesPorDia.isEmpty()) {
            return;
        }


        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();


        Map<String, List<Integer>> groupedExecucoes = new HashMap<>();

        for (Map.Entry<String, List<Integer>> entry : execucoesPorDia.entrySet()) {
            String day = extractDate(entry.getKey());  // Extrai a data da chave
            List<Integer> pesos = entry.getValue();  // Lista de pesos associados à data

            // Se a data já existir no mapa agrupado, adiciona os pesos
            if (groupedExecucoes.containsKey(day)) {
                groupedExecucoes.get(day).addAll(pesos);
            } else {
                // Caso contrário, cria uma nova lista para a data
                groupedExecucoes.put(day, new ArrayList<>(pesos));
            }
        }


        Map<String, List<Integer>> sortedGroupedExecucoes = new TreeMap<>(groupedExecucoes);


        int dayIndex = 0;
        for (Map.Entry<String, List<Integer>> entry : sortedGroupedExecucoes.entrySet()) {
            String day = entry.getKey();
            List<Integer> pesos = entry.getValue();


            float totalWeight = 0;
            for (Integer peso : pesos) {
                totalWeight += peso;
            }

            float averageWeight = totalWeight / pesos.size();

            entries.add(new Entry(dayIndex, averageWeight));
            dayIndex++;
            dates.add(day);
        }


        LineDataSet dataSet = new LineDataSet(entries, "Média de Pesos");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[3]);
        LineData lineData = new LineData(dataSet);


        lineChart.setData(lineData);
        lineChart.invalidate();


        lineChart.getDescription().setText("Evolução do Peso ao Longo do Tempo");
        lineChart.getDescription().setTextSize(12f);


        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {

                int index = (int) value;
                if (index < dates.size() && index >= 0) {
                    return dates.get(index);
                }
                return "";
            }
        });


        lineChart.getXAxis().setGranularity(1f);
    }



    private void showMaxWeightGraph() {

        if (execucoesPorDia.isEmpty()) {
            return;
        }


        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();


        Map<String, List<Integer>> groupedExecucoes = new HashMap<>();

        for (Map.Entry<String, List<Integer>> entry : execucoesPorDia.entrySet()) {
            String day = extractDate(entry.getKey());
            List<Integer> pesos = entry.getValue();

            // Se a data já existir no mapa agrupado, adiciona os pesos
            if (groupedExecucoes.containsKey(day)) {
                groupedExecucoes.get(day).addAll(pesos);
            } else {
                // Caso contrário, cria uma nova lista para a data
                groupedExecucoes.put(day, new ArrayList<>(pesos));
            }
        }

        Map<String, List<Integer>> sortedGroupedExecucoes = new TreeMap<>(groupedExecucoes);


        int dayIndex = 0;
        for (Map.Entry<String, List<Integer>> entry : sortedGroupedExecucoes.entrySet()) {
            String day = entry.getKey();  // Data
            List<Integer> pesos = entry.getValue();
            int maiorNumero = Collections.max(pesos);

            entries.add(new Entry(dayIndex, maiorNumero));
            dayIndex++;
            dates.add(day);
        }


        LineDataSet dataSet = new LineDataSet(entries, "Peso Máximo");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[2]);
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[3]);
        LineData lineData = new LineData(dataSet);


        lineChart.setData(lineData);
        lineChart.invalidate();


        lineChart.getDescription().setText("Evolução do Peso ao Longo do Tempo");
        lineChart.getDescription().setTextSize(12f);


        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Converter o valor do eixo X (índice do dia) para a data correspondente
                int index = (int) value;  // Índice do dia
                if (index < dates.size() && index >= 0) {
                    return dates.get(index);  // Retorna a data correspondente ao índice
                }
                return "";
            }
        });


        lineChart.getXAxis().setGranularity(1f);
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

    private void handleItemClick(String item) {
        ((MainActivity) requireActivity()).switchDetailsTrain();
    }

    private String extractDate(String fullDate) {
        try {
            // Define o formato da data original
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Converte a string para um objeto Date
            Date date = inputFormat.parse(fullDate);

            // Define o formato para a data desejada (sem horas)
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

            // Retorna a data formatada
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";  // Retorna uma string vazia em caso de erro
        }
    }


}
