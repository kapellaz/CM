package com.example.finalchallenge;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //inicializar o login
        //login_register login = new login_register();



        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new menu_principal(), "LOGIN")
                .commit();
        }


    public void switchMenu() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new menu_principal(),"MENU")
                .addToBackStack(null)
                .commit();
    }

    public void switchtoStats() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new Exercise_List(),"Stats")
                .addToBackStack(null)
                .commit();
    }


    public void switchtoEdit() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new train_edit_exercise(),"Train_Edit")
                .addToBackStack(null)
                .commit();
    }


    public void switchTrain() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new train_list(),"Training")
                .addToBackStack(null)
                .commit();
    }
    public void switchLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new login_register(),"Login")
                .addToBackStack(null)
                .commit();
    }

    public void switchDetailsTrain() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new train_detail(),"Train Details")
                .addToBackStack(null)
                .commit();
    }

    public void switchDetailsExercise() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new exercise_detail(),"Train Details")
                .addToBackStack(null)
                .commit();
    }

}

