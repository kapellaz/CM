package com.example.notes_app;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;

public class NoteEdit extends Fragment {
    private TextView textView;
    private EditText editText;
    private ModelView notesViewModel;

    public NoteEdit() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =  inflater.inflate(R.layout.fragment_note, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);




        notesViewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);
        notesViewModel.loadNotes(getContext());


        Note note = notesViewModel.getEditNote().getValue();
        System.out.println(note);
        String title = notesViewModel.getEditNote().getValue().getTitle();
        String description = notesViewModel.getEditNote().getValue().getDescription();

        textView = (TextView) view.findViewById(R.id.note_title);
        textView.setText("Name: "+title);
        editText = (EditText) view.findViewById(R.id.note_description);
        editText.setText(description);

        ;return view;
    }

}
