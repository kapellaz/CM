package com.example.finalchallenge.classes;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class viewModel extends ViewModel {
    private final MutableLiveData<TreinoPlano> selectedData = new MutableLiveData<>();

    private final MutableLiveData<List<Exercise>> Exercises = new MutableLiveData<>();

    private final MutableLiveData<Utilizador> user = new MutableLiveData<>();

    private final MutableLiveData<Exercicio> exercicioMutableLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isFirstTime = new MutableLiveData<>();

    private final MutableLiveData<Boolean> firstTIMEinFrag = new MutableLiveData<>();

    public MutableLiveData<TreinoPlano> getSelectedPlan() {
        return selectedData;
    }

    public MutableLiveData<Boolean> getIsFirstTime(){
        return isFirstTime;
    }

    public void setIsFirstTime(Boolean value){
        this.isFirstTime.setValue(value);
    }

    public MutableLiveData<Boolean> getFirstTIMEinFrag(){
        return isFirstTime;
    }

    public void setfirstTIMEinFrag(Boolean value){
        this.firstTIMEinFrag.setValue(value);
    }

    public void setSelectedPlan(TreinoPlano treinoExec) {
        this.selectedData.setValue(treinoExec);
    }

    public MutableLiveData<List<Exercise>> getExercises() {
        return Exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.Exercises.setValue(exercises);
    }

    public MutableLiveData<Utilizador> getUser() {
        return user;
    }

    public void setUser(Utilizador user) {
        this.user.setValue(user);
    }

    public void setExercicio(Exercicio exercicio) {
        this.exercicioMutableLiveData.setValue(exercicio);
    }

    public MutableLiveData<Exercicio> getExercicio() {
        return exercicioMutableLiveData;
    }

}
