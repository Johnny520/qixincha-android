/*
 * 企信查 (qixincha-android)
 * Copyright © 2026 文强哥 (Johnny520). All rights reserved.
 */

package com.qxx.johnny;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class InfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        String title = getIntent().getStringExtra("title");
        String text = getIntent().getStringExtra("text");

        // 当前主题为 NoActionBar，手动挂接 MaterialToolbar 显示各自法律文档标题与返回按钮
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setTitle(title != null ? title : getString(R.string.app_name));
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tv = findViewById(R.id.tv_info);
        tv.setText(text == null ? "" : text);
    }
}
