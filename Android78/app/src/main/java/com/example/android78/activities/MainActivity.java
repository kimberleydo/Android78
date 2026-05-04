package com.example.android78.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.android78.R;
import com.example.android78.adapters.AlbumAdapter;
import com.example.android78.models.Album;
import com.example.android78.utils.DataManager;

public class MainActivity extends AppCompatActivity {
    private AlbumAdapter albumAdapter;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = DataManager.getInstance();
        dataManager.load(this);

        ListView albumListView = findViewById(R.id.albumListView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddAlbum);
        FloatingActionButton fabSearch = findViewById(R.id.fabSearch);

        albumAdapter = new AlbumAdapter(this, dataManager.getAlbums());
        albumListView.setAdapter(albumAdapter);

        albumListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, AlbumActivity.class);
            intent.putExtra("albumIndex", position);
            startActivity(intent);
        });

        albumListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showAlbumOptionsDialog(position);
            return true;
        });

        fabAdd.setOnClickListener(v -> showCreateAlbumDialog());
        fabSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataManager.save(this);
        albumAdapter.notifyDataSetChanged();
    }

    private void showCreateAlbumDialog() {
        EditText input = new EditText(this);
        input.setHint("Album name");
        new AlertDialog.Builder(this)
                .setTitle("New Album")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if (!dataManager.addAlbum(name)) {
                        Toast.makeText(this, "Album already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        dataManager.save(this);
                        albumAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAlbumOptionsDialog(int position) {
        Album album = dataManager.getAlbums().get(position);
        new AlertDialog.Builder(this)
                .setTitle(album.getName())
                .setItems(new String[]{"Rename", "Delete"}, (dialog, which) -> {
                    if (which == 0) showRenameDialog(album);
                    else showDeleteDialog(album);
                }).show();
    }

    private void showRenameDialog(Album album) {
        EditText input = new EditText(this);
        input.setText(album.getName());
        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if (!dataManager.renameAlbum(album, newName)) {
                        Toast.makeText(this, "Album name already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        dataManager.save(this);
                        albumAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Album album) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Delete \"" + album.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dataManager.deleteAlbum(album);
                    dataManager.save(this);
                    albumAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}