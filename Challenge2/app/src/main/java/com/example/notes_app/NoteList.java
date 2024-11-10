package com.example.notes_app;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;


public class NoteList extends Fragment implements FileOperator.Callback {
    private FileOperator fileOperator;
    private ArrayAdapter arrayAdapter;
    public NoteSenderFireStore NoteSender = new NoteSenderFireStore(); // firestore
    private ModelView notesViewModel;
    private ListView listView;
    public ArrayList<Note> n = new ArrayList<>();
    public NoteList() {
        // Required empty public constructor
    }
    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        notesViewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);
        notesViewModel.loadNotes(getContext());


        View view =  inflater.inflate(R.layout.fragment_note_list, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        fileOperator = new FileOperator();


        listView = (ListView) view.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, Objects.requireNonNull(notesViewModel.getNotes().getValue()));
        listView.setAdapter(arrayAdapter);

        // click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Note note = (Note) listView.getItemAtPosition(i); // check the position of the note "clicked"
                notesViewModel.editData(note.getId_note()); // save last note to be edited
                Log.v("List Note","Edit Note " + note.getTitle());
                Toast.makeText(getContext(), "Editing Note : " + note.getTitle(), Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).switchToNoteEdit();
            }
        });
        // long click
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                longClick(i);
                return true;
            }
        });

        //
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_note_list, menu);
                requireActivity().setTitle("Notes");

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchItem.getActionView();
                assert searchView != null;
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
                // Set up the Create Note
                MenuItem createItem = menu.findItem(R.id.action_create);
                createItem.setOnMenuItemClickListener(item -> {
                    showCreateNoteDialog();
                    return true;
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return view;
    }



/**
    Secondary Functions (Pop-up's)
 */


// Function with Pop up - choose Erase or Change Title
    @SuppressLint("SetTextI18n")
    public void longClick(int pos){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Choose the option");

        // Setup Layout
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 30, 50, 30);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        buttonParams.setMargins(10, 0, 10, 0);

        // Button "Erase"
        Button eraseButton = new Button(getActivity());
        eraseButton.setText("Erase");
        eraseButton.setLayoutParams(buttonParams);
        layout.addView(eraseButton);

        // Button "Change Title"
        Button changeTitleButton = new Button(getActivity());
        changeTitleButton.setText("Change Title");
        changeTitleButton.setLayoutParams(buttonParams);
        layout.addView(changeTitleButton);

        dialog.setView(layout);
        AlertDialog dialog2 = dialog.create();
        dialog2.show();

        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note selectedNote = (Note) listView.getItemAtPosition(pos);

                RemoveNote(selectedNote.getId_note(),getContext());
                Log.v("List Note","Note " + selectedNote.getTitle() + " Deleted");
                Toast.makeText(getActivity(), "Note " + selectedNote.getTitle() +" Deleted", Toast.LENGTH_SHORT).show();
                dialog2.dismiss();
                arrayAdapter.notifyDataSetChanged();
            }
        });

        changeTitleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Note selectedNote = (Note) listView.getItemAtPosition(pos);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("New Title");

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
                            ChangeTitle(selectedNote.getId_note(),newTitle,getContext());
                            arrayAdapter.notifyDataSetChanged();
                            Log.v("List Note","Title Updated");
                            Toast.makeText(getActivity(), "Title Updated", Toast.LENGTH_SHORT).show();
                          } else {
                            Log.v("List Note","Title cannot be empty");
                            Toast.makeText(getActivity(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.v("List Note","Change Title Cancelled");
                        Toast.makeText(getActivity(), "Change Title Cancelled", Toast.LENGTH_SHORT).show();
                        dialogInterface.cancel(); // Dismiss the dialog
                    }
                });
                dialog.setView(layout);
                dialog.show();
                dialog2.dismiss();

            }
        });

    }

    /**
     *  Note Creation - Pop up with 2 fields (Only one is mandatory - title)
     *
     */

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

                String uuid = UUID.randomUUID().toString().replace("-", ""); // creating a random ID to the Note
                String id = uuid.substring(0, 16);

                //check if new title is not empty
                if (!newTitle.isEmpty()) {
                    addNote(new Note(id,newTitle,newDescription),getContext());
                    listView.requestLayout();
                    arrayAdapter.notifyDataSetChanged();
                    Log.v("List Note","New Note Created");
                    Toast.makeText(getActivity(), "New Note Created", Toast.LENGTH_SHORT).show();
                } else {
                    Log.v("List Note","Title cannot be empty");
                    Toast.makeText(getActivity(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.v("List Note","Cancel Note Creation");
                Toast.makeText(getActivity(), "Note Creation Cancelled", Toast.LENGTH_SHORT).show();
                dialogInterface.cancel(); // Dismiss the dialog
            }
        });

        dialog.show();
    }


    /**
     * Function to add a new note into arraylist (for another fragments use) and into the Internal Storage
     *
     */

    public void addNote(Note note, Context context) {
        ArrayList<Note> currentNotes = notesViewModel.getNotes().getValue();
        ArrayList<String> currentTitles = notesViewModel.getNotesTitle().getValue();
        Log.v("Model View","New Note Add: " + note.getTitle());
        if (currentNotes != null && currentTitles != null) {
            currentNotes.add(note);
            currentTitles.add(note.getTitle());
            notesViewModel.setNotesListLiveData(currentNotes);
            notesViewModel.setNoteTitlesListLiveData(currentTitles);

        }
        NoteSender.sendNoteToFireStoreIfConnected(context,note.getId_note(),note.getTitle(),note.getDescription());
        fileOperator.saveNotesToFile(context,currentNotes,this);
    }


    /**
     * Function to remove a note from arraylist (for another fragments use) and from the Internal Storage
     *
     */
    public void RemoveNote(String id, Context context) {
        Note aux = null;
        ArrayList<Note> currentNotes = notesViewModel.getNotes().getValue();
        ArrayList<String> currentTitles = notesViewModel.getNotesTitle().getValue();
        String title = null;
        assert currentNotes != null;

        for(Note n: currentNotes){ //find object with same id
            if (n.getId_note().equals(id)){
                aux = n;
                title = n.getTitle();
            }
        }
        Log.v("Model View","Remote Note: " + title);
        if (currentTitles != null) { // remove from the arraylists
            currentNotes.remove(aux);
            currentTitles.remove(title);
            notesViewModel.setNotesListLiveData(currentNotes);
            notesViewModel.setNoteTitlesListLiveData(currentTitles);
        }
        NoteSender.deleteNote(context,id); // Delete note from FireStore Database
        fileOperator.saveNotesToFile(context,currentNotes,this);
    }


    /**
     * Function to update a note title into arraylist (for another fragments use) and into the Internal Storage
     *
     */

    public void ChangeTitle(String id, String newTitle,Context context) {
        ArrayList<Note> currentNotes = notesViewModel.getNotes().getValue();
        ArrayList<String> currentTitles = notesViewModel.getNotesTitle().getValue();
        assert currentNotes != null;
        int counter = 0;
        int cert = 0;
        for(Note n: currentNotes){ // Send changes to FireStore Database
            counter += 1;
            if (n.getId_note().equals(id)){
                cert = counter;
                Log.v("Model View","Change Title Note : " + n.getTitle() + " to " + newTitle);
                NoteSender.updateNoteToFireStoreIfConnected(context,id,newTitle,n.getDescription());
                n.setTitle(newTitle);
            }
        }
        if (currentTitles != null) {  // update Arraylists
            currentTitles.set(cert-1,newTitle);
            notesViewModel.setNotesListLiveData(currentNotes);
            notesViewModel.setNoteTitlesListLiveData(currentTitles);
        }
        fileOperator.saveNotesToFile(context,currentNotes,this);
    }

    /**
     * CallBack
     */

    @Override
    public void onCompleteRead(ArrayList<Note> result) {
       Log.v("SAVE_Create/Remove","Notes: " + result.toString() + " Saved into Internal Storage");
    }

}