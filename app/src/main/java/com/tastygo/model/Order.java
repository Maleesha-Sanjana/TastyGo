package com.tastygo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    public static final String STATUS_PLACED = "PLACED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_PREPARING = "PREPARING";
    public static final String STATUS_OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String TYPE_DELIVERY = "DELIVERY";
    public static final String TYPE_PICKUP = "PICKUP";

    private long id;
    private long userId;
    private Long addressId; // nullable for pickup
    private double subtotal;
    private double discount;
    private double deliveryFee;
    private double tax;
    private double total;
    private String paymentMethod; // "CASH", "CARD", "WALLET"
    private String orderType;     // "DELIVERY" or "PICKUP"
    private String status;        // PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    private String note;
    private String createdAt;

    // Joined / transient fields
    private String userName;
    private String userPhone;
    private String addressText;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
        this.status = STATUS_PLACED;
        this.orderType = TYPE_DELIVERY;
    }

    public Order(long id, long userId, Long addressId, double subtotal, double discount, double deliveryFee, double tax, double total, String paymentMethod, String orderType, String status, String note, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.addressId = addressId;
        this.subtotal = subtotal;
        this.discount = discount;
        this.deliveryFee = deliveryFee;
        this.tax = tax;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.orderType = orderType;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Order(long userId, Long addressId, double subtotal, double discount, double deliveryFee, double tax, double total, String paymentMethod, String orderType, String status, String note, String createdAt) {
        this(-1, userId, addressId, subtotal, discount, deliveryFee, tax, total, paymentMethod, orderType, status, note, createdAt);
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

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod != null ? paymentMethod : "";
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getOrderType() {
        return orderType != null ? orderType : TYPE_DELIVERY;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getStatus() {
        return status != null ? status : STATUS_PLACED;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note != null ? note : "";
    }

    public void setNote(String note) {
        this.note = note;
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

    public String getUserPhone() {
        return userPhone != null ? userPhone : "";
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getAddressText() {
        return addressText != null ? addressText : "";
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public boolean canBeCancelled() {
        return STATUS_PLACED.equalsIgnoreCase(status) || STATUS_CONFIRMED.equalsIgnoreCase(status);
    }
}
