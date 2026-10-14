package com.tastygo.model;

import java.io.Serializable;

public class Coupon implements Serializable {
    public static final String TYPE_PERCENT = "PERCENT";
    public static final String TYPE_FLAT = "FLAT";

    private long id;
    private String code;
    private String discountType; // "PERCENT" or "FLAT"
    private double value;
    private double minOrder;
    private String expiryDate;
    private boolean isActive;

    public Coupon() {
        this.isActive = true;
    }

    public Coupon(long id, String code, String discountType, double value, double minOrder, String expiryDate, boolean isActive) {
        this.id = id;
        this.code = code;
        this.discountType = discountType;
        this.value = value;
        this.minOrder = minOrder;
        this.expiryDate = expiryDate;
        this.isActive = isActive;
    }

    public Coupon(String code, String discountType, double value, double minOrder, String expiryDate, boolean isActive) {
        this(-1, code, discountType, value, minOrder, expiryDate, isActive);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code != null ? code : "";
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType != null ? discountType : TYPE_PERCENT;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(double minOrder) {
        this.minOrder = minOrder;
    }

    public String getExpiryDate() {
        return expiryDate != null ? expiryDate : "";
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isPercentage() {
        return TYPE_PERCENT.equalsIgnoreCase(discountType);
    }

    public double calculateDiscount(double subtotal) {
        if (!isActive || subtotal < minOrder) {
            return 0.0;
        }
        if (TYPE_PERCENT.equalsIgnoreCase(discountType)) {
            return subtotal * (value / 100.0);
        } else {
            return Math.min(value, subtotal);
        }
    }
}
