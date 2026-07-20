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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qxx.johnny.CompanyCardAdapter;
import com.qxx.johnny.DetailActivity;
import com.qxx.johnny.MainActivity;
import com.qxx.johnny.R;
import com.qxx.johnny.model.Company;
import com.qxx.johnny.store.ConfigStore;

import java.util.ArrayList;
import java.util.List;

public class FollowFragment extends Fragment {
    private ListView listView;
    private TextView tvEmpty;
    private CompanyCardAdapter adapter;

    public FollowFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_follow, container, false);
        listView = v.findViewById(R.id.list_view);
        tvEmpty = v.findViewById(R.id.tv_empty);
        adapter = new CompanyCardAdapter(requireContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Company c = adapter.getItem(position);
            if (c != null) {
                Intent i = new Intent(requireActivity(), DetailActivity.class);
                i.putExtra("name", c.name);
                startActivity(i);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        ConfigStore config = ((MainActivity) requireActivity()).getConfig();
        List<String> names = config.getFollowList();
        List<Company> list = new ArrayList<>();
        for (String n : names) list.add(new Company(n));

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(getString(R.string.follow_empty) + "\n" + getString(R.string.follow_empty_sub));
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter.setData(list);
        }
    }
}
