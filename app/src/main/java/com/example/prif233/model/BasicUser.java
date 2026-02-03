package com.example.prif233.model;

import java.time.LocalDateTime;

public class BasicUser extends User {
    protected String address;
    protected int loyaltyPoints = 0;

    public BasicUser() {
    }

    public BasicUser(int id, String login, String password, String name, String surname,
                     String phoneNumber, LocalDateTime dateCreated, LocalDateTime dateUpdated,
                     boolean isAdmin, String address, int loyaltyPoints) {
        super(id, login, password, name, surname, phoneNumber, dateCreated, dateUpdated, isAdmin);
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
    }

    public BasicUser(String login, String password, String name, String surname,
                     String phoneNumber, String address) {
        super(login, password, name, surname, phoneNumber);
        this.address = address;
        this.loyaltyPoints = 0;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    @Override
    public String toString() {
        return String.format("%s %s (Points: %d)", name, surname, loyaltyPoints);
    }
}