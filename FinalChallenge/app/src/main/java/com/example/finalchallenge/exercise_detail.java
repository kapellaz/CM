package com.example.finalchallenge;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class exercise_detail extends Fragment {
    private Spinner spinner_friends;
    private Spinner exerciseSpinner;
    private LineChart lineChart;
    private ImageButton logoutButton;
    private ImageButton halterButton;
    private ImageButton perfilButton;
    private ImageButton statsButton;
    private boolean threads = true;
    private ProgressBar progressBar; // ProgressBar
    private DatabaseHelper databaseHelper;
    private List<Execution> executions;
    private Map<String, List<Integer>> execucoesPorDia = new HashMap<>();
    private viewModel modelview;
    private String selectedFriend;
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
        progressBar = view.findViewById(R.id.progressBar);
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);

        // Obtém o mapa com as execuções agrupadas por data (de pesos por dia)
        get();
        spinner_friends = view.findViewById(R.id.spinner_friends);
        // Localiza o Spinner
        exerciseSpinner = view.findViewById(R.id.spinner);
        lineChart = view.findViewById(R.id.activity_main_linechart);
        lineChart.setVisibility(View.INVISIBLE);
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
                    if(threads == false){
                        showWeightAverageGraph();
                    }

                } else if (position == 1) {
                    // Exibe o peso máximo no gráfico
                    if(threads == false){
                        showMaxWeightGraph();
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Nenhuma seleção
            }
        });

        // Dados para exibir no Spinner de amigos
        List<String> friendsList = new ArrayList<>();
        friendsList.add("Selecione o seu amigo");
        friendsList.add("John Doe");
        friendsList.add("Jane Smith");
        friendsList.add("Alice Johnson");
        friendsList.add("Bob Brown");

// Configura o Adapter para o Spinner de amigos
        ArrayAdapter<String> friendsAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item, // Layout padrão para itens no Spinner
                friendsList // Lista de amigos
        );

        friendsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_friends.setAdapter(friendsAdapter);

// Listener para o Spinner de amigos
        spinner_friends.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    selectedFriend = friendsList.get(position);
                    // Faça algo com o amigo selecionado, por exemplo:
                    System.out.println("Amigo selecionado: " + selectedFriend);
                    showWeightAverageGraphForBothUsers(); // Ou qualquer outra função necessária
                } else {
                    showWeightAverageGraph();
                    selectedFriend = null; // Nenhum amigo selecionado ainda
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Nenhuma seleção feita
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
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[0]);
        LineData lineData = new LineData(dataSet);
        dataSet.setCircleRadius(6f);  // Aumenta o raio dos pontos para 6 (valor padrão é 4f)
        dataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[1]);  // Cor dos círculos
        dataSet.setCircleHoleColor(Color.WHITE);

        lineChart.setData(lineData);
        lineChart.invalidate();


        lineChart.getDescription().setText("Evolução do Peso ao Longo do Tempo");
        lineChart.getDescription().setTextSize(14f); // Aumenta o tamanho do texto da descrição
        dataSet.setValueTextSize(11f);  // Aumenta o tamanho da fonte dos números associados aos pontos

// Aumenta o tamanho dos números no eixo Y
        lineChart.getAxisLeft().setTextSize(12f); // Para os números do eixo Y à esquerda
        lineChart.getAxisRight().setTextSize(12f); // Para os números do eixo Y à direita

// Aumenta o tamanho dos números no eixo X
        lineChart.getXAxis().setTextSize(12f);
        // Rotaciona os rótulos do eixo X para ficar vertical
        lineChart.getXAxis().setLabelRotationAngle(90f); // Rotaciona os rótulos do eixo X para 90 graus


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
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[2]);
        LineData lineData = new LineData(dataSet);
        dataSet.setCircleRadius(6f);  // Aumenta o raio dos pontos para 6 (valor padrão é 4f)
        dataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[1]);  // Cor dos círculos
        dataSet.setCircleHoleColor(Color.WHITE);

        lineChart.setData(lineData);
        lineChart.invalidate();


        lineChart.getDescription().setText("Evolução do Peso ao Longo do Tempo");
        lineChart.getDescription().setTextSize(14f); // Aumenta o tamanho do texto da descrição
        dataSet.setValueTextSize(11f);  // Aumenta o tamanho da fonte dos números associados aos pontos

