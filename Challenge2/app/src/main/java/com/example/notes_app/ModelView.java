package com.example.notes_app;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;


public class ModelView extends ViewModel{

    private MutableLiveData<ArrayList<Note>> notesListLiveData = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> noteTitlesListLiveData = new MutableLiveData<>();

    private MutableLiveData<Note> editData = new MutableLiveData<>();


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



    public void setNotesListLiveData(ArrayList<Note> notesListLiveDataa) {
        notesListLiveData.setValue(notesListLiveDataa);
    }

    public void setNoteTitlesListLiveData(ArrayList<String> noteTitlesListLiveData) {
        this.noteTitlesListLiveData.setValue(noteTitlesListLiveData);
    }

}