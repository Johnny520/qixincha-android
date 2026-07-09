package com.qxx.johnny;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private EditText etKey;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etKey = findViewById(R.id.et_key);
        Button btnSave = findViewById(R.id.btn_save);
        sp = getSharedPreferences("qxc", MODE_PRIVATE);

        etKey.setText(sp.getString("api_key", ""));
        btnSave.setOnClickListener(v -> {
            sp.edit().putString("api_key", etKey.getText().toString().trim()).apply();
            finish();
        });
    }
}
