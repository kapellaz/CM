package com.example.challenge1;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
public class ModelView extends ViewModel{
    // LiveData que armazena dados de um único objeto Animal (o último a ser selecionado/editado)
    private MutableLiveData<Animal> AnimalData = new MutableLiveData<>();

    // LiveData que armazena uma lista de objetos Animal
    private MutableLiveData<ArrayList<Animal>> ListAnimal = new MutableLiveData<>();

    // Define o valor do AnimalData com um novo objeto Animal
    public void setAnimalData(Animal animal) {
        AnimalData.setValue(animal);
    }

    // Retorna o LiveData que contém o objeto Animal
    public LiveData<Animal> getAnimal() {
        return AnimalData;
    }

    // Define a lista de animais (ArrayList<Animal>) no ListAnimal
    public void setAnimalList(ArrayList<Animal> list) {
        ListAnimal.setValue(list);
    }

    // Retorna o LiveData que contém a lista de animais (ArrayList<Animal>)
    public LiveData<ArrayList<Animal>> getAnimalList() {
        return ListAnimal;
    }
}
