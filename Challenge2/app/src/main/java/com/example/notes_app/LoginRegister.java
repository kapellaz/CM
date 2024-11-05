package com.example.notes_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;



public class LoginRegister extends Fragment {

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
        Auth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
    }
    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_login_register, container, false);
        usernameEditText = view.findViewById(R.id.editName);
        passwordEditText = view.findViewById(R.id.editPass);
        loginButton = view.findViewById(R.id.Login);
        registerButton = view.findViewById(R.id.Register);


        // login
        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                Auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) { //sucessful case
                                Log.v("Login","Login Sucessful");
                                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                ((MainActivity) requireActivity()).switchToNoteList();
                            } else { // login failed
                                Log.v("Login","Login failed: Incorrect email/password");
                                Toast.makeText(getContext(), "Login failed: Incorrect email/password", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else { //fields empty
                Log.v("Login","Please fill in all fields");
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Register
        registerButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                Auth.createUserWithEmailAndPassword(email, password) // function to create user using email and password
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.v("Register","Successfully registered!");
                                Toast.makeText(getContext(), "Successfully registered!", Toast.LENGTH_SHORT).show();
                                ((MainActivity) requireActivity()).switchToNoteList();
                            } else {
                                Log.v("Register","Login failed: Incorrect email/password");
                                Toast.makeText(getContext(), "Login failed: Incorrect email/password", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Log.v("Register","Please fill in all fields");
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


}