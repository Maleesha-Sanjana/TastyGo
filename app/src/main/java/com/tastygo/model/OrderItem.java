package com.tastygo.model;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private long id;
    private long orderId;
    private Long foodId; // nullable if food was deleted
    private String nameSnapshot;
    private double priceSnapshot;
    private int quantity;

    public OrderItem() {
        this.quantity = 1;
    }

    public OrderItem(long id, long orderId, Long foodId, String nameSnapshot, double priceSnapshot, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.foodId = foodId;
        this.nameSnapshot = nameSnapshot;
        this.priceSnapshot = priceSnapshot;
        this.quantity = quantity;
    }

    public OrderItem(long orderId, Long foodId, String nameSnapshot, double priceSnapshot, int quantity) {
        this(-1, orderId, foodId, nameSnapshot, priceSnapshot, quantity);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public String getNameSnapshot() {
        return nameSnapshot != null ? nameSnapshot : "";
    }

    public void setNameSnapshot(String nameSnapshot) {
        this.nameSnapshot = nameSnapshot;
    }

    public double getPriceSnapshot() {
        return priceSnapshot;
    }

    public void setPriceSnapshot(double priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getItemTotal() {
        return priceSnapshot * quantity;
    }
}
