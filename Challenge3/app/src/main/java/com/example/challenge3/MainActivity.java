package com.example.challenge3;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {


    private FirstTimeFrag login;
    private static final String TAG_NOTE_EDIT = "NOTE_EDIT";
    private static final String TAG_NOTE_LIST = "NOTE_LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        login = new FirstTimeFrag();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, login);
        ft.commit();

    }


    /**
     * Switch Fragment - First Fragment to Chat List
     */
    public void switchToChatList() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new ChatList(),TAG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();
    }





}