package com.example.finalchallenge.classes;

import androidx.annotation.NonNull;

public class Execution {
    private int peso;
    private int numeroSerie;
    private int exec;
    private int series;
    private int repeticoes;
    private String data;

    public Execution(int peso, int numeroSerie, int exec, int series, int repeticoes,String data) {
        this.peso = peso;
        this.numeroSerie = numeroSerie;
        this.exec = exec;
        this.series = series;
        this.repeticoes = repeticoes;
        this.data = data;
    }

    // Getters e setters
    public int getPeso() {
        return peso;
    }
    public String getData() {
        return data;
    }

    public int getNumeroSerie() {
        return numeroSerie;
    }

    public int getExec() {
        return exec;
    }

    public int getSeries() {
        return series;
    }

    public int getRepeticoes() {
        return repeticoes;
    }

}
