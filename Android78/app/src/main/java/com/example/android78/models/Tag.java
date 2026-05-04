package com.example.android78.models;

import java.io.Serializable;

public class Tag implements Serializable {
    public enum TagType { PERSON, LOCATION }

    private TagType type;
    private String value;

    public Tag(TagType type, String value) {
        this.type = type;
        this.value = value.trim().toLowerCase();
    }

    public TagType getType() { return type; }
    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tag)) return false;
        Tag t = (Tag) o;
        return type == t.type && value.equalsIgnoreCase(t.value);
    }

    @Override
    public int hashCode() {
        return (type.name() + value.toLowerCase()).hashCode();
    }

    @Override
    public String toString() {
        return (type == TagType.PERSON ? "Person" : "Location") + ": " + value;
    }
}