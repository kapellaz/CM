package com.example.challenge1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class Fragment1 extends Fragment {
    
    private Spinner spin;
    private Animal animal;
    private ImageView animalView;
    private TextView animalName;
    private TextView animalAge;
    private TextView animalOwner;
    ArrayList<Animal> animals = new ArrayList<>();
    private Button button;
    private ModelView modelView;

    public Fragment1() {
        // Required empty public constructor
    }


    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //finde the view model
        modelView = new ViewModelProvider(requireActivity()).get(ModelView.class);
        //get animals from the view model
        animals = modelView.getAnimalList().getValue();


        View view = inflater.inflate(R.layout.fragment_1, container, false);
        spin = view.findViewById(R.id.spinner);
        animalView = view.findViewById(R.id.animalView);
        animalName = view.findViewById(R.id.animalName);
        animalAge = view.findViewById(R.id.animalAge);
        animalOwner = view.findViewById(R.id.animalOwner);

        ArrayAdapter<Animal> adapter = new ArrayAdapter<Animal>(getContext(), android.R.layout.simple_spinner_dropdown_item, animals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        if(modelView.getAnimal() != null){
            spin.setSelection(animals.indexOf(modelView.getAnimal().getValue()));
        }

        // Set the animal to the selected item
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                animal = (Animal) adapterView.getSelectedItem();
                setAnimal(animal);
                modelView.setAnimalData(animal); // saving last animal
            }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });



        return view;
    }


    /**
     * Called when the fragment is created.
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        modelView = new ViewModelProvider(requireActivity()).get(ModelView.class);
        button = view.findViewById(R.id.button);

        // Switch to Fragment2 when the button is clicked
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("TAG","button clicked");
                modelView.setAnimalData(animal);
                ((MainActivity) getActivity()).switchToFragment2();
            }
        });


    }


    /**
     * Set the animal to the selected item
     */
    public void setAnimal(Animal animal) {
        switch (animal.getType()) {
            case "Frog":
                animalView.setImageResource(R.drawable.frog);
                break;
            case "Rhino":
                animalView.setImageResource(R.drawable.rhino);
                break;
            case "Snail":
                animalView.setImageResource(R.drawable.snail);
                break;
        }
        animalName.setText("Name: " + animal.getName());
        animalAge.setText("Age: " + animal.getAge());
        animalOwner.setText("Owner: " + animal.getOwner());
    }
}