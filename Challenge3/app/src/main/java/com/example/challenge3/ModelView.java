package com.example.challenge3;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ModelView extends ViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> contactName = new MutableLiveData<>();

    // Getter for username
    public LiveData<String> getUsername() {
        return username;
    }

    // Setter for username
    public void setUsername(String username) {
        this.username.setValue(username);
    }

    // Getter for contactName
    public LiveData<String> getContactName() {
        return contactName;
    }

    // Setter for contactName
    public void setContactName(String contactName) {
        this.contactName.setValue(contactName);
    }
}
