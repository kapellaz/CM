package com.example.challenge1;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
public class ModelView extends ViewModel{
    private MutableLiveData<Animal> AnimalData = new MutableLiveData<>();

    private MutableLiveData<ArrayList<Animal>> ListAnimal = new MutableLiveData<>();
    private ArrayList<Animal> animals;


    public void setAnimalData(Animal animal) {
        AnimalData.setValue(animal);
    }

    public LiveData<Animal> getAnimal() {
        return AnimalData;
    }

    public void setAnimalList(ArrayList<Animal> list) {
        ListAnimal.setValue(list);
    }

    public LiveData<ArrayList<Animal>> getAnimalList() {
        return ListAnimal;
    }
}
