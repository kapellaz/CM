package com.example.finalchallenge.classes;

public class TreinosDone {
    private int id;
    private int treino_id;
    private String data;
    private int exec;


    public TreinosDone(int id, int treino_id, String data, int exec) {
        this.id = id;
        this.treino_id = treino_id;
        this.data = data;
        this.exec = exec;
    }

    // Getters e setters
    public int getId() {
        return id;
    }

    public int getExec() {
        return exec;
    }

    public void setExec(int exec) {
        this.exec = exec;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTreino_id() {
        return treino_id;
    }

    public void setTreino_id(int treino_id) {
        this.treino_id = treino_id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Treino ID: " + treino_id + " Data: " + data + " Execução: " + exec;
    }

}
