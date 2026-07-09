package com.qxx.johnny.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.qxx.johnny.CompanyCardAdapter;
import com.qxx.johnny.DetailActivity;
import com.qxx.johnny.MainActivity;
import com.qxx.johnny.R;
import com.qxx.johnny.model.Company;
import com.qxx.johnny.net.CompanyFetcher;

import java.util.List;

public class SearchFragment extends Fragment {
    private EditText etQuery;
    private MaterialButton btnSearch;
    private TextView tvStatus;
    private ListView listView;
    private CompanyCardAdapter adapter;

    public SearchFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        etQuery = v.findViewById(R.id.et_query);
        btnSearch = v.findViewById(R.id.btn_search);
        tvStatus = v.findViewById(R.id.tv_status);
        listView = v.findViewById(R.id.list_view);

        adapter = new CompanyCardAdapter(requireContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Company c = adapter.getItem(position);
            if (c != null) openDetail(c.name);
        });

        btnSearch.setOnClickListener(view -> doSearch());
        etQuery.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
        return v;
    }

    private void doSearch() {
        String q = etQuery.getText().toString().trim();
        if (TextUtils.isEmpty(q)) return;
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(R.string.loading);
        listView.setVisibility(View.GONE);

        new Thread(() -> {
            CompanyFetcher fetcher = ((MainActivity) requireActivity()).getFetcher();
            final List<Company> list = fetcher.search(q);
            requireActivity().runOnUiThread(() -> {
                if (list.isEmpty()) {
                    tvStatus.setText(R.string.no_result);
                    listView.setVisibility(View.GONE);
                } else {
                    tvStatus.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    adapter.setData(list);
                }
            });
        }).start();
    }

    private void openDetail(String name) {
        Intent i = new Intent(requireActivity(), DetailActivity.class);
        i.putExtra("name", name);
        startActivity(i);
    }
}
