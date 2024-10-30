package com.example.notes_app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class NoteEdit extends Fragment {
    private TextView textView;
    private EditText editText;
    private ModelView notesViewModel;
    private ArrayAdapter arrayAdapter;


    public NoteEdit() {
        // Required empty public constructor
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_note, menu); // Inflate your menu
        super.onCreateOptionsMenu(menu, inflater);

        // button to save the note

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("Item ID: " + item.getItemId());
        if (item.getItemId() == R.id.action_save) {
            saveNote();
            return true;
        } else if (item.getItemId() == R.id.action_cancel) {
            cancelEdit();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void saveNote() {
        String newDescription = editText.getText().toString().trim();
        System.out.println("New Description: " + newDescription);
        notesViewModel.updateNoteDescription(newDescription, getContext());
        Toast.makeText(getActivity(), "Note Saved", Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed(); // Go back to the previous screen
    }

    private void cancelEdit() {
        Toast.makeText(getActivity(), "Edit Canceled", Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        requireActivity().setTitle("Edit Note");

        notesViewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);
        notesViewModel.loadNotes(getContext());

        Note note = notesViewModel.getEditNote().getValue();
        String title = note.getTitle();
        String description = note.getDescription();

        textView = view.findViewById(R.id.note_title);
        textView.setText("Name: " + title);
        editText = view.findViewById(R.id.note_description);
        editText.setText(description);

        return view;
    }

}
