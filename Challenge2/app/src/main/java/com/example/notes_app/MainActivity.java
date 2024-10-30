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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private LoginRegister login;
    private NoteList List;
    private static final String TAG_NOTE_EDIT = "NOTE_EDIT";
    private static final String TAG_NOTE_LIST = "NOTE_LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        login = new LoginRegister();
        List = new NoteList();

        ModelView modelView = new ViewModelProvider(this).get(ModelView.class);

        if (savedInstanceState != null) {
            // Retrieve the saved fragment tag
            String fragmentTag = savedInstanceState.getString("currentFragmentTag");

            if (fragmentTag != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

                if (fragment == null) {
                    // Fragment is not currently in the manager, recreate it based on the tag
                    if (fragmentTag.equals(TAG_NOTE_EDIT)) {
                        fragment = new NoteEdit();
                    } else if (fragmentTag.equals(TAG_NOTE_LIST )) {
                        fragment = new NoteList();
                    } else {
                        fragment = new LoginRegister(); // Load your default fragment if needed
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main, fragment, fragmentTag)
                            .commit();
                }
            }
        } else {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main, List);
            ft.commit();
        }
    }


    public void switchToNoteList() {
        Log.v("TAG", "switch to fragment 2");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new NoteList(),TAG_NOTE_LIST)
                .addToBackStack(null)
                .commit();
    }
    public void switchToNoteEdit() {
        Log.v("TAG", "switch to fragment Edit");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new NoteEdit(),TAG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main);
        if (currentFragment != null) {
            Log.v("DADA","Save" + currentFragment.getTag());
            outState.putString("currentFragmentTag", currentFragment.getTag());
        }
    }

}