package com.example.finalchallenge.classes;

public class TreinoPlano {
    private int id;
    private String nome;
    private String userId;
    public TreinoPlano() {
    }
    // Construtor
    public TreinoPlano(int id, String nome, String userId) {
        this.id = id;
        this.nome = nome;
        this.userId = userId;
    }

    // Getters e Setters
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return nome ;
    }
}
