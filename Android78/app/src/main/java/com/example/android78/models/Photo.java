package com.example.android78.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Photo implements Serializable {
    private String filePath;
    private List<Tag> tags;

    public Photo(String filePath) {
        this.filePath = filePath;
        this.tags = new ArrayList<>();
    }

    public String getFilePath() { return filePath; }

    public String getFileName() {
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    public List<Tag> getTags() { return tags; }

    public boolean addTag(Tag tag) {
        if (tags.contains(tag)) return false;
        tags.add(tag);
        return true;
    }

    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    public List<Tag> getTagsByType(Tag.TagType type) {
        List<Tag> result = new ArrayList<>();
        for (Tag t : tags) {
            if (t.getType() == type) result.add(t);
        }
        return result;
    }
}