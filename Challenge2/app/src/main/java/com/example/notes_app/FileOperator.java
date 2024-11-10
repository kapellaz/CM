package com.example.notes_app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;

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
