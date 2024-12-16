package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.w3c.dom.Text;

public class login_register extends Fragment {
    private FirebaseAuth mAuth;
    TextView status;

    public login_register() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_register, container, false);
        EditText usernameEditText = view.findViewById(R.id.editName);
        EditText passwordEditText = view.findViewById(R.id.editPass);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);
        status = view.findViewById(R.id.statusText);
        mAuth = FirebaseAuth.getInstance();


        // login
        loginButton.setOnClickListener(v -> {
            String name = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(name.isEmpty() || password.isEmpty()){
                status.setText("Please fill all fields!");
            }else {
                loginUser(name, password);

            }


        });

        // Register
        registerButton.setOnClickListener(v -> {
            String name = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(name.isEmpty() || password.isEmpty()){
                status.setText("Please fill all fields!");
            }else {
                if(password.length() < 6){
                    status.setText("Password should have 6 characteres!");
                }else {
                    registerUser(name, password);
                }

            }
        });

        return view;

    }

    // Método para realizar login
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getContext(), "Login Sucessful", Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).switchMenu();

                    } else {
                        status.setText("Name or password incorrect!");
                    }
                });
    }

    // Método para registrar usuário
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getContext(), "Register Sucessful", Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).switchMenu();

                    } else {
                        status.setText("Registration failed: " + task.getException().getMessage());
                    }
                });
    }
}