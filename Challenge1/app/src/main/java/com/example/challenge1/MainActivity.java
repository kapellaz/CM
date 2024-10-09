package com.example.challenge1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.io.Serializable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private Fragment1 infofr;



    ArrayList<Animal> animals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        infofr = new Fragment1();

        ModelView modelView = new ViewModelProvider(this).get(ModelView.class);

        if (savedInstanceState != null) {
            animals = (ArrayList<Animal>) savedInstanceState.getSerializable("animals_list");
        } else {
            animals = new ArrayList<>();
            animals.add(new Animal("Frog","Rui", "Guegan", "2"));
            animals.add(new Animal("Snail","Rui", "Bruno", "3"));
            animals.add(new Animal("Rhino","Rui", "Samu", "4"));
        }

        modelView.setAnimalList(animals);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, infofr);
        ft.commit();


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //salvar os objetos
        outState.putSerializable("animals_list", animals);
    }

    public void switchToFragment2() {
        Log.v("TAG", "switch to fragment 2");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new Fragment2())
                .addToBackStack(null)
                .commit();
    }

    public void switchToFragment1() {
        getSupportFragmentManager().popBackStack();
    }
}