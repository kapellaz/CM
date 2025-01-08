package com.example.finalchallenge.classes;

import java.util.HashMap;
import java.util.Map;

public class TreinoPlano {
    private int id;
    private String nome;
    private String userId;
    private int valid;

    // Construtor
    public TreinoPlano(int id, String nome, String userId,int valid) {
        this.id = id;
        this.nome = nome;
        this.userId = userId;
        this.valid = valid;
    }

    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id",id);
        map.put("nome", nome);
        map.put("user_id", userId);
        map.put("valid", valid);
        return map;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | " + nome;
    }
}
