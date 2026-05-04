package com.example.android78.utils;

import android.content.Context;
import com.example.android78.models.Album;
import com.example.android78.models.Photo;
import com.example.android78.models.Tag;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String FILE_NAME = "photos_data.json";
    private static DataManager instance;
    private List<Album> albums;

    private DataManager() {
        albums = new ArrayList<>();
    }

    public static DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    public List<Album> getAlbums() { return albums; }

    public boolean addAlbum(String name) {
        for (Album a : albums) {
            if (a.getName().equalsIgnoreCase(name)) return false;
        }
        albums.add(new Album(name));
        return true;
    }

    public boolean deleteAlbum(Album album) { return albums.remove(album); }

    public boolean renameAlbum(Album album, String newName) {
        for (Album a : albums) {
            if (a != album && a.getName().equalsIgnoreCase(newName)) return false;
        }
        album.setName(newName);
        return true;
    }

    public List<Photo> searchByTag(Tag.TagType type, String query) {
        List<Photo> results = new ArrayList<>();
        String q = query.trim().toLowerCase();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTagsByType(type)) {
                    if (tag.getValue().startsWith(q)) {
                        results.add(photo);
                        break;
                    }
                }
            }
        }
        return results;
    }

    public List<String> getAutocompleteSuggestions(Tag.TagType type, String query) {
        List<String> suggestions = new ArrayList<>();
        String q = query.trim().toLowerCase();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTagsByType(type)) {
                    if (tag.getValue().startsWith(q) && !suggestions.contains(tag.getValue())) {
                        suggestions.add(tag.getValue());
                    }
                }
            }
        }
        return suggestions;
    }

    public List<Photo> searchConjunction(List<Tag> requiredTags) {
        List<Photo> results = new ArrayList<>();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                boolean matchAll = true;
                for (Tag required : requiredTags) {
                    boolean found = false;
                    for (Tag t : photo.getTagsByType(required.getType())) {
                        if (t.getValue().equalsIgnoreCase(required.getValue())) { found = true; break; }
                    }
                    if (!found) { matchAll = false; break; }
                }
                if (matchAll) results.add(photo);
            }
        }
        return results;
    }

    public List<Photo> searchDisjunction(List<Tag> anyTags) {
        List<Photo> results = new ArrayList<>();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                outer:
                for (Tag required : anyTags) {
                    for (Tag t : photo.getTagsByType(required.getType())) {
                        if (t.getValue().equalsIgnoreCase(required.getValue())) {
                            results.add(photo);
                            break outer;
                        }
                    }
                }
            }
        }
        return results;
    }

    public void save(Context context) {
        try {
            JSONArray albumsJson = new JSONArray();
            for (Album album : albums) {
                JSONObject albumObj = new JSONObject();
                albumObj.put("name", album.getName());
                JSONArray photosJson = new JSONArray();
                for (Photo photo : album.getPhotos()) {
                    JSONObject photoObj = new JSONObject();
                    photoObj.put("filePath", photo.getFilePath());
                    JSONArray tagsJson = new JSONArray();
                    for (Tag tag : photo.getTags()) {
                        JSONObject tagObj = new JSONObject();
                        tagObj.put("type", tag.getType().name());
                        tagObj.put("value", tag.getValue());
                        tagsJson.put(tagObj);
                    }
                    photoObj.put("tags", tagsJson);
                    photosJson.put(photoObj);
                }
                albumObj.put("photos", photosJson);
                albumsJson.put(albumObj);
            }
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(albumsJson.toString().getBytes());
            fos.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void load(Context context) {
        albums.clear();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            fis.close();

            JSONArray albumsJson = new JSONArray(sb.toString());
            for (int i = 0; i < albumsJson.length(); i++) {
                JSONObject albumObj = albumsJson.getJSONObject(i);
                Album album = new Album(albumObj.getString("name"));
                JSONArray photosJson = albumObj.getJSONArray("photos");
                for (int j = 0; j < photosJson.length(); j++) {
                    JSONObject photoObj = photosJson.getJSONObject(j);
                    Photo photo = new Photo(photoObj.getString("filePath"));
                    JSONArray tagsJson = photoObj.getJSONArray("tags");
                    for (int k = 0; k < tagsJson.length(); k++) {
                        JSONObject tagObj = tagsJson.getJSONObject(k);
                        Tag.TagType type = Tag.TagType.valueOf(tagObj.getString("type"));
                        photo.addTag(new Tag(type, tagObj.getString("value")));
                    }
                    album.addPhoto(photo);
                }
                albums.add(album);
            }
        } catch (FileNotFoundException e) {
            // First launch — no saved data yet
        } catch (Exception e) { e.printStackTrace(); }
    }
}