package com.example.finalchallenge;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
;import com.example.finalchallenge.classes.Utilizador;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //inicializar o login
        login login = new login();





    if (savedInstanceState != null) {
        // Retrieve the saved fragment tag
        String fragmentTag = savedInstanceState.getString("currentFragmentTag");
        if (fragmentTag != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (fragment == null) {
                // Fragment is not currently in the manager, recreate it based on the tag
                if (fragmentTag.equals("MENU")) {
                    fragment = new menu_principal();
                } else if (fragmentTag.equals("Stats")) {
                    fragment = new Exercise_List();
                } else if (fragmentTag.equals("Train_Edit")) {
                    fragment = new train_edit_exercise();
                } else if (fragmentTag.equals("Training")) {
                    fragment = new train_list();
                } else if (fragmentTag.equals("Login")) {
                    fragment = new login();
                } else if (fragmentTag.equals("Train Details")) {
                    fragment = new train_detail();
                } else if (fragmentTag.equals("FriendsList")) {
                    fragment = new FriendsList();
                }  else if (fragmentTag.equals("Train Details")) {
                    fragment = new exercise_detail();
                } else {
                    fragment = new login(); // Load your default fragment if needed
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, fragment, fragmentTag)
                        .commit();
            }
        }
    } else {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, login);
        ft.commit();
    }
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
                .replace(R.id.main, new login(),"Login")
                .addToBackStack(null)
                .commit();
    }

    public void switchDetailsTrain() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new train_detail(),"Train Details")
                .addToBackStack(null)
                .commit();
    }

    public void switchtoFriends(){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new FriendsList(),"FriendsList")
                .addToBackStack(null)
                .commit();
    }

    public void switchtoFriendProfile(Utilizador friend) {
        FriendProfile friendProfile = new FriendProfile();
        Bundle args = new Bundle();
        args.putParcelable("friend", friend); // Pass the Parcelable object
        friendProfile.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, friendProfile, "FriendProfile")
                .addToBackStack(null)
                .commit();
    }

    public void switchDetailsExercise() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new exercise_detail(),"Train Details")
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main);
        if (currentFragment != null) {
            Log.v("DADA","Save" + currentFragment.getTag());
            outState.putString("currentFragmentTag", currentFragment.getTag());
        }
    }


}

