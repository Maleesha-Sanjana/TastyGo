package com.tastygo.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private long id;
    private long userId;
    private long foodId;
    private int quantity;
    private String size;
    private String addons;
    private String note;

    private String foodName;
    private double unitPrice;
    private String foodImagePath;
    private boolean isVeg;

    public CartItem() {
        this.quantity = 1;
    }

    public CartItem(long id, long userId, long foodId, int quantity, String size, String addons, String note) {
        this.id = id;
        this.userId = userId;
        this.foodId = foodId;
        this.quantity = quantity;
        this.size = size;
        this.addons = addons;
        this.note = note;
    }

    public CartItem(long userId, long foodId, int quantity, String size, String addons, String note) {
        this(-1, userId, foodId, quantity, size, addons, note);
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size != null ? size : "";
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getAddons() {
        return addons != null ? addons : "";
    }

    public void setAddons(String addons) {
        this.addons = addons;
    }

    public String getNote() {
        return note != null ? note : "";
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFoodName() {
        return foodName != null ? foodName : "";
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getFoodImagePath() {
        return foodImagePath != null ? foodImagePath : "";
    }

    public void setFoodImagePath(String foodImagePath) {
        this.foodImagePath = foodImagePath;
    }

    public boolean isVeg() {
        return isVeg;
    }

    public void setVeg(boolean veg) {
        isVeg = veg;
    }

    public double getItemTotal() {
        return unitPrice * quantity;
    }
}
