package com.example.challenge1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private Fragment1 infofr;
    private Fragment2 editfr;


    ArrayList<Animal> animals = new ArrayList<>();

    Animal animal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        infofr = new Fragment1();
        editfr = new Fragment2();


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.animal_info, infofr);
        ft.add(R.id.animal_edit, editfr);
        ft.commit();


    }
}