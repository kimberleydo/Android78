package com.example.android78.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.android78.R;
import com.example.android78.adapters.PhotoGridAdapter;
import com.example.android78.models.Album;
import com.example.android78.models.Photo;
import com.example.android78.utils.DataManager;

public class AlbumActivity extends AppCompatActivity {
    private int albumIndex;
    private Album album;
    private DataManager dataManager;
    private PhotoGridAdapter photoAdapter;

    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Photo photo = new Photo(uri.toString());
                    if (!album.addPhoto(photo)) {
                        Toast.makeText(this, "Photo already in this album", Toast.LENGTH_SHORT).show();
                    } else {
                        dataManager.save(this);
                        photoAdapter.notifyDataSetChanged();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        dataManager = DataManager.getInstance();
        albumIndex = getIntent().getIntExtra("albumIndex", 0);
        album = dataManager.getAlbums().get(albumIndex);
        setTitle(album.getName());

        GridView gridView = findViewById(R.id.photoGrid);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddPhoto);

        photoAdapter = new PhotoGridAdapter(this, album.getPhotos());
        gridView.setAdapter(photoAdapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra("albumIndex", albumIndex);
            intent.putExtra("photoIndex", position);
            startActivity(intent);
        });

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            Photo photo = album.getPhotos().get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Remove Photo")
                    .setMessage("Remove \"" + photo.getFileName() + "\" from this album?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        album.removePhoto(photo);
                        dataManager.save(this);
                        photoAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        fabAdd.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoAdapter.notifyDataSetChanged();
    }
}