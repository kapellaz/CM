package com.example.finalchallenge.classes;

import java.util.HashMap;
import java.util.Map;

public class TreinoExercicioPlano {
    private int id;               // ID do plano de exercício
    private int exercicio_id;     // ID do exercício
    private int treino_id;        // ID do treino
    private int series;           // Número de séries
    private int repeticoes;       // Número de repetições
    private int order_id;         // ID da ordem do exercício
    private String user_id;       // ID do usuário

    // Construtor
    public TreinoExercicioPlano(int id, int exercicio_id, int treino_id, int series, int repeticoes, int order_id, String user_id) {
        this.id = id;
        this.exercicio_id = exercicio_id;
        this.treino_id = treino_id;
        this.series = series;
        this.repeticoes = repeticoes;
        this.order_id = order_id;
        this.user_id = user_id;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExercicio_id() {
        return exercicio_id;
    }

    public void setExercicio_id(int exercicio_id) {
        this.exercicio_id = exercicio_id;
    }

    public int getTreino_id() {
        return treino_id;
    }

    public void setTreino_id(int treino_id) {
        this.treino_id = treino_id;
    }

    public int getSeries() {
        return series;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public int getRepeticoes() {
        return repeticoes;
    }

    public void setRepeticoes(int repeticoes) {
        this.repeticoes = repeticoes;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("exercicio_id",exercicio_id);
        map.put("treino_id",treino_id);
        map.put("series",series);
        map.put("repeticoes",repeticoes);
        map.put("order_id",order_id);
        map.put("user_id", user_id);
        return map;
    }

    @Override
    public String toString() {
        return "series: " + series +
                " repeticoes: " + repeticoes;
    }
}
