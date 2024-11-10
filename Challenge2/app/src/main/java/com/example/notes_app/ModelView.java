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
import java.io.InputStreamReader;
import java.util.ArrayList;


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



    public void setNotesListLiveData(ArrayList<Note> notesListLiveData) {
        this.notesListLiveData.setValue(notesListLiveData);
    }

    public void setNoteTitlesListLiveData(ArrayList<String> noteTitlesListLiveData) {
        this.noteTitlesListLiveData.setValue(noteTitlesListLiveData);
    }



    public void loadNotes(Context context) {

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

}