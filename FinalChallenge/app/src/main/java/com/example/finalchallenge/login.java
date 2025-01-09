package com.example.finalchallenge;


import android.annotation.SuppressLint;
import android.content.Context;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.example.finalchallenge.classes.viewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import com.example.finalchallenge.classes.Utilizador;

import java.util.HashMap;
import java.util.Map;


public class login extends Fragment {
    private viewModel modelview;
    private DatabaseHelper databaseHelper;
    TextView status;

    public login() {
        // Required empty public constructor
    }

    /**
     * Initializes the fragment and sets up essential components such as the ViewModel and database.
    */

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getContext());
        modelview = new ViewModelProvider(requireActivity()).get(viewModel.class);
    }


    /**
     * Called when the fragment is first created.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        EditText usernameEditText = view.findViewById(R.id.editName);
        EditText passwordEditText = view.findViewById(R.id.editPass);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);
        status = view.findViewById(R.id.statusText);


        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(username.isEmpty() || password.isEmpty()){
                status.setText("Please fill all fields!");
            }else {
                loginUser(username, password);
            }
        });


        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(username.isEmpty() || password.isEmpty()){
                status.setText("Please fill all fields!");
            }else {
                registerUser(username, password);
            }
        });
        return view;
    }


    /**
     * Method to log in to an account
     * @param username - Username account
     * @param password - Password account
     */

    private void loginUser(String username, String password) {

        // Check if the user exists in the local database
        Utilizador user = databaseHelper.loginUser(username, password);
        if (user != null) {

            user.setFirstTimeFragment(true);
            modelview.setUser(user);
            modelview.setIsFirstTime(false);

            Toast.makeText(getContext(), "Login Sucessful", Toast.LENGTH_SHORT).show();
            ((MainActivity) requireActivity()).switchMenu();


            return;
        }


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            Utilizador userprov = new Utilizador(document.getString("username"), document.getId());

                            databaseHelper.addUser(userprov,document.getString("password"));
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    userprov.setFirstTimeFragment(true);
                                    modelview.setUser(userprov);
                                    modelview.setIsFirstTime(true);
                                    Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                    ((MainActivity) requireActivity()).switchMenu();
                                }
                            });
                        } else {
                            status.setText("Invalid username or password!");


                        }
                    } else {
                        Toast.makeText(getContext(), "Error logging in", Toast.LENGTH_SHORT).show();
                    }
                });
            }


    /**
     * Method that registers the user, both in fb and in the local database
     * @param username - Username account
     * @param password - Password
     */


    private void registerUser(String username, String password) {
        //check if we have wifi
        // Check if we have internet connection
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isConnected()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show()
                );
                return;
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            status.setText("Invalid username or password!");

                        } else {

                            Map<String, Object> newUser = new HashMap<>();
                            newUser.put("username", username);
                            newUser.put("password", password);

                            db.collection("users")
                                    .add(newUser)
                                    .addOnSuccessListener(documentReference -> {
                                        //add user to local DB
                                        String docId = documentReference.getId();
                                        Utilizador userprov = new Utilizador(username, docId);
                                        databaseHelper.addUser(userprov, password);
                                        userprov.setFirstTimeFragment(true);
                                        modelview.setIsFirstTime(true);
                                        modelview.setUser(userprov);
                                        ((MainActivity) requireActivity()).switchMenu();
                                        Toast.makeText(getContext(), "Register Successful!!", Toast.LENGTH_SHORT).show();

                                    })
                                    .addOnFailureListener(e -> {
                                        requireActivity().runOnUiThread(() -> {
                                            Toast.makeText(getContext(), "Error adding user", Toast.LENGTH_SHORT).show();
                                        });
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "No wifi connection", Toast.LENGTH_SHORT).show();

                        }
                });
    }

}