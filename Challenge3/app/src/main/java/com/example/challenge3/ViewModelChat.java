package com.example.challenge3;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class ViewModelChat extends ViewModel{

    private MutableLiveData<String> username = new MutableLiveData<>();
    private MutableLiveData<String> contactName = new MutableLiveData<>();

    public String getUsername() {
        return username.getValue();
    }

    public String getContactName() {
        return contactName.getValue();
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public void setContactName(String contactName) {
        this.contactName.setValue(contactName);
    }
}