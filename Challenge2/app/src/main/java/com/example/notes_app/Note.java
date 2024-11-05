package com.example.notes_app;

import androidx.annotation.NonNull;

public class Note {
    private String id_note;
    private String title;
    private String description;

    public Note(String id, String title, String description) {
        this.id_note = id;
        this.title = title;
        this.description = description;
    }

    public String getId_note() {
        return id_note;
    }


    public String getTitle() {
        return title;
    }
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
