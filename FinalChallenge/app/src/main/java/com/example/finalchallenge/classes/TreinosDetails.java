package com.example.finalchallenge.classes;

import java.util.List;

public class TreinosDetails {
    private int id;
    private int treino_id;
    private String data;
    List<ExerciseDetailed> exercise;
    private int exec;

    public TreinosDetails(int id, int treino_id, String data, List<ExerciseDetailed> exercises, int exec) {
        this.id = id;
        this.treino_id = treino_id;
        this.data = data;
        this.exec = exec;
        this.exercise = exercises;

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

    public List<ExerciseDetailed> getExercise() {
        return exercise;
    }




    public void setExercise(List<ExerciseDetailed> exercise) {
        this.exercise = exercise;
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
        return "Treino ID: " + treino_id + " Data: " + data;
    }
}
