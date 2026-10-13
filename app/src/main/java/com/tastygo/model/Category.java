package com.tastygo.model;

import java.io.Serializable;

public class Category implements Serializable {
    private long id;
    private String name;
    private String imagePath;

    public Category() {
    }

    public Category(long id, String name, String imagePath) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
    }

    public Category(String name, String imagePath) {
        this(-1, name, imagePath);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath != null ? imagePath : "";
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
