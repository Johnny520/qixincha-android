/*
 * 企信查 (qixincha-android)
 * Copyright © 2026 文强哥 (Johnny520). All rights reserved.
 */

package com.qxx.johnny;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.qxx.johnny.model.Company;
import com.qxx.johnny.net.CompanyFetcher;
import com.qxx.johnny.store.CacheStore;
import com.qxx.johnny.store.ConfigStore;

import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    private LinearLayout container;
    private ConfigStore config;
    private CacheStore cache;
    private CompanyFetcher fetcher;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        name = getIntent().getStringExtra("name");
        if (name == null || name.isEmpty()) {
            finish();
            return;
        }

        // 当前主题为 NoActionBar，手动挂接 MaterialToolbar 提供可见标题与返回按钮
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(name);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        container = findViewById(R.id.container);

        config = new ConfigStore(this);
        cache = CacheStore.getInstance();
        fetcher = new CompanyFetcher(this, config, cache);

        ProgressDialog pd = ProgressDialog.show(this, null, getString(R.string.loading), true);
        new Thread(() -> {
            final Company c = fetcher.getDetail(name);
            runOnUiThread(() -> {
                // 窗口已销毁后 dismiss 可能抛 IllegalArgumentException 或泄漏，需守卫并 try/catch
                if (pd != null && pd.isShowing()) {
                    try {
                        if (!isFinishing() && !isDestroyed()) pd.dismiss();
                    } catch (IllegalArgumentException ignore) {
                        // 窗口已 detach，忽略
                    }
                }
                // Activity 已销毁则不再渲染，避免崩溃
                if (isFinishing() || isDestroyed()) return;
                render(c);
            });
        }).start();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private MaterialCardView newCard() {
        MaterialCardView cv = new MaterialCardView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        cv.setLayoutParams(lp);
        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card));
        cv.setRadius(dp(14));
        cv.setCardElevation(dp(2));
        cv.setContentPadding(dp(16), dp(16), dp(16), dp(16));
        return cv;
    }

    private void render(Company c) {
        container.removeAllViews();
        container.addView(buildHeader(c));
        if (c.hasStructured()) container.addView(buildFieldCard(c));
        addBlock(container, R.string.detail_shareholders, c.shareholders);
        addBlock(container, R.string.detail_changes, c.changes);
        addBlock(container, R.string.detail_keypersons, c.keyPersons);
        addBlock(container, R.string.detail_investments, c.investments);
        addBlock(container, R.string.detail_branches, c.branches);
        if (c.extra.containsKey("提示")) {
            container.addView(buildTipCard(c.extra.get("提示")));
        }
    }

    private View buildHeader(Company c) {
        MaterialCardView cv = newCard();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView nameTv = new TextView(this);
        nameTv.setText(c.name);
        nameTv.setTextSize(20);
        nameTv.setTypeface(null, Typeface.BOLD);
        nameTv.setTextColor(ContextCompat.getColor(this, R.color.text));
        root.addView(nameTv);

        if (c.status != null) {
            TextView status = new TextView(this);
            status.setText(c.status);
            status.setTextSize(13);
            status.setTextColor(ContextCompat.getColor(this, R.color.ok));
            status.setTypeface(null, Typeface.BOLD);
            status.setPadding(0, 8, 0, 0);
            root.addView(status);
        }

        MaterialButton btn = new MaterialButton(this);
        boolean followed = config.isFollowed(c.name);
        btn.setText(followed ? R.string.follow_remove : R.string.follow_add);
        btn.setPadding(0, dp(8), 0, 0);
        btn.setOnClickListener(v -> {
            boolean now = config.toggleFollow(c.name);
            btn.setText(now ? R.string.follow_remove : R.string.follow_add);
        });
        root.addView(btn);

        cv.addView(root);
        return cv;
    }

    private View buildFieldCard(Company c) {
        MaterialCardView cv = newCard();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        addField(root, R.string.detail_credit, c.creditCode);
        addField(root, R.string.detail_legal, c.legalPerson);
        addField(root, R.string.detail_status, c.status);
        addField(root, R.string.detail_capital, c.registeredCapital);
        addField(root, R.string.detail_date, c.establishDate);
        addField(root, R.string.detail_addr, c.regAddress);
        for (Map.Entry<String, String> e : c.extra.entrySet()) {
            if (!"提示".equals(e.getKey())) addField(root, e.getKey(), e.getValue());
        }
        cv.addView(root);
        return cv;
    }

    private void addField(LinearLayout root, int labelRes, String value) {
        if (value == null || value.isEmpty()) return;
        addField(root, getString(labelRes), value);
    }

    private void addField(LinearLayout root, String label, String value) {
        if (value == null || value.isEmpty()) return;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView k = new TextView(this);
        k.setText(label);
        k.setTextSize(14);
        k.setTextColor(ContextCompat.getColor(this, R.color.sub));
        LinearLayout.LayoutParams kp = new LinearLayout.LayoutParams(dp(110), LinearLayout.LayoutParams.WRAP_CONTENT);
        k.setLayoutParams(kp);

        TextView val = new TextView(this);
        val.setText(value);
        val.setTextSize(14);
        val.setTextColor(ContextCompat.getColor(this, R.color.text));
        val.setTypeface(null, Typeface.BOLD);

        row.addView(k);
        row.addView(val);
        root.addView(row);
    }

    private void addBlock(LinearLayout container, int titleRes, List<String> items) {
        if (items == null || items.isEmpty()) return;
        MaterialCardView cv = newCard();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText(getString(titleRes));
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(this, R.color.primary));
        title.setPadding(0, 0, 0, dp(8));
        root.addView(title);

        for (String it : items) {
            TextView line = new TextView(this);
            line.setText("· " + it);
            line.setTextSize(14);
            line.setTextColor(ContextCompat.getColor(this, R.color.text));
            line.setPadding(0, dp(4), 0, dp(4));
            root.addView(line);
        }
        cv.addView(root);
        container.addView(cv);
    }

    private View buildTipCard(String tip) {
        MaterialCardView cv = newCard();
        TextView t = new TextView(this);
        t.setText(tip);
        t.setTextSize(13);
        t.setTextColor(ContextCompat.getColor(this, R.color.sub));
        t.setLineSpacing(dp(4), 1f);
        cv.addView(t);
        return cv;
    }
}