// Aumenta o tamanho dos números no eixo Y
        lineChart.getAxisLeft().setTextSize(12f); // Para os números do eixo Y à esquerda
        lineChart.getAxisRight().setTextSize(12f); // Para os números do eixo Y à direita

// Aumenta o tamanho dos números no eixo X
        lineChart.getXAxis().setTextSize(12f);
        // Rotaciona os rótulos do eixo X para ficar vertical
      //  lineChart.getXAxis().setLabelRotationAngle(90f); // Rotaciona os rótulos do eixo X para 90 graus


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


        lineChart.getXAxis().setGranularity(10f);
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

    private void showWeightAverageGraphForBothUsers() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {

                if (execucoesPorDia.isEmpty()) {
                return;
                }

            // Listas para armazenar os pontos do gráfico
            List<Entry> entriesUser1 = new ArrayList<>();
            List<Entry> entriesUser2 = new ArrayList<>();
            List<String> dates = new ArrayList<>();

            // Mapas para agrupar as execuções por data para dois usuários
            Map<String, List<Integer>> groupedExecucoesUser1 = new HashMap<>();
            Map<String, List<Integer>> groupedExecucoesUser2 = new HashMap<>();

            // Supondo que `execucoesPorDia` contenha dados de execuções de 2 usuários
            // Vamos simular para dois usuários, com dados diferentes para cada um

            for (Map.Entry<String, List<Integer>> entry : execucoesPorDia.entrySet()) {
                String day = extractDate(entry.getKey());  // Extrai a data da chave
                List<Integer> pesos = entry.getValue();  // Lista de pesos associados à data

                // Simulando pesos diferentes para os dois usuários
                List<Integer> pesosUser1 = new ArrayList<>(pesos);  // Para o User 1
                List<Integer> pesosUser2 = new ArrayList<>(pesos);  // Para o User 2

                // Ajustando os pesos para simular dados diferentes para os dois usuários
                for (int i = 0; i < pesosUser1.size(); i++) {
                    pesosUser1.set(i, pesosUser1.get(i) + 5000);  // Simula aumento para o User 1
                    pesosUser2.set(i, pesosUser2.get(i) - 4000);   // Simula redução para o User 2
                }

                // Agrupando as execuções por data para os dois usuários
                if (groupedExecucoesUser1.containsKey(day)) {
                    groupedExecucoesUser1.get(day).addAll(pesosUser1);
                } else {
                    groupedExecucoesUser1.put(day, new ArrayList<>(pesosUser1));
                }

                if (groupedExecucoesUser2.containsKey(day)) {
                    groupedExecucoesUser2.get(day).addAll(pesosUser2);
                } else {
                    groupedExecucoesUser2.put(day, new ArrayList<>(pesosUser2));
                }
            }

            // Ordena os mapas para garantir que as datas sejam exibidas de forma crescente
            Map<String, List<Integer>> sortedGroupedExecucoesUser1 = new TreeMap<>(groupedExecucoesUser1);
            Map<String, List<Integer>> sortedGroupedExecucoesUser2 = new TreeMap<>(groupedExecucoesUser2);

            int dayIndex = 0;

            // Calcula a média de pesos para o primeiro usuário
            for (Map.Entry<String, List<Integer>> entry : sortedGroupedExecucoesUser1.entrySet()) {
                String day = entry.getKey();
                List<Integer> pesosUser1 = entry.getValue();

                float totalWeightUser1 = 0;
                for (Integer peso : pesosUser1) {
                    totalWeightUser1 += peso;
                }

                float averageWeightUser1 = totalWeightUser1 / pesosUser1.size();
                entriesUser1.add(new Entry(dayIndex, averageWeightUser1));
                dayIndex++;
                dates.add(day);  // Adiciona a data à lista
            }

            dayIndex = 0;

            // Calcula a média de pesos para o segundo usuário
            for (Map.Entry<String, List<Integer>> entry : sortedGroupedExecucoesUser2.entrySet()) {
                String day = entry.getKey();
                List<Integer> pesosUser2 = entry.getValue();

                float totalWeightUser2 = 0;
                for (Integer peso : pesosUser2) {
                    totalWeightUser2 += peso;
                }

                float averageWeightUser2 = totalWeightUser2 / pesosUser2.size();
                entriesUser2.add(new Entry(dayIndex, averageWeightUser2));
                dayIndex++;
            }
        // Atualiza a interface na thread principal (UI thread)
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
        // Cria os conjuntos de dados para os dois usuários
                    LineDataSet dataSetUser1 = new LineDataSet(entriesUser1, "Média de Pesos - " + modelview.getUser().getValue().getUsername());
                    dataSetUser1.setColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor para o Usuário 1
                    dataSetUser1.setValueTextColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor dos números para o Usuário 1
                    dataSetUser1.setCircleRadius(6f);  // Tamanho do círculo
                    dataSetUser1.setCircleColor(ColorTemplate.MATERIAL_COLORS[1]);  // Cor do círculo
                    dataSetUser1.setCircleHoleColor(Color.WHITE);  // Cor do centro do círculo
                    dataSetUser1.setValueTextSize(11f);  // Tamanho do texto dos valores

                    LineDataSet dataSetUser2 = new LineDataSet(entriesUser2, "Média de Pesos - " + selectedFriend);
                    dataSetUser2.setColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor para o Usuário 2
                    dataSetUser2.setValueTextColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor dos números para o Usuário 2
                    dataSetUser2.setCircleRadius(6f);  // Tamanho do círculo
                    dataSetUser2.setCircleColor(ColorTemplate.MATERIAL_COLORS[3]);  // Cor do círculo
                    dataSetUser2.setCircleHoleColor(Color.WHITE);  // Cor do centro do círculo
                    dataSetUser2.setValueTextSize(11f);  // Tamanho do texto dos valores

                    // Cria o conjunto de dados para o gráfico
                    LineData lineData = new LineData(dataSetUser1, dataSetUser2);

                    // Atualiza o gráfico
                    lineChart.setData(lineData);
                    lineChart.invalidate();

                    // Título do gráfico
                    lineChart.getDescription().setText("Evolução do Peso ao Longo do Tempo");
                    lineChart.getDescription().setTextSize(14f); // Tamanho do texto da descrição

                    // Ajusta o tamanho do texto dos números nos eixos
                    lineChart.getAxisLeft().setTextSize(12f);  // Eixo Y à esquerda
                    lineChart.getAxisRight().setTextSize(12f); // Eixo Y à direita
                    lineChart.getXAxis().setTextSize(12f);  // Eixo X
                    lineChart.getXAxis().setLabelRotationAngle(90f); // Rotaciona os rótulos do eixo X para 90 graus

                    // Configura a exibição das datas nos eixos
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
            }
            });
            }
        });
    }


    public void get(){
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);  // Mostrar o ProgressBar
                }

                execucoesPorDia = databaseHelper.getExecucoesPorExercicio(
                        Objects.requireNonNull(modelview.getUser().getValue()).getId(),
                        modelview.getExercicio().getValue().getId()
                );
                // Atualize a UI na thread principal
                lineChart.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        lineChart.setVisibility(View.VISIBLE);

                        showWeightAverageGraph(); // Atualize o gráfico
                    }
                });

                threads = false;
            }}
        );

    }


}
