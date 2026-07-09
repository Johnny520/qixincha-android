package com.qxx.johnny;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.qxx.johnny.model.Company;
import com.qxx.johnny.net.CompanyFetcher;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText etQuery;
    private ListView listView;
    private TextView tvEmpty;
    private ArrayAdapter<String> adapter;
    private ProgressDialog pd;
    private final CompanyFetcher fetcher = new CompanyFetcher();
    private final List<Company> companies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etQuery = findViewById(R.id.et_query);
        listView = findViewById(R.id.list_view);
        tvEmpty = findViewById(R.id.tv_empty);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnSettings = findViewById(R.id.btn_settings);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> doSearch());
        etQuery.setOnEditorActionListener((v, actionId, event) -> {
            doSearch();
            return true;
        });
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < companies.size()) {
                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra("name", companies.get(position).name);
                startActivity(i);
            }
        });
    }

    private void doSearch() {
        String q = etQuery.getText().toString().trim();
        if (TextUtils.isEmpty(q)) return;
        pd = ProgressDialog.show(this, null, getString(R.string.loading), true);
        new Thread(() -> {
            List<Company> list = fetcher.search(q);
            runOnUiThread(() -> {
                if (pd != null && pd.isShowing()) pd.dismiss();
                companies.clear();
                companies.addAll(list);
                adapter.clear();
                if (list.isEmpty()) {
                    tvEmpty.setText(R.string.no_result);
                    tvEmpty.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                } else {
                    for (Company c : list) adapter.add(c.name);
                    tvEmpty.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }
}
