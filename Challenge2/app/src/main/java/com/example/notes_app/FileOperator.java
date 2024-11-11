package com.example.notes_app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FileOperator {

    final Executor executor = Executors.newSingleThreadExecutor();
    final Handler handler = new Handler(Looper.getMainLooper());





    public interface Callback {
        void onCompleteRead(ArrayList<Note> result);
    }

    /**
     * Method using executor and handlers to load all notes from file
     */
    public void loadNotesFromFile(Context context, ModelView modelView,NoteSenderFireStore NoteSender) {
        executor.execute(() -> {
            Log.v("FileOperator", "Loading notes on thread: " + Thread.currentThread().getName());
            ArrayList<Note> loadedNotes = new ArrayList<>();
            ArrayList<String> notesTitle = new ArrayList<>();
            ArrayList<String> ids = new ArrayList<>();
            try {
                FileInputStream fis = context.openFileInput("notes.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject noteObject = jsonArray.getJSONObject(i);
                    String id = noteObject.getString("id");
                    String title = noteObject.getString("title");
                    String description = noteObject.getString("description");
                    ids.add(id);
                    notesTitle.add(title);
                    loadedNotes.add(new Note(id, title, description));
                    NoteSender.syncNotesToFireStore(context,id,title,description);

                }
                NoteSender.deleteNotesNotInFile(context,ids);
            } catch (Exception e) {
                e.printStackTrace();
            }


            handler.post(() -> {
                modelView.setNoteTitlesListLiveData(notesTitle);
                modelView.setNotesListLiveData(loadedNotes);

            });
        });
    }


    /**
     * Method using executors and handlers to Save all notes to internal storage
     */

    public void saveNotesToFile(Context context, ArrayList<Note> notes, Callback callback) {
        executor.execute(() -> {
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
                FileOutputStream fos = context.openFileOutput("notes.txt", Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                osw.write(jsonArray.toString());
                osw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(()->{
                callback.onCompleteRead(notes);
            });
        });
    }
}
