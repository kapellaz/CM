package com.example.notes_app;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
public class ModelView extends ViewModel{
    private static final String FILE_NAME = "notes.txt";
    private MutableLiveData<ArrayList<Note>> notesListLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> noteTitlesListLiveData = new MutableLiveData<>();



    private ArrayList<Note> notes = new ArrayList<>();
    private ArrayList<String> notesTitle = new ArrayList<>();



    public LiveData<ArrayList<Note>> getNotes() {
        return notesListLiveData;
    }

    public LiveData<ArrayList<String>> getNotesbyTitle() {
        return noteTitlesListLiveData;
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
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject noteObject = jsonArray.getJSONObject(i);
                String title = noteObject.getString("title");
                notesTitle.add(title);
                String description = noteObject.getString("description");
                notes.add(new Note(title, description));
            }
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
                Log.v("QUANTOS", String.valueOf(notes.size()))   ;
                JSONObject noteObject = new JSONObject();
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

        if (currentNotes != null && currentTitles != null) {
            currentNotes.add(note);
            currentTitles.add(note.getTitle());


            notesListLiveData.setValue(currentNotes);
            noteTitlesListLiveData.setValue(currentTitles);
        }
        saveNotesToFile(context); // Save to file
    }




}