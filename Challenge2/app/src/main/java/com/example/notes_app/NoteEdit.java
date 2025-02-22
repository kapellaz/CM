package com.example.notes_app;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;


public class NoteEdit extends Fragment implements FileOperator.Callback {
    private EditText editText;
    private ModelView notesViewModel;
    public NoteSenderFireStore NoteSender = new NoteSenderFireStore();
    private FileOperator fileOperator;

    public NoteEdit() {
        // Required empty public constructor
    }

    /**
     * Function to Save Edit and go back to the previous screen
     */

    private void saveNote() {
        String newDescription = editText.getText().toString().trim();
        Log.v("Edit Note","Note Saved");
        updateNoteDescription(newDescription, getContext());
        Toast.makeText(getActivity(), "Note Saved", Toast.LENGTH_SHORT).show();
        requireActivity().getOnBackPressedDispatcher().onBackPressed(); // Go back to the previous screen
    }

    /**
     * Function to cancel Edit and go back to the previous screen
     */

    private void cancelEdit() {
        Log.v("Edit Note","Edit Canceled");
        Toast.makeText(getActivity(), "Edit Canceled", Toast.LENGTH_SHORT).show();
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    /**
     * Called when the fragment is first created.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_note, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        requireActivity().setTitle("Edit Note");
        fileOperator = new FileOperator();

        notesViewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);


        Note note = notesViewModel.getEditNote().getValue();
        assert note != null;
        String title = note.getTitle();
        String description = note.getDescription();

        TextView textView = view.findViewById(R.id.note_title);
        textView.setText("Title: " + title);
        editText = view.findViewById(R.id.note_description);
        editText.setText(description);

        // Adding the menu provider to handle menu creation and item selection (Save or Cancel edit)
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_note, menu); // Inflate your menu
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_save) { // if save button was clicked
                    saveNote();
                    return true;
                } else if (item.getItemId() == R.id.action_cancel) { // if cancel button was clicked
                    cancelEdit();
                    return true;
                } else {
                    return false;
                }
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return view;
    }



    /**
     * Function to update a description of a note and update FireStore Database
     *
     *
     */
    public void updateNoteDescription(String newDescription, Context context) {

        Note note = notesViewModel.getEditNote().getValue();

        for (Note n : Objects.requireNonNull(notesViewModel.getNotes().getValue())) {
            assert note != null;
            if (n.getId_note().equals(note.getId_note())) {
                Log.v("Model View", "Change Description Note : " + n.getDescription() + " to " + newDescription);
                NoteSender.updateNoteToFireStoreIfConnected(context, n.getId_note(), n.getTitle(), newDescription);
                n.setDescription(newDescription);
            }
        }
        fileOperator.saveNotesToFile(context,notesViewModel.getNotes().getValue(), this);
    }

    /**
     * Callback
     */
    @Override
    public void onCompleteRead(ArrayList<Note> result) {
        Log.v("SAVE_Update Description","Notes: " + result.toString() + " Saved into Internal Storage");

    }
}
