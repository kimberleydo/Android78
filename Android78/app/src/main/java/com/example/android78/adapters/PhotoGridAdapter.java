package com.example.android78.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import com.example.android78.R;
import com.example.android78.models.Photo;
import java.util.List;

public class PhotoGridAdapter extends BaseAdapter {
    private final Context context;
    private final List<Photo> photos;

    public PhotoGridAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }

    @Override public int getCount() { return photos.size(); }
    @Override public Photo getItem(int pos) { return photos.get(pos); }
    @Override public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_photo_thumb, parent, false);
        }
        ImageView imageView = convertView.findViewById(R.id.thumbImage);
        TextView nameText = convertView.findViewById(R.id.thumbName);
        Photo photo = photos.get(position);
        nameText.setText(photo.getFileName());
        try {
            imageView.setImageURI(Uri.parse(photo.getFilePath()));
        } catch (Exception e) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        return convertView;
    }
}