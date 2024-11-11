package com.example.notes_app;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;


public class LoginRegister extends Fragment {


    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private Map<String,String> accounts = new HashMap<>();
    public LoginRegister() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoadAccounts(getContext());
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
            String name = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(!name.isEmpty() && !password.isEmpty()) {
                if(LoginCheck(name,password)){
                    Log.v("Login", "Login Sucessful");
                    Toast.makeText(getContext(), "Login Sucessful", Toast.LENGTH_SHORT).show();
                    ((MainActivity) requireActivity()).switchToNoteList();
                }else {
                    Log.v("Login", "Login Failed");
                    Toast.makeText(getContext(), "Login Failed: Incorrect name/Password", Toast.LENGTH_SHORT).show();
                }
            }else{
                Log.v("Login","Please fill in all fields");
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();

            }

        });

        // Register
        registerButton.setOnClickListener(v -> {
            String name = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!name.isEmpty() && !password.isEmpty()) {
                if(checkAvailable(name)){ // if already exists any account with that name
                    accounts.put(name,password);
                    SaveAccountsToFile(getContext());
                    Log.v("Register","Register Sucessful");
                    Toast.makeText(getContext(), "Register Sucessful", Toast.LENGTH_SHORT).show();
                    ((MainActivity) requireActivity()).switchToNoteList();
                }else{
                    Log.v("Register","Register Sucessful");
                    Toast.makeText(getContext(), "Register Failed: Name/Password Incorrect", Toast.LENGTH_SHORT).show();
                }


            } else {
                Log.v("Register","Please fill in all fields");
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Method to load all acounts
     */
    public void LoadAccounts(Context context) {

        try {
            FileInputStream fis = context.openFileInput("login.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();

            // Parse JSON content
            JSONArray jsonArray = new JSONArray(content.toString());
            Log.v("Login","Loading accounts");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject noteObject = jsonArray.getJSONObject(i);
                String name = noteObject.getString("name");
                String pass = noteObject.getString("pass");
                accounts.put(name,pass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Check if exists any account with same name
     */

    public boolean checkAvailable(String name){
        for (Map.Entry<String, String> entry : accounts.entrySet()) {
            if (entry.getKey().equals(name)){
                  return false;
            }
        }
        return true;
    }

    /**
     * Login Function
     */
    public boolean LoginCheck(String name, String pass){
        for (Map.Entry<String, String> entry : accounts.entrySet()) {
            if (entry.getKey().equals(name) && entry.getValue().equals(pass)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Method to save all accounts into a File (login.txt)
     */


    public void SaveAccountsToFile(Context context) {
        try {
            JSONArray jsonArray = new JSONArray();
            Log.v("Model View","Saving Notes");
            for (Map.Entry<String, String> entry : accounts.entrySet()) {
                JSONObject noteObject = new JSONObject();
                noteObject.put("name",entry.getKey());
                noteObject.put("pass",entry.getValue());
                jsonArray.put(noteObject);
            }

            FileOutputStream fos = context.openFileOutput("login.txt", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(jsonArray.toString());
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}