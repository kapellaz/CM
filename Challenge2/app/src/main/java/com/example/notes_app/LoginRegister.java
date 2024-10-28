package com.example.notes_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;


public class LoginRegister extends Fragment {
    private ModelView modelView;
    private FirebaseAuth Auth;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    public LoginRegister() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Auth = FirebaseAuth.getInstance(); // Inicializar Firebase Auth
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
        usernameEditText = view.findViewById(R.id.editName);
        passwordEditText = view.findViewById(R.id.editPass);
        loginButton = view.findViewById(R.id.Login);
        registerButton = view.findViewById(R.id.Register);

        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                Auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                                ((MainActivity) getActivity()).switchToNoteList();
                            } else {
                                Toast.makeText(getContext(), "Falha no login: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });


        registerButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                Auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Registrado com sucesso!", Toast.LENGTH_SHORT).show();
                                ((MainActivity) getActivity()).switchToNoteList();
                            } else {
                                Toast.makeText(getContext(), "Falha no registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });



        return view;
    }


    // Called when the fragment is created
    public void onViewCreated(View view, Bundle savedInstanceState) {


    }

}