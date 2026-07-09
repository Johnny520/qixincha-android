package com.qxx.johnny;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.qxx.johnny.fragments.CompareFragment;
import com.qxx.johnny.fragments.FollowFragment;
import com.qxx.johnny.fragments.SearchFragment;
import com.qxx.johnny.fragments.SettingsFragment;
import com.qxx.johnny.net.CompanyFetcher;
import com.qxx.johnny.store.CacheStore;
import com.qxx.johnny.store.ConfigStore;
import com.qxx.johnny.store.RepairCenter;

public class MainActivity extends AppCompatActivity {
    private ConfigStore config;
    private CacheStore cache;
    private CompanyFetcher fetcher;
    private RepairCenter repair;

    private SearchFragment fSearch;
    private FollowFragment fFollow;
    private CompareFragment fCompare;
    private SettingsFragment fSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        config = new ConfigStore(this);
        cache = CacheStore.getInstance();
        fetcher = new CompanyFetcher(this, config, cache);
        repair = new RepairCenter(config, cache);

        // 启动自动修复（网络/配置/缓存），修复了才提示
        new Thread(() -> {
            final java.util.List<RepairCenter.RepairResult> fixes = repair.autoRepair();
            runOnUiThread(() -> {
                if (!fixes.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.startup_fixed, fixes.size()), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();

        FragmentManager fm = getSupportFragmentManager();
        fSearch = (SearchFragment) fm.findFragmentByTag("search");
        fFollow = (FollowFragment) fm.findFragmentByTag("follow");
        fCompare = (CompareFragment) fm.findFragmentByTag("compare");
        fSettings = (SettingsFragment) fm.findFragmentByTag("settings");

        FragmentTransaction ft = fm.beginTransaction();
        if (fSearch == null) {
            fSearch = new SearchFragment();
            ft.add(R.id.nav_host, fSearch, "search");
        }
        if (fFollow == null) {
            fFollow = new FollowFragment();
            ft.add(R.id.nav_host, fFollow, "follow");
        }
        if (fCompare == null) {
            fCompare = new CompareFragment();
            ft.add(R.id.nav_host, fCompare, "compare");
        }
        if (fSettings == null) {
            fSettings = new SettingsFragment();
            ft.add(R.id.nav_host, fSettings, "settings");
        }
        ft.hide(fFollow);
        ft.hide(fCompare);
        ft.hide(fSettings);
        ft.show(fSearch);
        // 提交改用 commitAllowingStateLoss，避免 onSaveInstanceState 之后提交抛 IllegalStateException
        ft.commitAllowingStateLoss();

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_search) show(fSearch);
            else if (id == R.id.nav_follow) show(fFollow);
            else if (id == R.id.nav_compare) show(fCompare);
            else if (id == R.id.nav_settings) show(fSettings);
            return true;
        });
    }

    private void show(Fragment f) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(fSearch);
        ft.hide(fFollow);
        ft.hide(fCompare);
        ft.hide(fSettings);
        ft.show(f);
        // 提交改用 commitAllowingStateLoss，避免 onSaveInstanceState 之后提交抛 IllegalStateException
        ft.commitAllowingStateLoss();
    }

    public ConfigStore getConfig() {
        return config;
    }

    public CacheStore getCache() {
        return cache;
    }

    public CompanyFetcher getFetcher() {
        return fetcher;
    }
}
