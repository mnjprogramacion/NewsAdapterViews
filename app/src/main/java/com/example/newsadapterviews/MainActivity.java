package com.example.newsadapterviews;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.StackView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<NewsItem> newsList = new ArrayList<>();
    private NewsAdapter adapter;
    private FrameLayout container;

    // URL pública de Google Sheets exportada como CSV
    private static final String CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRr3-LdTANTCbj0ojujZRxBttAdkwjLtwIIY7XT4SMCDZoBLSW8NqAQPS7BfuO2nn5AByiqS31x6kaJ/pub?gid=0&single=true&output=csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        container = findViewById(R.id.container);
        adapter = new NewsAdapter(this, newsList);

        Spinner spinner = findViewById(R.id.spinnerViewType);
        String[] viewTypes = {"ListView", "GridView", "StackView", "Gallery"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, viewTypes));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showAdapterView(viewTypes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        downloadCsv();
    }

    @SuppressWarnings("deprecation")
    private void showAdapterView(String type) {
        container.removeAllViews();
        AdapterView adapterView;

        switch (type) {
            case "GridView":
                GridView grid = new GridView(this);
                grid.setNumColumns(2);
                adapterView = grid;
                break;
            case "StackView":
                adapterView = new StackView(this);
                break;
            case "Gallery":
                Gallery gallery = new Gallery(this);
                gallery.setSpacing(8);
                adapterView = gallery;
                break;
            default:
                adapterView = new ListView(this);
                break;
        }

        adapterView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        adapterView.setAdapter(adapter);
        adapterView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("news", newsList.get(position));
            startActivity(intent);
        });

        container.addView(adapterView);
    }

    private void downloadCsv() {
        new Thread(() -> {
            try {
                URL url = new URL(CSV_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                boolean first = true;
                List<NewsItem> items = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    if (first) { first = false; continue; } // saltar cabecera
                    String[] cols = parseCsvLine(line);
                    if (cols.length >= 5) {
                        items.add(new NewsItem(
                                cols[0].trim(), cols[1].trim(), cols[2].trim(),
                                cols[3].trim(), cols[4].trim()));
                    }
                }
                reader.close();

                runOnUiThread(() -> {
                    newsList.clear();
                    newsList.addAll(items);
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}