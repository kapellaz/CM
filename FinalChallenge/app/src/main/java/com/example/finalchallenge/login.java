package com.example.finalchallenge;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalchallenge.classes.viewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import com.example.finalchallenge.classes.Utilizador;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class login extends Fragment {
    private FirebaseAuth mAuth;
    private viewModel modelview;
    private Utilizador user;
    private DatabaseHelper databaseHelper;
    TextView status;

    public login() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
        user = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        EditText usernameEditText = view.findViewById(R.id.editName);
        EditText passwordEditText = view.findViewById(R.id.editPass);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);
        status = view.findViewById(R.id.statusText);
        mAuth = FirebaseAuth.getInstance();

        // login
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(username.isEmpty() || password.isEmpty()){
                status.setText("Please fill all fields!");
            }else {
                loginUser(username, password);
            }
        });

        // Register
        registerButton.setOnClickListener(v -> {
            //mudar o screen
        });
        return view;
    }

    // Método para realizar login
    private void loginUser(String username, String password) {
        //procurar na DB local
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Check if the user exists in the local database
                Utilizador user = databaseHelper.loginUser(username, password);
                if (user != null) {
                    // User found in local database, proceed with login
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            modelview.setUser(user);
                            Toast.makeText(getContext(), "Login Sucessful", Toast.LENGTH_SHORT).show();
                            ((MainActivity) requireActivity()).switchMenu();
                        }
                    });
                    return; // Exit the method if user is found locally
                }

                // User not found locally, check Firebase
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .whereEqualTo("username", username)
                        .whereEqualTo("password", password)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    //trabalhar com o documento obtido
                                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                    //colocar o documento obtido em um utilizador
                                    Utilizador userprov = new Utilizador(document.getString("username"), document.getId());

                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            modelview.setUser(userprov);
                                            Toast.makeText(getContext(), "Login Sucessful", Toast.LENGTH_SHORT).show();
                                            ((MainActivity) requireActivity()).switchMenu();
                                        }
                                    });
                                    //add user to local DB
                                    databaseHelper.addUser(userprov,document.getString("password"));
                                } else {
                                    // User not found in Firebase
                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                // Error retrieving data from Firebase
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Error logging in", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }
        });
    }

    // Método para registrar usuário
    private void registerUser(String email, String password) {
        //ver se já existe na FB

        //Escrever na DB local

        //Escrever na FB
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