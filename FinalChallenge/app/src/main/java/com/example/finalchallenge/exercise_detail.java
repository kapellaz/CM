package com.example.finalchallenge;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.finalchallenge.classes.Execution;
import com.example.finalchallenge.classes.Utilizador;
import com.example.finalchallenge.classes.viewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.w3c.dom.Text;

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
    private TextView oxygenInfo;

    private boolean threads = true;
    private ProgressBar progressBar; // ProgressBar
    private DatabaseHelper databaseHelper;
    private List<Execution> executions;
    private Map<String, List<String>> execucoesPorDia = new HashMap<>();
    private viewModel modelview;
    List<String> friendsList = new ArrayList<>();
    private String selectedFriend;
    private FirebaseFirestorehelper firebaseFirestorehelper;
    public Map<String,String> nomeIDsFriends = new HashMap<>();
    public Map<String,List<String>> detail = new HashMap<>();
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

        firebaseFirestorehelper = new FirebaseFirestorehelper();

        // Obtém o mapa com as execuções agrupadas por data (de pesos por dia)
        get();
        TextView title = view.findViewById(R.id.exerciseName);
        title.setText(modelview.getExercicio().getValue().getNome());
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
        oxygenInfo = view.findViewById(R.id.oxygenInfo);

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

        List<String> exerciseOptions = new ArrayList<>();
        exerciseOptions.add("Média de Pesos");
        exerciseOptions.add("Peso Máximo");


        // Configura o Adapter para o Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                exerciseOptions
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        exerciseSpinner.setAdapter(adapter);
        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    if(threads == false){
                        if(spinner_friends.getSelectedItemPosition() == 0) {
                            showWeightAverageGraph();
                        }else {
                            showWeightAverageGraphForBothUsers(detail);
                        }
                    }

                } else if (position == 1) {
                    if(threads == false){
                        if(spinner_friends.getSelectedItemPosition() == 0) {
                            showMaxWeightGraph();
                        }else {
                            showWeightMaxGraphForBothUsers(detail);
                        }

                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Nenhuma seleção
            }
        });


        firebaseFirestorehelper.getAllFriends(modelview.getUser().getValue().getId(), new FriendsCallback() {
            @Override
            public void onFriendsFetched(List<Utilizador> amigos) {


                friendsList.add("Selecione o amigo:");
                for (Utilizador amigo : amigos) {
                    nomeIDsFriends.put(amigo.getUsername(),amigo.getId());
                    friendsList.add(amigo.getUsername());
                }

                // Atualizar o Spinner com a lista de amigos
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        friendsList
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_friends.setAdapter(adapter);
            }
        });


        spinner_friends.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    selectedFriend = friendsList.get(position);
                    String idfriend = nomeIDsFriends.get(selectedFriend);
                    firebaseFirestorehelper.getExecucoesPorExercicio(idfriend, modelview.getExercicio().getValue().getId(), new DetalhesTrainFriend() {
                        @Override
                        public void onDetalhes(Map<String, List<String>> detalhes) {

                            System.out.println("SPINNERRR  " + exerciseSpinner.getSelectedItemPosition() + " " + detalhes);
                            detail = detalhes;
                            if(detalhes == null){
                                lineChart.clear();
                                lineChart.invalidate();
                            }
                            if(exerciseSpinner.getSelectedItemPosition() == 0){
                                showWeightAverageGraphForBothUsers(detalhes);
                            }else {
                                showWeightMaxGraphForBothUsers(detalhes);

                            }
                        }
                    });

                } else {
                    showWeightAverageGraph();
                    selectedFriend = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Nenhuma seleção feita
            }
        });

    }

    public interface DetalhesTrainFriend {
        void onDetalhes(Map<String, List<String>> detalhes);
    }
    public interface FriendsCallback {
        void onFriendsFetched(List<Utilizador> amigos);
    }



    @SuppressLint("SetTextI18n")
    private void showWeightAverageGraph() {
        if (execucoesPorDia.isEmpty()) {
            return;
        }
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        ArrayList<Integer> oxigen = new ArrayList<>();
        ArrayList<Integer> bati = new ArrayList<>();

        Map<String, List<String>> groupedExecucoes = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : execucoesPorDia.entrySet()) {
            String day = extractDate(entry.getKey());
            List<String> info = entry.getValue();

            if (groupedExecucoes.containsKey(day)) {
                groupedExecucoes.get(day).addAll(info);
            } else {

                groupedExecucoes.put(day, new ArrayList<>(info));
            }
        }
        Map<String, List<String>> sortedGroupedExecucoes = new TreeMap<>(groupedExecucoes);
        int dayIndex = 0;
        for (Map.Entry<String, List<String>> entry : sortedGroupedExecucoes.entrySet()) {
            String day = entry.getKey();
            List<String> pesos = entry.getValue();

            float totalWeight = 0;
            for (String peso : pesos) {
                String[] data = peso.split("\\|");
                totalWeight += Integer.parseInt(data[0]);
                bati.add(Integer.parseInt(data[1]));
                oxigen.add(Integer.parseInt(data[2]));
            }

            float averageWeight = totalWeight / pesos.size();
            entries.add(new Entry(dayIndex, averageWeight));
            dayIndex++;
            dates.add(day);
        }


        oxygenInfo.setText("Oxigenação: " + CalculateMean(oxigen) + " %\nBatimentos Cardíacos: " + CalculateMean(bati) + " bpm");

        LineDataSet dataSet = new LineDataSet(entries, "Média de Pesos");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[0]);
        LineData lineData = new LineData(dataSet);
        dataSet.setCircleRadius(6f);
        dataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[1]);
        dataSet.setCircleHoleColor(Color.WHITE);

        lineChart.setData(lineData);
        lineChart.invalidate();


        lineChart.getDescription().setText("Evolução do Peso ao Longo do Tempo");
        lineChart.getDescription().setTextSize(14f);
        dataSet.setValueTextSize(11f);


        lineChart.getAxisLeft().setTextSize(12f);
        lineChart.getAxisRight().setTextSize(12f);


        lineChart.getXAxis().setTextSize(12f);

        lineChart.getXAxis().setLabelRotationAngle(90f);


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



    @SuppressLint("SetTextI18n")
    private void showMaxWeightGraph() {
        if (execucoesPorDia.isEmpty()) {
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();


        Map<String, List<Integer>> groupedExecucoesPeso = new HashMap<>();
        Map<String, List<Integer>> groupedExecucoesBatimentos = new HashMap<>();
        Map<String, List<Integer>> groupedExecucoesOxigenacao = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : execucoesPorDia.entrySet()) {
            String day = extractDate(entry.getKey());
            List<String> info = entry.getValue();


            for (String data : info) {
                String[] partes = data.split("\\|");


                if (partes.length == 3) {
                    int peso = Integer.parseInt(partes[0]);  // Peso
                    int batimentos = Integer.parseInt(partes[1]);  // Batimentos
                    int oxigenacao = Integer.parseInt(partes[2]);  // Oxigenação

                    groupedExecucoesPeso.computeIfAbsent(day, k -> new ArrayList<>()).add(peso);
                    groupedExecucoesBatimentos.computeIfAbsent(day, k -> new ArrayList<>()).add(batimentos);
                    groupedExecucoesOxigenacao.computeIfAbsent(day, k -> new ArrayList<>()).add(oxigenacao);
                }
            }
        }

        Map<String, List<Integer>> sortedGroupedExecucoesPeso = new TreeMap<>(groupedExecucoesPeso);


        int dayIndex = 0;
        for (String day : sortedGroupedExecucoesPeso.keySet()) {
            List<Integer> pesos = sortedGroupedExecucoesPeso.get(day);
            int maxPeso = pesos.isEmpty() ? 0 : Collections.max(pesos);
            entries.add(new Entry(dayIndex, maxPeso));
            dayIndex++;
            dates.add(day);
        }

        oxygenInfo.setText("Oxigenação: " + calcularMaiorValor(groupedExecucoesOxigenacao) + " %\nBatimentos Cardíacos: " + calcularMaiorValor(groupedExecucoesBatimentos) + " bpm");


        LineDataSet dataSet = new LineDataSet(entries, "Peso Máximo");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[2]);
        dataSet.setValueTextColor(ColorTemplate.MATERIAL_COLORS[2]);
        LineData lineData = new LineData(dataSet);
        dataSet.setCircleRadius(6f);
        dataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[1]);  // Cor dos círculos
        dataSet.setCircleHoleColor(Color.WHITE);
        lineChart.setData(lineData);
        lineChart.invalidate();
        lineChart.getDescription().setText("Evolução do Peso ao Longo do Tempo");
        lineChart.getDescription().setTextSize(14f);
        dataSet.setValueTextSize(11f);
        lineChart.getAxisLeft().setTextSize(12f);
        lineChart.getAxisRight().setTextSize(12f);
        lineChart.getXAxis().setTextSize(12f);
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
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private String extractDate(String fullDate) {
        try {

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


            Date date = inputFormat.parse(fullDate);


            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void showWeightAverageGraphForBothUsers(Map<String, List<String>> detalhes) {


            if (execucoesPorDia.isEmpty() || detalhes.isEmpty()) {
                System.out.println("YAHHHH");
                lineChart.clear();
                lineChart.invalidate();
            return;
            }

        // Listas para armazenar os pontos do gráfico
        List<Entry> entriesUser1 = new ArrayList<>();
        List<Entry> entriesUser2 = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        // Mapas para agrupar as execuções por data para dois usuários
        Map<String, List<Integer>> groupedExecucoesUser1 = new HashMap<>();
        Map<String, List<Integer>> groupedExecucoesUser2 = new HashMap<>();


        for (Map.Entry<String, List<String>> entry : execucoesPorDia.entrySet()) {
            String day = extractDate(entry.getKey());  // Extrai a data da chave
            List<String> pesos = entry.getValue();  // Lista de pesos associados à data


            List<Integer> pesosUser1 = new ArrayList<>();  // Para o User 1



            for (int i = 0; i < pesos.size(); i++) {

                String[] data2 = pesos.get(i).split("\\|");
                pesosUser1.add(i, Integer.parseInt(data2[0]));
               }

            // Agrupando as execuções por data para os dois usuários
            if (groupedExecucoesUser1.containsKey(day)) {
                groupedExecucoesUser1.get(day).addAll(pesosUser1);
            } else {
                groupedExecucoesUser1.put(day, new ArrayList<>(pesosUser1));
            }

        }


            for (Map.Entry<String, List<String>> entry : detalhes.entrySet()) {
                String day = extractDate(entry.getKey());  // Extrai a data da chave
                List<String> pesos = entry.getValue();  // Lista de pesos associados à data


                List<Integer> pesosUser2 = new ArrayList<>();  // Para o User 2

                // Ajustando os pesos para simular dados diferentes para os dois usuários
                for (int i = 0; i < pesos.size(); i++) {

                    String[] data2 = pesos.get(i).split("\\|");

                    pesosUser2.add(i, Integer.parseInt(data2[0]));   // Simula redução para o User 2
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

    // Cria os conjuntos de dados para os dois usuários
                LineDataSet dataSetUser1 = new LineDataSet(entriesUser1, "Média de Pesos - " + modelview.getUser().getValue().getUsername());
                dataSetUser1.setColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor para o Usuário 1
                dataSetUser1.setValueTextColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor dos números para o Usuário 1
                dataSetUser1.setCircleRadius(6f);  // Tamanho do círculo
                dataSetUser1.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor do círculo
                dataSetUser1.setCircleHoleColor(Color.WHITE);  // Cor do centro do círculo
                dataSetUser1.setValueTextSize(11f);  // Tamanho do texto dos valores

                Legend legend = lineChart.getLegend();
                legend.setTextSize(14);  // Aumenta o tamanho do texto da legenda
                legend.setFormSize(10f);  // Opcional: Tamanho do marcador (círculo) na legenda
                legend.setForm(Legend.LegendForm.CIRCLE);

            LineDataSet dataSetUser2 = new LineDataSet(entriesUser2, "Média de Pesos - " + selectedFriend);
                dataSetUser2.setColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor para o Usuário 2
                dataSetUser2.setValueTextColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor dos números para o Usuário 2
                dataSetUser2.setCircleRadius(6f);  // Tamanho do círculo
                dataSetUser2.setCircleColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor do círculo
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










    public void get(){
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                execucoesPorDia = databaseHelper.getExecucoesPorExercicio(
                        Objects.requireNonNull(modelview.getUser().getValue()).getId(),
                        modelview.getExercicio().getValue().getId()
                );

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
    public static int calcularMaiorValor(Map<String, List<Integer>> groupedExecucoes) {
        int maiorValor = Integer.MIN_VALUE;


        for (Map.Entry<String, List<Integer>> entry : groupedExecucoes.entrySet()) {
            List<Integer> valores = entry.getValue();

            // Encontra o maior valor na lista
            for (Integer valor : valores) {
                if (valor > maiorValor) {
                    maiorValor = valor;
                }
            }
        }

        return maiorValor;
    }





    private void showWeightMaxGraphForBothUsers(Map<String, List<String>> detalhes) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {

                if (execucoesPorDia.isEmpty() || detalhes.isEmpty()) {
                    return;
                }
                System.out.println();
                // Listas para armazenar os pontos do gráfico
                List<Entry> entriesUser1 = new ArrayList<>();
                List<Entry> entriesUser2 = new ArrayList<>();
                List<String> dates = new ArrayList<>();

                // Mapas para agrupar as execuções por data para dois usuários
                Map<String, List<Integer>> groupedExecucoesUser1 = new HashMap<>();
                Map<String, List<Integer>> groupedExecucoesUser2 = new HashMap<>();

                // Processamento dos dados para o User 1
                for (Map.Entry<String, List<String>> entry : execucoesPorDia.entrySet()) {
                    String day = extractDate(entry.getKey());  // Extrai a data da chave
                    List<String> pesos = entry.getValue();  // Lista de pesos associados à data

                    List<Integer> pesosUser1 = new ArrayList<>();  // Para o User 1
                    for (int i = 0; i < pesos.size(); i++) {
                        String[] data2 = pesos.get(i).split("\\|");
                        pesosUser1.add(i, Integer.parseInt(data2[0]));
                    }

                    // Agrupando as execuções por data para o User 1
                    if (groupedExecucoesUser1.containsKey(day)) {
                        groupedExecucoesUser1.get(day).addAll(pesosUser1);
                    } else {
                        groupedExecucoesUser1.put(day, new ArrayList<>(pesosUser1));
                    }
                }

                // Processamento dos dados para o User 2
                for (Map.Entry<String, List<String>> entry : detalhes.entrySet()) {
                    String day = extractDate(entry.getKey());  // Extrai a data da chave
                    List<String> pesos = entry.getValue();  // Lista de pesos associados à data

                    List<Integer> pesosUser2 = new ArrayList<>();  // Para o User 2
                    for (int i = 0; i < pesos.size(); i++) {
                        String[] data2 = pesos.get(i).split("\\|");
                        pesosUser2.add(i, Integer.parseInt(data2[0]));   // Simula dados para o User 2
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

                // Calcula o peso máximo para o User 1
                for (Map.Entry<String, List<Integer>> entry : sortedGroupedExecucoesUser1.entrySet()) {
                    String day = entry.getKey();
                    List<Integer> pesosUser1 = entry.getValue();

                    // Encontra o peso máximo para o User 1
                    int maxWeightUser1 = Integer.MIN_VALUE;
                    for (Integer peso : pesosUser1) {
                        maxWeightUser1 = Math.max(maxWeightUser1, peso);
                    }

                    entriesUser1.add(new Entry(dayIndex, maxWeightUser1));
                    dayIndex++;
                    dates.add(day);  // Adiciona a data à lista
                }

                dayIndex = 0;

                // Calcula o peso máximo para o User 2
                for (Map.Entry<String, List<Integer>> entry : sortedGroupedExecucoesUser2.entrySet()) {
                    String day = entry.getKey();
                    List<Integer> pesosUser2 = entry.getValue();

                    // Encontra o peso máximo para o User 2
                    int maxWeightUser2 = Integer.MIN_VALUE;
                    for (Integer peso : pesosUser2) {
                        maxWeightUser2 = Math.max(maxWeightUser2, peso);
                    }

                    entriesUser2.add(new Entry(dayIndex, maxWeightUser2));
                    dayIndex++;
                }
                   requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Cria os conjuntos de dados para os dois usuários
                        LineDataSet dataSetUser1 = new LineDataSet(entriesUser1, "Peso Máximo - " + modelview.getUser().getValue().getUsername());
                        dataSetUser1.setColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor para o Usuário 1
                        dataSetUser1.setValueTextColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor dos números para o Usuário 1
                        dataSetUser1.setCircleRadius(6f);  // Tamanho do círculo
                        dataSetUser1.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);  // Cor do círculo
                        dataSetUser1.setCircleHoleColor(Color.WHITE);  // Cor do centro do círculo
                        dataSetUser1.setValueTextSize(11f);  // Tamanho do texto dos valores

                        LineDataSet dataSetUser2 = new LineDataSet(entriesUser2, "Peso Máximo - " + selectedFriend);
                        dataSetUser2.setColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor para o Usuário 2
                        dataSetUser2.setValueTextColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor dos números para o Usuário 2
                        dataSetUser2.setCircleRadius(6f);  // Tamanho do círculo
                        dataSetUser2.setCircleColor(ColorTemplate.MATERIAL_COLORS[2]);  // Cor do círculo
                        dataSetUser2.setCircleHoleColor(Color.WHITE);  // Cor do centro do círculo
                        dataSetUser2.setValueTextSize(11f);  // Tamanho do texto dos valores

                        // Cria o conjunto de dados para o gráfico
                        LineData lineData = new LineData(dataSetUser1, dataSetUser2);

                        // Atualiza o gráfico
                        lineChart.setData(lineData);
                        lineChart.invalidate();

                        // Título do gráfico
                        lineChart.getDescription().setText("Evolução do Peso Máximo ao Longo do Tempo");
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




    public static double CalculateMean(ArrayList<Integer> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        int sum = 0;
        for (int num : list) {
            sum += num;
        }


        return (double) sum / list.size();
    }


}
