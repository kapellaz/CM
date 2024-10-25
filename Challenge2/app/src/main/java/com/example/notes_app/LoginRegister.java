package com.example.notes_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;


public class LoginRegister extends Fragment {
    private ModelView modelView;

    public LoginRegister() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //find the view model
        modelView = new ViewModelProvider(requireActivity()).get(ModelView.class);

        View view = inflater.inflate(R.layout.fragment_login_register, container, false);




        return view;
    }


    // Called when the fragment is created
    public void onViewCreated(View view, Bundle savedInstanceState) {


    }

}