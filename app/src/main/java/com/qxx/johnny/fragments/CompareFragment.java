package com.qxx.johnny.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.qxx.johnny.MainActivity;
import com.qxx.johnny.R;
import com.qxx.johnny.model.Company;
import com.qxx.johnny.net.CompanyFetcher;

import java.util.Map;

public class CompareFragment extends Fragment {
    private EditText etA;
    private EditText etB;
    private ProgressBar pb;
    private LinearLayout colA;
    private LinearLayout colB;

    public CompareFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_compare, container, false);
        etA = v.findViewById(R.id.et_a);
        etB = v.findViewById(R.id.et_b);
        pb = v.findViewById(R.id.pb);
        colA = v.findViewById(R.id.col_a);
        colB = v.findViewById(R.id.col_b);
        v.findViewById(R.id.btn_compare).setOnClickListener(view -> doCompare());
        return v;
    }

    private void doCompare() {
        String a = etA.getText().toString().trim();
        String b = etB.getText().toString().trim();
        if (a.isEmpty() || b.isEmpty()) {
            if (getContext() == null) return;
            colA.removeAllViews();
            colB.removeAllViews();
            addInfo(colA, getString(R.string.compare_empty));
            return;
        }
        pb.setVisibility(View.VISIBLE);
        colA.removeAllViews();
        colB.removeAllViews();

        new Thread(() -> {
            // 后台线程：先守卫，避免 Fragment 已 detached 后 requireActivity() 抛 IllegalStateException
            if (getContext() == null || isDetached() || isRemoving()) return;
            CompanyFetcher f = ((MainActivity) requireActivity()).getFetcher();
            final Company ca = f.getDetail(a);
            final Company cb = f.getDetail(b);
            requireActivity().runOnUiThread(() -> {
                // 回到 UI 线程：Fragment 已 detached/销毁时直接返回，避免崩溃
                if (getContext() == null || isDetached() || isRemoving()) return;
                pb.setVisibility(View.GONE);
                fill(colA, ca);
                fill(colB, cb);
            });
        }).start();
    }

    private void fill(LinearLayout col, Company c) {
        // fill 内改用 getContext() 并判空，杜绝 detached 后 requireContext() 抛异常
        Context ctx = getContext();
        if (ctx == null) return;
        col.removeAllViews();
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_edittext);
        card.setPadding(16, 16, 16, 16);
        addRow(card, c.name, true);
        if (c.hasStructured()) {
            if (c.status != null) addRow(card, "登记状态：" + c.status, false);
            if (c.legalPerson != null) addRow(card, "法定代表人：" + c.legalPerson, false);
            if (c.registeredCapital != null) addRow(card, "注册资本：" + c.registeredCapital, false);
            if (c.establishDate != null) addRow(card, "成立日期：" + c.establishDate, false);
            if (c.creditCode != null) addRow(card, "信用代码：" + c.creditCode, false);
            if (c.regAddress != null) addRow(card, "注册地址：" + c.regAddress, false);
        } else {
            addRow(card, getString(R.string.compare_no_data), false);
        }
        // 一并展示 extra（数据来源/提示等），对标 Flutter compare_screen
        for (Map.Entry<String, String> e : c.extra.entrySet()) {
            addRow(card, e.getKey() + "：" + e.getValue(), false);
        }
        col.addView(card);
    }

    private void addInfo(LinearLayout col, String text) {
        Context ctx = getContext();
        if (ctx == null) return;
        TextView t = new TextView(ctx);
        t.setText(text);
        t.setTextColor(ContextCompat.getColor(ctx, R.color.sub));
        col.addView(t);
    }

    private void addRow(LinearLayout card, String text, boolean bold) {
        Context ctx = getContext();
        if (ctx == null) return;
        TextView t = new TextView(ctx);
        t.setText(text);
        t.setTextSize(bold ? 17 : 14);
        t.setTextColor(ContextCompat.getColor(ctx, bold ? R.color.text : R.color.sub));
        if (bold) t.setPadding(0, 0, 0, 8);
        else t.setPadding(0, 6, 0, 6);
        card.addView(t);
    }
}
