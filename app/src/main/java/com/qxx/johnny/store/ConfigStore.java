/*
 * 企信查 (qixincha-android)
 * Copyright © 2026 文强哥 (Johnny520). All rights reserved.
 */

package com.qxx.johnny.store;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 配置存储：对标 Flutter 的 ConfigService。
 * 统一读写用户设置（API 密钥、兜底开关、主题、关注列表），全程容错。
 */
public class ConfigStore {
    private static final String PREF = "qxc";

    private final SharedPreferences sp;

    public ConfigStore(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public String getString(String key, String def) {
        try {
            String v = sp.getString(key, def);
            return v == null ? def : v;
        } catch (Exception e) {
            return def;
        }
    }

    public void setString(String key, String v) {
        try {
            sp.edit().putString(key, v == null ? "" : v).apply();
        } catch (Exception ignore) {
        }
    }

    public boolean getBool(String key, boolean def) {
        try {
            return sp.getBoolean(key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public void setBool(String key, boolean v) {
        try {
            sp.edit().putBoolean(key, v).apply();
        } catch (Exception ignore) {
        }
    }

    /** 关注列表（去重副本，避免持有 SharedPreferences 内部可变集合） */
    public List<String> getFollowList() {
        try {
            Set<String> s = sp.getStringSet("follow_list", new HashSet<>());
            return new ArrayList<>(s);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public boolean isFollowed(String name) {
        try {
            return sp.getStringSet("follow_list", new HashSet<>()).contains(name);
        } catch (Exception e) {
            return false;
        }
    }

    /** 关注/取消，返回操作后是否处于关注态 */
    public boolean toggleFollow(String name) {
        try {
            Set<String> s = new HashSet<>(sp.getStringSet("follow_list", new HashSet<>()));
            if (s.contains(name)) s.remove(name);
            else s.add(name);
            sp.edit().putStringSet("follow_list", s).apply();
            return s.contains(name);
        } catch (Exception e) {
            return false;
        }
    }

    /** 重置为默认（修复中心调用） */
    public void reset() {
        try {
            SharedPreferences.Editor e = sp.edit();
            e.remove("apibyte_key");
            e.remove("jisu_key");
            e.remove("juhe_key");
            e.remove("xxapi_key");
            e.remove("use_scrape_fallback");
            e.remove("theme_blue");
            e.remove("follow_list");
            e.apply();
        } catch (Exception ignore) {
        }
    }

    /**
     * 真正检测配置是否损坏（供 RepairCenter.checkConfig 使用）：
     *  1) 类型校验：关键 key（如 follow_list）存在但类型异常（非 Set）即视为损坏；
     *  2) 读写回环：写测试值再读回比对，SharedPreferences 不可写/读即视为损坏。
     * 未写入的 key 返回 null 属首装正常，不算损坏。
     */
    public boolean isCorrupted() {
        try {
            Object follow = sp.getAll().get("follow_list");
            if (follow != null && !(follow instanceof Set)) return true;

            String probe = "qxc_probe_" + System.currentTimeMillis();
            SharedPreferences.Editor e = sp.edit();
            e.putString("__qxc_probe__", probe);
            if (!e.commit()) return true;
            String back = sp.getString("__qxc_probe__", null);
            sp.edit().remove("__qxc_probe__").commit();
            return !probe.equals(back);
        } catch (Exception ex) {
            return true;
        }
    }
}
