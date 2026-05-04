package com.example.android78.activities;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android78.R;
import com.example.android78.models.Album;
import com.example.android78.models.Photo;
import com.example.android78.models.Tag;
import com.example.android78.utils.DataManager;
import java.util.List;

public class PhotoActivity extends AppCompatActivity {
    private int albumIndex;
    private int currentPhotoIndex;
    private Album album;
    private DataManager dataManager;
    private ImageView photoImageView;
    private TextView photoNameText;
    private TextView tagsText;
    private Button prevBtn, nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        dataManager = DataManager.getInstance();
        albumIndex = getIntent().getIntExtra("albumIndex", 0);
        currentPhotoIndex = getIntent().getIntExtra("photoIndex", 0);
        album = dataManager.getAlbums().get(albumIndex);

        photoImageView = findViewById(R.id.photoImageView);
        photoNameText = findViewById(R.id.photoNameText);
        tagsText = findViewById(R.id.tagsText);
        prevBtn = findViewById(R.id.btnPrev);
        nextBtn = findViewById(R.id.btnNext);
        Button addTagBtn = findViewById(R.id.btnAddTag);
        Button deleteTagBtn = findViewById(R.id.btnDeleteTag);
        Button moveBtn = findViewById(R.id.btnMove);

        displayCurrentPhoto();

        prevBtn.setOnClickListener(v -> {
            if (currentPhotoIndex > 0) { currentPhotoIndex--; displayCurrentPhoto(); }
        });
        nextBtn.setOnClickListener(v -> {
            if (currentPhotoIndex < album.size() - 1) { currentPhotoIndex++; displayCurrentPhoto(); }
        });
        addTagBtn.setOnClickListener(v -> showAddTagDialog());
        deleteTagBtn.setOnClickListener(v -> showDeleteTagDialog());
        moveBtn.setOnClickListener(v -> showMovePhotoDialog());
    }

    private void displayCurrentPhoto() {
        Photo photo = album.getPhotos().get(currentPhotoIndex);
        photoNameText.setText(photo.getFileName());
        try {
            photoImageView.setImageURI(Uri.parse(photo.getFilePath()));
        } catch (Exception e) {
            photoImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        List<Tag> tags = photo.getTags();
        if (tags.isEmpty()) {
            tagsText.setText("No tags");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Tag t : tags) sb.append(t.toString()).append("\n");
            tagsText.setText(sb.toString().trim());
        }
        prevBtn.setEnabled(currentPhotoIndex > 0);
        nextBtn.setEnabled(currentPhotoIndex < album.size() - 1);
    }

    private void showAddTagDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_tag, null);
        RadioGroup typeGroup = view.findViewById(R.id.tagTypeGroup);
        EditText valueInput = view.findViewById(R.id.tagValueInput);
        new AlertDialog.Builder(this)
                .setTitle("Add Tag")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String value = valueInput.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(this, "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Tag.TagType type = (typeGroup.getCheckedRadioButtonId() == R.id.radioPerson)
                            ? Tag.TagType.PERSON : Tag.TagType.LOCATION;
                    Photo photo = album.getPhotos().get(currentPhotoIndex);
                    if (!photo.addTag(new Tag(type, value))) {
                        Toast.makeText(this, "Tag already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        dataManager.save(this);
                        displayCurrentPhoto();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteTagDialog() {
        Photo photo = album.getPhotos().get(currentPhotoIndex);
        List<Tag> tags = photo.getTags();
        if (tags.isEmpty()) {
            Toast.makeText(this, "No tags to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] tagStrings = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) tagStrings[i] = tags.get(i).toString();
        new AlertDialog.Builder(this)
                .setTitle("Delete Tag")
                .setItems(tagStrings, (dialog, which) -> {
                    photo.removeTag(tags.get(which));
                    dataManager.save(this);
                    displayCurrentPhoto();
                }).show();
    }

    private void showMovePhotoDialog() {
        List<Album> allAlbums = dataManager.getAlbums();
        String[] albumNames = allAlbums.stream()
                .filter(a -> a != album)
                .map(Album::getName)
                .toArray(String[]::new);
        if (albumNames.length == 0) {
            Toast.makeText(this, "No other albums to move to", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Move to Album")
                .setItems(albumNames, (dialog, which) -> {
                    Album target = allAlbums.stream()
                            .filter(a -> a.getName().equals(albumNames[which]))
                            .findFirst().orElse(null);
                    if (target == null) return;
                    Photo photo = album.getPhotos().get(currentPhotoIndex);
                    if (!target.addPhoto(photo)) {
                        Toast.makeText(this, "Photo already in target album", Toast.LENGTH_SHORT).show();
                    } else {
                        album.removePhoto(photo);
                        dataManager.save(this);
                        Toast.makeText(this, "Moved to " + target.getName(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).show();
    }
}