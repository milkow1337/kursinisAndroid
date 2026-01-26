package com.example.prif233.model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class BasicUser extends User{
    protected String address;

    public BasicUser() {
    }

    public BasicUser(int id, String login, String password, String name, String surname, String phoneNumber, LocalDateTime dateCreated, LocalDateTime dateUpdated, boolean isAdmin, String address) {
        super(id, login, password, name, surname, phoneNumber, dateCreated, dateUpdated, isAdmin);
        this.address = address;
    }

    public BasicUser(String login, String password, String name, String surname, String phoneNumber, String address) {
        super(login, password, name, surname, phoneNumber);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
