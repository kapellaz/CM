package com.example.finalchallenge.classes;
public class Exercise {
    private int id;
    private String name;
    private int series;
    private int repetitions;
    private int order;

    public Exercise(int id, String name, int series, int repetitions, int order) {
        this.id = id;
        this.name = name;
        this.series = series;
        this.repetitions = repetitions;
        this.order = order;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSeries() {
        return series;
    }

    public int getRepetitions() {
        return repetitions;
    }
    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }


    public void setSeries(int series) {
        this.series = series;
    }



    @Override
    public String toString() {
        return name  +
                " | Séries: " + series +
                " | Repetições: " + repetitions +
                "| " + order;
    }
}
