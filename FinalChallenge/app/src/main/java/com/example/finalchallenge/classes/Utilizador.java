package com.example.finalchallenge.classes;

import android.os.Parcel;
import android.os.Parcelable;

public class Utilizador implements Parcelable {
    private String username;
    private String id;
    private boolean firstTimeFragment;

    // Default constructor
    public Utilizador() {}

    // Constructor with parameters
    public Utilizador(String username, String id) {
        this.username = username;
        this.id = id;
    }

    // Getter and setter methods
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getFirstTimeFragment() {
        return firstTimeFragment;
    }

    public void setFirstTimeFragment(boolean firstTimeFragment) {
        this.firstTimeFragment = firstTimeFragment;
    }

    @Override
    public String toString() {
        return "Utilizador{" +
                "username='" + username + '\'' +
                ", id=" + id +
                ", firstTimeFragment=" + firstTimeFragment +
                '}';
    }

    // Parcelable implementation

    // Constructor for creating from Parcel
    protected Utilizador(Parcel in) {
        username = in.readString();
        id = in.readString();
        firstTimeFragment = in.readByte() != 0; // Read boolean as a byte
    }

    // Parcelable.Creator implementation
    public static final Creator<Utilizador> CREATOR = new Creator<Utilizador>() {
        @Override
        public Utilizador createFromParcel(Parcel in) {
            return new Utilizador(in);
        }

        @Override
        public Utilizador[] newArray(int size) {
            return new Utilizador[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(id);
        dest.writeByte((byte) (firstTimeFragment ? 1 : 0)); // Write boolean as a byte
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
