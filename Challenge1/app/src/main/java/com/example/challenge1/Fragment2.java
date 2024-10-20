package com.example.challenge1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Fragment2 extends Fragment {



    private ModelView viewModel;

    private Button button;

    public Fragment2() {
        // Required empty public constructor
    }


    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2, container, false);
        return view;

    }

    /**
     * Called when the fragment is created.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //find the view model
        viewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);
        //get animal from the view model
        Animal animal = viewModel.getAnimal().getValue();

        TextView editTextAnimalName = view.findViewById(R.id.edit_name_field);

        TextView editTextAnimalAge = view.findViewById(R.id.edit_age_field);

        TextView editTextAnimalOwner = view.findViewById(R.id.edit_owner_field);

        button = view.findViewById(R.id.save_button);

        // On click save changes and go back to fragment 1
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextAnimalName.getText().toString();
                String age = editTextAnimalAge.getText().toString();
                String owner = editTextAnimalOwner.getText().toString();
                checkChanges(name,age,owner,animal);

                Log.v("TAG","button clicked - Save");
                ((MainActivity) getActivity()).switchToFragment1();
            }
        });


    }

    /**
     * Saves the changes made by the user.
     */
    public void checkChanges(String name,String age, String Owner,Animal animal){
        if(!name.isEmpty()){
            animal.setName(name);
        }
        if(!age.isEmpty()){
            animal.setAge(age);
        }
        if(!Owner.isEmpty()){
            animal.setOwner(Owner);
        }
    }
}