package com.example.prif233.model;


import java.util.List;

public class Restaurant extends BasicUser {
    private String restaurantName;
    private String openingTime; // Format: "HH:mm"
    private String closingTime; // Format: "HH:mm"

    public Restaurant(String login, String password, String name, String surname, String phoneNumber, String address) {
        super(login, password, name, surname, phoneNumber, address);
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    @Override
    public String toString() {
        return restaurantName != null ? restaurantName : super.toString();
    }
}