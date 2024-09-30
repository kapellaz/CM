package com.example.challenge1;

public class Animal
{
    private String owner;
    private String name;
    private String age;

    public Animal(String owner, String name, String age) {
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

    @Override
    public String toString() {
        return name;
    }
}
