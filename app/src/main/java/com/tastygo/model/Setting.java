package com.tastygo.model;

import java.io.Serializable;

public class Setting implements Serializable {
    private String key;
    private String value;

    public Setting() {
    }

    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key != null ? key : "";
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value != null ? value : "";
    }

    public void setValue(String value) {
        this.value = value;
    }
}
