package com.tastygo.model;

import java.io.Serializable;

public class FoodOption implements Serializable {
    private long id;
    private long foodId;
    private String type;
    private String name;
    private double extraPrice;

    public FoodOption() {
    }

    public FoodOption(long id, long foodId, String type, String name, double extraPrice) {
        this.id = id;
        this.foodId = foodId;
        this.type = type;
        this.name = name;
        this.extraPrice = extraPrice;
    }

    public FoodOption(long foodId, String type, String name, double extraPrice) {
        this(-1, foodId, type, name, extraPrice);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFoodId() {
        return foodId;
    }

    public void setFoodId(long foodId) {
        this.foodId = foodId;
    }

    public String getType() {
        return type != null ? type : "SIZE";
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(double extraPrice) {
        this.extraPrice = extraPrice;
    }

    public boolean isSize() {
        return "SIZE".equalsIgnoreCase(type);
    }

    public boolean isAddon() {
        return "ADDON".equalsIgnoreCase(type);
    }
}
