package com.example.challenge1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment1 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Spinner spin;
    private Animal animal;
    private ImageView animalView;
    private TextView animalName;
    private TextView animalAge;
    private TextView animalOwner;
    ArrayList<Animal> animals = new ArrayList<>();
    private Button button;

    public Fragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment1 newInstance(String param1, String param2) {
        Fragment1 fragment = new Fragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(animals.isEmpty()){
            animals.add(new Animal("Rui", "Guegan", "2"));
            animals.add(new Animal("Rui", "Bruno", "3"));
            animals.add(new Animal("Rui", "Samu", "4"));
        }

        View view = inflater.inflate(R.layout.fragment_1, container, false);
        spin = view.findViewById(R.id.spinner);
        animalView = view.findViewById(R.id.animalView);
        animalName = view.findViewById(R.id.animalName);
        animalAge = view.findViewById(R.id.animalAge);
        animalOwner = view.findViewById(R.id.animalOwner);

        ArrayAdapter<Animal> adapter = new ArrayAdapter<Animal>(getContext(), android.R.layout.simple_spinner_dropdown_item, animals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                animal = (Animal) adapterView.getSelectedItem();
                setAnimal(animal);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return view;
    }


    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("TAG","button clicked");
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Fragment2 editFragment = new Fragment2();

                transaction.replace(R.id.animal_info, editFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


    }


    public void setAnimal(Animal animal) {
        switch (animal.getName()) {
            case "Guegan":
                animalView.setImageResource(R.drawable.frog);
                break;
            case "Bruno":
                animalView.setImageResource(R.drawable.rhino);
                break;
            case "Samu":
                animalView.setImageResource(R.drawable.snail);
                break;
        }
        animalName.setText("Name: " + animal.getName());
        animalAge.setText("Age: " + animal.getAge());
        animalOwner.setText("Owner: " + animal.getOwner());
    }
}