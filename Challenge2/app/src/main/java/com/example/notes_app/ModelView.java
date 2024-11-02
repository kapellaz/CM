package com.example.notes_app;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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

    public NoteSenderFireStore NoteSender = new NoteSenderFireStore();

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
                Log.v("ASHHAD","AHS");
                editData.setValue(n);
            }
        }
        System.out.println(editData.getValue().getTitle());
    }
    public LiveData<Note> getEditNote() {
        return editData;
    }

    public LiveData<ArrayList<Note>> getNotes() {
        return notesListLiveData;
    }

    public LiveData<ArrayList<String>> getNotesbyTitle() {
        return noteTitlesListLiveData;
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
            Log.v("AHAH","TA A GRAVAR 5");
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
    public void saveNotesToFile(Context context) {
        try {
            JSONArray jsonArray = new JSONArray();

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





    // new Note, save into Internal Storage
    public void addNote(Note note, Context context) {
        ArrayList<Note> currentNotes = notesListLiveData.getValue();
        ArrayList<String> currentTitles = noteTitlesListLiveData.getValue();
        Log.v("AHAH --- AADDDDDD",note.getId_note());
        if (currentNotes != null && currentTitles != null) {
            currentNotes.add(note);
            currentTitles.add(note.getTitle());
            //notes.add(note);
            notesListLiveData.setValue(currentNotes);
            noteTitlesListLiveData.setValue(currentTitles);
        }

        NoteSender.sendNoteToFireStoreIfConnected(context,note.getId_note(),note.getTitle(),note.getDescription());
        saveNotesToFile(context); // Save to file
    }

    // new Note, save into Internal Storage
    public void RemoveNote(String id, Context context) {
        Note aux = null;

        ArrayList<Note> currentNotes = notesListLiveData.getValue();
        ArrayList<String> currentTitles = noteTitlesListLiveData.getValue();
        String title = null;
        assert currentNotes != null;
        for(Note n: currentNotes){
            if (n.getId_note().equals(id)){
                aux = n;
                title = n.getTitle();
            }
        }
        if (currentTitles != null) {
            currentNotes.remove(aux);
            currentTitles.remove(title);
            notesListLiveData.setValue(currentNotes);
            noteTitlesListLiveData.setValue(currentTitles);
        }
        NoteSender.deleteNoteByTitle(context,id);
        saveNotesToFile(context); // Save to file
    }
    public void ChangeTitle(String id, String newTitle,Context context) {


        ArrayList<Note> currentNotes = notesListLiveData.getValue();
        ArrayList<String> currentTitles = noteTitlesListLiveData.getValue();

        assert currentNotes != null;
        int counter = 0;
        int cert = 0;
        for(Note n: currentNotes){
            counter += 1;
            if (n.getId_note().equals(id)){
                cert = counter;
                System.out.println("ALO");
                NoteSender.updateNoteToFireStoreIfConnected(context,id,newTitle,n.getDescription());
                n.setTitle(newTitle);
            }
        }


        if (currentTitles != null) {
            currentTitles.set(cert-1,newTitle);
            notesListLiveData.setValue(currentNotes);

            noteTitlesListLiveData.setValue(currentTitles);
        }
        saveNotesToFile(context);


    }


    public void updateNoteDescription(String newDescription, Context context) {
        Note note = editData.getValue();
        System.out.println("Note: " + note.getId_note());
        for (Note n : Objects.requireNonNull(notesListLiveData.getValue())) {
            if (n.getId_note().equals(note.getId_note())) {
                NoteSender.updateNoteToFireStoreIfConnected(context,n.getId_note(),n.getTitle(),newDescription);
                System.out.println("Old Description: " + n.getDescription());
                n.setDescription(newDescription);
                System.out.println("New Description: " + n.getDescription());
            }
        }
        saveNotesToFile(context);
    }
}