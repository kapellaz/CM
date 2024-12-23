package com.example.finalchallenge.classes;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class viewModel extends ViewModel {
    private final MutableLiveData<TreinoPlano> selectedData = new MutableLiveData<>();

    private final MutableLiveData<List<Exercise>> Exercises = new MutableLiveData<>();

    public MutableLiveData<TreinoPlano> getSelectedPlan() {
        return selectedData;
    }
    public void setSelectedPlan(TreinoPlano treinoExec) {
        this.selectedData.setValue(treinoExec);
    }

    public MutableLiveData<List<Exercise>> getExercises() {
        return Exercises;
    }
    public void setExercises(List<Exercise> exercises) {
        System.out.println(exercises);
        this.Exercises.setValue(exercises);
    }
}
