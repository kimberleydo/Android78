package com.example.android78.adapters;

import android.content.Context;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.android78.models.Album;
import java.util.List;

public class AlbumAdapter extends ArrayAdapter<Album> {
    public AlbumAdapter(Context context, List<Album> albums) {
        super(context, android.R.layout.simple_list_item_2, android.R.id.text1, albums);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Album album = getItem(position);
        TextView text1 = view.findViewById(android.R.id.text1);
        TextView text2 = view.findViewById(android.R.id.text2);
        text1.setText(album.getName());
        text2.setText(album.size() + " photo(s)");
        return view;
    }
}