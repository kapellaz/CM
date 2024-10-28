package com.example.notes_app;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private LoginRegister login;
    private NoteList List;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        login = new LoginRegister();
        List = new NoteList();

        ModelView modelView = new ViewModelProvider(this).get(ModelView.class);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, login);
        ft.commit();

    }
    public void switchToNoteList() {
        Log.v("TAG", "switch to fragment 2");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new NoteList())
                .addToBackStack(null)
                .commit();
    }

}