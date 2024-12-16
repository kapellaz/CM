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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class exercise_detail extends Fragment {

    private Spinner exerciseSpinner;
    private LineChart lineChart;
    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;
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




    // Exemplo de método para exibir gráfico de média de pesos
    private void showWeightAverageGraph() {
        // Dados fictícios para gráfico (média de pesos)
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 50));  // Dia 0, Peso 50kg
        entries.add(new Entry(1, 52));  // Dia 1, Peso 52kg
        entries.add(new Entry(2, 51));  // Dia 2, Peso 51kg
        entries.add(new Entry(3, 53));  // Dia 3, Peso 53kg
        entries.add(new Entry(4, 54));  // Dia 4, Peso 54kg
        entries.add(new Entry(5, 55));  // Dia 5, Peso 55kg


        LineDataSet dataSet = new LineDataSet(entries, "Média de Pesos");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor do gráfico
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[3]);  // Cor do texto dos valores
        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.invalidate();
        // Personalização do gráfico: adicionar título e eixos
        lineChart.getDescription().setText("Evolução do Peso ao Longo do Tempo");
        lineChart.getDescription().setTextSize(12f);

        // Personalizar os eixos
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Converte o valor do eixo X (tempo) para formato de dia ou tempo adequado
                return String.valueOf((int) value); // Remove os decimais, apenas números inteiros
            }
        });
        // Garantir que o eixo X tenha um passo de 1 para garantir números inteiros
        lineChart.getXAxis().setGranularity(1f); // A granularidade ajuda a evitar valores decimais

    }

    // Exemplo de método para exibir gráfico de peso máximo
    private void showMaxWeightGraph() {
        // Dados fictícios para gráfico (peso máximo)
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 100));  // Dia 0, Peso máximo 100kg
        entries.add(new Entry(1, 110));  // Dia 1, Peso máximo 110kg
        entries.add(new Entry(2, 120));  // Dia 2, Peso máximo 120kg
        entries.add(new Entry(3, 130));  // Dia 3, Peso máximo 130kg
        entries.add(new Entry(4, 140));  // Dia 4, Peso máximo 140kg
        entries.add(new Entry(5, 150));  // Dia 5, Peso máximo 150kg

        LineDataSet dataSet = new LineDataSet(entries, "Peso Máximo");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor do gráfico
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[3]);  // Cor do texto dos valores
        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.invalidate();  // Atualiza o gráfico

        // Personalização do gráfico: adicionar título e eixos
        lineChart.getDescription().setText("Evolução do Peso Máximo ao Longo do Tempo");
        lineChart.getDescription().setTextSize(12f);

        // Personalizar os eixos
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Converte o valor do eixo X (tempo) para formato de dia ou tempo adequado
                return String.valueOf((int) value); // Remove os decimais, apenas números inteiros
            }
        });

        // Garantir que o eixo X tenha um passo de 1 para garantir números inteiros
        lineChart.getXAxis().setGranularity(1f); // A granularidade ajuda a evitar valores decimais
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


}
