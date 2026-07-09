package com.qxx.johnny;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.qxx.johnny.model.Company;
import com.qxx.johnny.net.CompanyFetcher;

public class DetailActivity extends AppCompatActivity {
    private TextView tvContent;
    private ProgressDialog pd;
    private final CompanyFetcher fetcher = new CompanyFetcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        tvContent = findViewById(R.id.tv_detail);

        String name = getIntent().getStringExtra("name");
        if (TextUtils.isEmpty(name)) {
            finish();
            return;
        }
        setTitle(name);

        pd = ProgressDialog.show(this, null, getString(R.string.loading), true);
        new Thread(() -> {
            final Company c = fetcher.getDetail(name);
            runOnUiThread(() -> {
                if (pd != null && pd.isShowing()) pd.dismiss();
                tvContent.setText(c.getLine());
            });
        }).start();
    }
}
