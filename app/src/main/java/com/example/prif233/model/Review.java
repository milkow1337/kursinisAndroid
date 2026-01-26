package com.example.prif233.model;


import java.time.LocalDate;


public class Review {

    private int id;
    private int rating;
    private String reviewText;
    private LocalDate dateCreated;

    public Review(int id, int rating, String reviewText, LocalDate dateCreated) {
        this.id = id;
        this.rating = rating;
        this.reviewText = reviewText;
        this.dateCreated = dateCreated;
    }

    public Review() {
    }

    public Review(String reviewText) {
        this.reviewText = reviewText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        if (dateCreated != null) {
            return reviewText + " (" + dateCreated + ")";
        }
        return reviewText != null ? reviewText : "";
    }
}
