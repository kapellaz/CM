package com.example.finalchallenge.classes;

public class TreinoExec {
    private int id;
    private String nome;
    private int userId;
    private String data;

    public TreinoExec(int id, String nome, int userId, String data) {
        this.id = id;
        this.nome = nome;
        this.userId = userId;
        this.data = data;
    }

    // Getters e setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return nome + " Data: " + data;
    }
}
