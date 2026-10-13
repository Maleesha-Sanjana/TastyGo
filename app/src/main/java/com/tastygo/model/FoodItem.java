package com.tastygo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FoodItem implements Serializable {
    private long id;
    private long categoryId;
    private String name;
    private String description;
    private double price;
    private String imagePath;
    private boolean isAvailable;
    private boolean isVeg;
    private double avgRating;

    // Joined / transient fields
    private String categoryName;
    private boolean isFavorite;
    private List<FoodOption> options = new ArrayList<>();

    public FoodItem() {
        this.isAvailable = true;
        this.isVeg = true;
    }

    public FoodItem(long id, long categoryId, String name, String description, double price, String imagePath, boolean isAvailable, boolean isVeg, double avgRating) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imagePath = imagePath;
        this.isAvailable = isAvailable;
        this.isVeg = isVeg;
        this.avgRating = avgRating;
    }

    public FoodItem(long categoryId, String name, String description, double price, String imagePath, boolean isAvailable, boolean isVeg, double avgRating) {
        this(-1, categoryId, name, description, price, imagePath, isAvailable, isVeg, avgRating);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImagePath() {
        return imagePath != null ? imagePath : "";
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isVeg() {
        return isVeg;
    }

    public void setVeg(boolean veg) {
        isVeg = veg;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public String getCategoryName() {
        return categoryName != null ? categoryName : "";
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public List<FoodOption> getOptions() {
        return options;
    }

    public void setOptions(List<FoodOption> options) {
        this.options = options != null ? options : new ArrayList<>();
    }
}
