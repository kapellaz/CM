package com.example.finalchallenge.classes;

import java.util.Map;

public class ExerciseDetailed {

    private final Map<Integer, Integer> seriesMap;
    private final Exercise exercise;


    public ExerciseDetailed(Exercise exercise, Map<Integer, Integer> seriesMap) {
        this.exercise = exercise;
        this.seriesMap = seriesMap;

    }

    public Exercise getExercise() {
        return exercise;
    }

    public Map<Integer, Integer> getSeriesMap() {
        return seriesMap;
    }

    @Override
    public String toString() {
        return exercise.getName() +
                " | Séries: " + exercise.getSeries() +
                " | Repetições: " + exercise.getRepetitions() +
                "| ID: " + exercise.getId();
    }

}
