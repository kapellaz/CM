package com.example.notes_app;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Objects;

public class ModelView extends ViewModel{
    private static final String FILE_NAME = "notes.txt";

    public NoteSenderFireStore NoteSender = new NoteSenderFireStore(); // firestore

    private MutableLiveData<ArrayList<Note>> notesListLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> noteTitlesListLiveData = new MutableLiveData<>();

    private MutableLiveData<Note> editData = new MutableLiveData<>();


    private ArrayList<Note> notes = new ArrayList<>();
    private ArrayList<String> notesTitle = new ArrayList<>();

    private ArrayList<String> ids = new ArrayList<>();

    public void editData(String id){
        ArrayList<Note> currentNotes = notesListLiveData.getValue();
        assert currentNotes != null;
        for(Note n: currentNotes){
            if (n.getId_note().equals(id)){
                editData.setValue(n);
            }
        }
    }

    public LiveData<Note> getEditNote() {
        return editData;
    }

    public LiveData<ArrayList<Note>> getNotes() {
        return notesListLiveData;
    }

    public LiveData<ArrayList<String>> getNotesTitle() {
        return noteTitlesListLiveData;
    }

    public void setNoteSender(NoteSenderFireStore noteSender) {
        NoteSender = noteSender;
    }

    public void setNotesListLiveData(ArrayList<Note> notesListLiveData) {
        this.notesListLiveData.setValue(notesListLiveData);
    }

    public void setNoteTitlesListLiveData(ArrayList<String> noteTitlesListLiveData) {
        this.noteTitlesListLiveData.setValue(noteTitlesListLiveData);
    }

    public void setEditData(MutableLiveData<Note> editData) {
        this.editData = editData;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

    public void setNotesTitle(ArrayList<String> notesTitle) {
        this.notesTitle = notesTitle;
    }

    public void setIds(ArrayList<String> ids) {
        this.ids = ids;
    }

    public void loadNotes(Context context) {
      //  clearFileContent(context);
        notes.clear();
        notesTitle.clear();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();

            // Parse JSON content
            JSONArray jsonArray = new JSONArray(content.toString());
            Log.v("Model View","Loading Notes");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject noteObject = jsonArray.getJSONObject(i);
                String id = noteObject.getString("id");
                String title = noteObject.getString("title");
                String description = noteObject.getString("description");
                ids.add(id);
                notesTitle.add(title);
                notes.add(new Note(id,title, description));
                NoteSender.syncNotesToFireStore(context,id,title,description);
            }
            NoteSender.deleteNotesNotInFile(context,ids);
        } catch (Exception e) {
            e.printStackTrace();
        }

        notesListLiveData.setValue(notes);
        noteTitlesListLiveData.setValue(notesTitle);
    }




    // uso para dar debug (apagar tudo da INternal Storage)
    public static void clearFileContent(Context context) {
        String fileName = "notes.txt";
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write("".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Function to save all notes into Internal Storage
     *
     */

    public void saveNotesToFile(Context context) {
        try {
            JSONArray jsonArray = new JSONArray();
            Log.v("Model View","Saving Notes");
            for (Note note : notes) {

                JSONObject noteObject = new JSONObject();
                noteObject.put("id",note.getId_note());
                noteObject.put("title", note.getTitle());
                noteObject.put("description", note.getDescription());
                jsonArray.put(noteObject);
            }
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(jsonArray.toString());
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Function to add a new note into arraylist (for another fragments use) and into the Internal Storage
     *
     */

    public void addNote(Note note, Context context) {
        ArrayList<Note> currentNotes = notesListLiveData.getValue();
        ArrayList<String> currentTitles = noteTitlesListLiveData.getValue();
        Log.v("Model View","New Note Add: " + note.getTitle());
        if (currentNotes != null && currentTitles != null) {
            currentNotes.add(note);
            currentTitles.add(note.getTitle());
            notesListLiveData.setValue(currentNotes);
            noteTitlesListLiveData.setValue(currentTitles);
        }
        NoteSender.sendNoteToFireStoreIfConnected(context,note.getId_note(),note.getTitle(),note.getDescription());
        saveNotesToFile(context); // Save to file
    }


    /**
     * Function to remove a note from arraylist (for another fragments use) and from the Internal Storage
     *
     */
    public void RemoveNote(String id, Context context) {
        Note aux = null;

        ArrayList<Note> currentNotes = notesListLiveData.getValue();
        ArrayList<String> currentTitles = noteTitlesListLiveData.getValue();
        String title = null;
        assert currentNotes != null;

        for(Note n: currentNotes){ //find object with same id
            if (n.getId_note().equals(id)){
                aux = n;
                title = n.getTitle();
            }
        }
        Log.v("Model View","New Note Add: " + title);
        if (currentTitles != null) { // remove from the arraylists
            currentNotes.remove(aux);
            currentTitles.remove(title);
            notesListLiveData.setValue(currentNotes);
            noteTitlesListLiveData.setValue(currentTitles);
        }
        NoteSender.deleteNote(context,id); // Delete note from FireStore Database
        saveNotesToFile(context); // Save to file
    }


    /**
     * Function to update a note title into arraylist (for another fragments use) and into the Internal Storage
     *
     */

    public void ChangeTitle(String id, String newTitle,Context context) {
        ArrayList<Note> currentNotes = notesListLiveData.getValue();
        ArrayList<String> currentTitles = noteTitlesListLiveData.getValue();

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
            notesListLiveData.setValue(currentNotes);
            noteTitlesListLiveData.setValue(currentTitles);
        }
        saveNotesToFile(context);
    }

    /**
     * Function to update a description of a note and update FireStore Database
     *
     *
     */
    public void updateNoteDescription(String newDescription, Context context) {
        Note note = editData.getValue();

        for (Note n : Objects.requireNonNull(notesListLiveData.getValue())) {
            assert note != null;
            if (n.getId_note().equals(note.getId_note())) {
                Log.v("Model View","Change Description Note : " + n.getDescription() + " to " + newDescription);
                NoteSender.updateNoteToFireStoreIfConnected(context,n.getId_note(),n.getTitle(),newDescription);
                n.setDescription(newDescription);
            }
        }
        saveNotesToFile(context);
    }
}