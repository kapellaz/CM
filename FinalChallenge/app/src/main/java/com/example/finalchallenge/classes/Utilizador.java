package com.example.finalchallenge.classes;

public class Utilizador {
    private String username;
    private String id;
    private boolean firstTimeFragment;

    public Utilizador(){

    }

    public Utilizador(String username, String id){
        this.username=username;
        this.id=id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }
    public boolean getFirstTimeFragment(){
        return firstTimeFragment;
    }

    public void setFirstTimeFragment(boolean firstTimeFragment) {
        this.firstTimeFragment = firstTimeFragment;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Utilizador{" +
                "username='" + username + '\'' +
                ", id=" + id +
                '}';
    }
}
