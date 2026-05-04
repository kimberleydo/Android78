package com.example.android78.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Album implements Serializable {
    private String name;
    private List<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Photo> getPhotos() { return photos; }

    public boolean addPhoto(Photo photo) {
        for (Photo p : photos) {
            if (p.getFilePath().equals(photo.getFilePath())) return false;
        }
        photos.add(photo);
        return true;
    }

    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }

    public int size() { return photos.size(); }
}