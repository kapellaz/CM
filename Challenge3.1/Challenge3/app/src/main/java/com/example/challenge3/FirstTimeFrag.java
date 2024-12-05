package com.example.challenge3;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FirstTimeFrag extends Fragment {



    public FirstTimeFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_first_time, container, false);


        EditText usernameInput = view.findViewById(R.id.username_input);
        Button submitButton = view.findViewById(R.id.submit_button);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = usernameInput.getText().toString().trim();

                // Verifica se o campo está vazio
                if (!username.isEmpty()) {
                    ((MainActivity) requireActivity()).switchToChatList(username);
                    Toast.makeText(getActivity(), "Username: " + username, Toast.LENGTH_SHORT).show();
                } else {
                    // Mostra uma mensagem se o campo estiver vazio
                    Toast.makeText(getActivity(), "Please enter a username", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}