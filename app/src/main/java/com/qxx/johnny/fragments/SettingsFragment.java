/*
 * 企信查 (qixincha-android)
 * Copyright © 2026 文强哥 (Johnny520). All rights reserved.
 */

package com.qxx.johnny.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.qxx.johnny.InfoActivity;
import com.qxx.johnny.MainActivity;
import com.qxx.johnny.R;
import com.qxx.johnny.store.ConfigStore;
import com.qxx.johnny.store.RepairCenter;

import java.util.List;

public class SettingsFragment extends Fragment {
    private EditText etApibyte;
    private EditText etJuhe;
    private EditText etJisu;
    private EditText etXxapi;
    private Switch swScrape;
    private MaterialButton btnSave;
    private MaterialButton btnRepair;
    private MaterialButton btnClearCache;
    private MaterialButton btnReset;
    private TextView tvVersion;
    private TextView tvDisclaimer;
    private TextView tvAgreement;
    private TextView tvPrivacy;

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        etApibyte = v.findViewById(R.id.et_apibyte);
        etJuhe = v.findViewById(R.id.et_juhe);
        etJisu = v.findViewById(R.id.et_jisu);
        etXxapi = v.findViewById(R.id.et_xxapi);
        swScrape = v.findViewById(R.id.sw_scrape);
        btnSave = v.findViewById(R.id.btn_save);
        btnRepair = v.findViewById(R.id.btn_repair);
        btnClearCache = v.findViewById(R.id.btn_clear_cache);
        btnReset = v.findViewById(R.id.btn_reset);
        tvVersion = v.findViewById(R.id.tv_version);
        tvDisclaimer = v.findViewById(R.id.tv_disclaimer);
        tvAgreement = v.findViewById(R.id.tv_agreement);
        tvPrivacy = v.findViewById(R.id.tv_privacy);

        ConfigStore config = ((MainActivity) requireActivity()).getConfig();
        etApibyte.setText(config.getString("apibyte_key", ""));
        etJuhe.setText(config.getString("juhe_key", ""));
        etJisu.setText(config.getString("jisu_key", ""));
        etXxapi.setText(config.getString("xxapi_key", ""));
        swScrape.setChecked(config.getBool("use_scrape_fallback", true));

        tvVersion.setText(getString(R.string.version_label) + " " + getString(R.string.app_version));

        btnSave.setOnClickListener(view -> {
            config.setString("apibyte_key", etApibyte.getText().toString().trim());
            config.setString("juhe_key", etJuhe.getText().toString().trim());
            config.setString("jisu_key", etJisu.getText().toString().trim());
            config.setString("xxapi_key", etXxapi.getText().toString().trim());
            config.setBool("use_scrape_fallback", swScrape.isChecked());
            Toast.makeText(getContext(), R.string.save, Toast.LENGTH_SHORT).show();
        });

        btnRepair.setOnClickListener(view -> runRepair());

        // 清理本地缓存（对标 Flutter 设置页「数据与缓存」）
        btnClearCache.setOnClickListener(view -> {
            MainActivity act = (MainActivity) requireActivity();
            act.getCache().clearAll();
            Toast.makeText(getContext(), R.string.settings_cleared, Toast.LENGTH_SHORT).show();
        });

        // 重置所有设置（API 密钥/兜底开关/关注列表等恢复默认）
        btnReset.setOnClickListener(view -> {
            ((MainActivity) requireActivity()).getConfig().reset();
            Toast.makeText(getContext(), R.string.settings_reset_done, Toast.LENGTH_SHORT).show();
        });

        tvDisclaimer.setOnClickListener(view -> openInfo(R.string.disclaimer_title, R.string.disclaimer_text));
        tvAgreement.setOnClickListener(view -> openInfo(R.string.agreement_title, R.string.agreement_text));
        tvPrivacy.setOnClickListener(view -> openInfo(R.string.privacy_title, R.string.privacy_text));

        return v;
    }

    private void openInfo(int titleRes, int textRes) {
        Intent i = new Intent(requireActivity(), InfoActivity.class);
        i.putExtra("title", getString(titleRes));
        i.putExtra("text", getString(textRes));
        startActivity(i);
    }

    private void runRepair() {
        new Thread(() -> {
            // 后台线程：先守卫，避免 Fragment 已 detached 后 requireActivity() 抛 IllegalStateException
            if (getActivity() == null || isDetached()) return;
            MainActivity act = (MainActivity) requireActivity();
            RepairCenter repair = new RepairCenter(act.getConfig(), act.getCache());
            final List<RepairCenter.RepairResult> report = repair.runRepair();
            requireActivity().runOnUiThread(() -> {
                // 回到 UI 线程：用户已退出则直接返回，避免闪退
                if (getActivity() == null || isDetached()) return;
                showReport(report);
            });
        }).start();
    }

    private void showReport(List<RepairCenter.RepairResult> report) {
        StringBuilder sb = new StringBuilder();
        for (RepairCenter.RepairResult r : report) {
            String mark = r.ok ? "✓ " : (r.fixed ? "🔧 " : "✗ ");
            sb.append(mark).append(r.name).append("：").append(r.detail).append("\n");
        }
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.settings_repair_report)
                .setMessage(sb.toString().trim())
                .setPositiveButton(R.string.back, null)
                .show();
    }
}
