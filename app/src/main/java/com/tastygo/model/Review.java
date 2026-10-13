package com.tastygo.model;

import java.io.Serializable;

public class Review implements Serializable {
    private long id;
    private long userId;
    private long foodId;
    private double rating;
    private String comment;
    private String createdAt;

    // Joined fields
    private String userName;
    private String foodName;

    public Review() {
    }

    public Review(long id, long userId, long foodId, double rating, String comment, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.foodId = foodId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Review(long userId, long foodId, double rating, String comment, String createdAt) {
        this(-1, userId, foodId, rating, comment, createdAt);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFoodId() {
        return foodId;
    }

    public void setFoodId(long foodId) {
        this.foodId = foodId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment != null ? comment : "";
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt : "";
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName != null ? userName : "";
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFoodName() {
        return foodName != null ? foodName : "";
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
}
