package com.example.prif233.model;

import java.util.List;

public class FoodOrder {

    private int id;
    private String name;
    private Double price;

    public FoodOrder(int id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public FoodOrder() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
