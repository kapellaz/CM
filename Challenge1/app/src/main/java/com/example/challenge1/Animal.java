package com.example.challenge1;
import java.io.Serializable;

public class Animal implements Serializable
{
    private String type;
    private String owner;
    private String name;
    private String age;

    public Animal(String type, String owner, String name, String age) {
        this.type = type;
        this.owner = owner;
        this.name = name;
        this.age = age;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getType() {
        return type;
    }




    @Override
    public String toString() {
        return type;
    }
}
