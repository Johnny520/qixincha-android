package com.qxx.johnny;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        String title = getIntent().getStringExtra("title");
        String text = getIntent().getStringExtra("text");
        if (title != null) setTitle(title);
        TextView tv = findViewById(R.id.tv_info);
        tv.setText(text == null ? "" : text);
    }
}
