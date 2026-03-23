package com.example.newsadapterviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.StackView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsAdapter extends BaseAdapter {

    private final Context context;
    private final List<NewsItem> items;

    public NewsAdapter(Context context, List<NewsItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public NewsItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        boolean useCard = parent instanceof StackView || parent instanceof Gallery;
        int layoutRes = useCard ? R.layout.item_news_stack : R.layout.item_news;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        }

        NewsItem item = items.get(position);

        TextView txtTitulo = convertView.findViewById(R.id.txtTitulo);
        TextView txtDescripcion = convertView.findViewById(R.id.txtDescripcion);
        ImageView imgThumbnail = convertView.findViewById(R.id.imgThumbnail);

        txtTitulo.setText(item.titulo);
        txtDescripcion.setText(item.descripcion);

        if (item.thumbnailUrl != null && !item.thumbnailUrl.isEmpty()) {
            Picasso.get().load(item.thumbnailUrl).placeholder(android.R.drawable.ic_menu_gallery).into(imgThumbnail);
        }

        return convertView;
    }
}
