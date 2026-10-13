package com.tastygo.model;

import java.io.Serializable;

public class Address implements Serializable {
    private long id;
    private long userId;
    private String label; // "Home", "Work", "Other"
    private String addressText;
    private boolean isDefault;

    public Address() {
    }

    public Address(long id, long userId, String label, String addressText, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.label = label;
        this.addressText = addressText;
        this.isDefault = isDefault;
    }

    public Address(long userId, String label, String addressText, boolean isDefault) {
        this(-1, userId, label, addressText, isDefault);
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

    public String getLabel() {
        return label != null ? label : "";
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddressText() {
        return addressText != null ? addressText : "";
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
