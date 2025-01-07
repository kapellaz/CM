package com.example.finalchallenge.classes;

public class SeriesInfo {
    private int peso;
    private int series;
    private int exercicioId;
    private int treinoId;
    private int exec;
    private int oxigenacao;
    private int batimentos;

    // Construtor
    public SeriesInfo(int peso, int series, int exercicioId, int treinoId, int exec, int oxigenacao, int batimentos) {
        this.peso = peso;
        this.series = series;
        this.exercicioId = exercicioId;
        this.treinoId = treinoId;
        this.exec = exec;
        this.oxigenacao = oxigenacao;
        this.batimentos = batimentos;
    }

    // Getters e Setters
    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public int getSeries() {
        return series;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public int getExercicioId() {
        return exercicioId;
    }

    public void setExercicioId(int exercicioId) {
        this.exercicioId = exercicioId;
    }

    public int getTreinoId() {
        return treinoId;
    }

    public void setTreinoId(int treinoId) {
        this.treinoId = treinoId;
    }

    public int getExec() {
        return exec;
    }

    public void setExec(int exec) {
        this.exec = exec;
    }

    public int getOxigenacao() {
        return oxigenacao;
    }

    public void setOxigenacao(int oxigenacao) {
        this.oxigenacao = oxigenacao;
    }

    public int getBatimentos() {
        return batimentos;
    }

    public void setBatimentos(int batimentos) {
        this.batimentos = batimentos;
    }

    // Método para exibir os dados de SeriesInfo
    @Override
    public String toString() {
        return "SeriesInfo{" +
                "peso=" + peso +
                ", series=" + series +
                ", exercicioId=" + exercicioId +
                ", treinoId=" + treinoId +
                ", exec=" + exec +
                ", oxigenacao=" + oxigenacao +
                ", batimentos=" + batimentos +
                '}';
    }
}
