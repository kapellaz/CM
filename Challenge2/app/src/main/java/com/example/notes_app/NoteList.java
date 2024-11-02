package com.example.notes_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class NoteList extends Fragment {

    private ArrayAdapter arrayAdapter;

    private ModelView notesViewModel;
    private ListView listView;
    public NoteList() {
        // Required empty public constructor
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_note_list, menu); // Inflate your menu
        requireActivity().setTitle("Notes");
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Type Here");
        // Set up the search view listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter notes based on search query
                arrayAdapter.getFilter().filter(newText);
                return true;
            }
        });

        MenuItem createItem = menu.findItem(R.id.action_create);
        createItem.setOnMenuItemClickListener(item -> {
            showCreateNoteDialog(); // Show the dialog when clicked
            return true;
        });

    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment

        notesViewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);
        notesViewModel.loadNotes(getContext());

        View view =  inflater.inflate(R.layout.fragment_note_list, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);



        listView = (ListView) view.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,notesViewModel.getNotes().getValue());
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Note note = (Note) listView.getItemAtPosition(i);

                notesViewModel.editData(note.getTitle());

                ((MainActivity) getActivity()).switchToNoteEdit();

            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                longClick(i);
                return true;
            }
        })
        ;return view;
    }





// secundary Functions
    public void longClick(int pos){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Choose the option");


        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 30, 50, 30);


        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0, // Largura igual para ambos os botões, ocupando o espaço disponível
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f // Peso igual para dividir o espaço horizontalmente
        );
        buttonParams.setMargins(10, 0, 10, 0); // Margem horizontal entre os botões

// Botão "Erase"
        Button eraseButton = new Button(getActivity());
        eraseButton.setText("Erase");
        eraseButton.setLayoutParams(buttonParams); // Aplica parâmetros de layout
        layout.addView(eraseButton);

// Botão "Change Title"
        Button changeTitleButton = new Button(getActivity());
        changeTitleButton.setText("Change Title");
        changeTitleButton.setLayoutParams(buttonParams); // Aplica parâmetros de layout
        layout.addView(changeTitleButton);

// Adiciona o layout ao diálogo e exibe-o
        dialog.setView(layout);

        AlertDialog dialog2 = dialog.create();
        dialog2.show();


        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note selectedNote = (Note) listView.getItemAtPosition(pos);

                notesViewModel.RemoveNote(selectedNote.getId_note(),getContext());
                Toast.makeText(getActivity(), "Ação de apagar executada!", Toast.LENGTH_SHORT).show();
                dialog2.dismiss();
                arrayAdapter.notifyDataSetChanged();
            }
        });

        changeTitleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note selectedNote = (Note) listView.getItemAtPosition(pos);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("New Note");

                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText noteTitleInput = new EditText(getActivity());
                noteTitleInput.setInputType(InputType.TYPE_CLASS_TEXT);
                noteTitleInput.setHint("Enter note title");
                layout.addView(noteTitleInput);

                dialog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newTitle = noteTitleInput.getText().toString().trim();

                        // Check if the title already exists
                        if (!newTitle.isEmpty()) {
                            listView.requestLayout();
                            notesViewModel.ChangeTitle(selectedNote.getId_note(),newTitle,getContext());
                            arrayAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Title Updated", Toast.LENGTH_SHORT).show();
                          } else {
                            Toast.makeText(getActivity(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel(); // Dismiss the dialog
                    }
                });
                dialog.setView(layout);
                dialog.show();

               // Toast.makeText(getActivity(), "Ação de apagar executada!", Toast.LENGTH_SHORT).show();
                dialog2.dismiss();

            }
        });

    }
// Note Creation
    private void showCreateNoteDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("New Note");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText noteTitleInput = new EditText(getActivity());
        noteTitleInput.setInputType(InputType.TYPE_CLASS_TEXT);
        noteTitleInput.setHint("Enter note title");
        layout.addView(noteTitleInput);

        final EditText noteDescriptionInput = new EditText(getActivity());
        noteDescriptionInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteDescriptionInput.setHint("Enter note description");
        noteDescriptionInput.setMinLines(3);
        layout.addView(noteDescriptionInput);
        dialog.setView(layout);

        dialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String newTitle = noteTitleInput.getText().toString().trim();
                String newDescription = noteDescriptionInput.getText().toString().trim();
                // Check if the title already exists
                String id = UUID.randomUUID().toString().replace("-", "");
                String uuid = UUID.randomUUID().toString().replace("-", "");
                id = uuid.substring(0, 16);
                System.out.println("NOVO ID: (SIIIU)) "+ id);
                if (!newTitle.isEmpty()) {

                    notesViewModel.addNote(new Note(id,newTitle,newDescription),getContext());
                    listView.requestLayout();
                    Toast.makeText(getActivity(), "New Note Created", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel(); // Dismiss the dialog
            }
        });

        dialog.show();
    }


}