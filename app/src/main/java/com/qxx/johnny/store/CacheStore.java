/*
 * 企信查 (qixincha-android)
 * Copyright © 2026 文强哥 (Johnny520). All rights reserved.
 */

package com.qxx.johnny.store;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内存缓存：对标 Flutter 的 CacheService。全程容错，损坏时标记 broken。
 * 改为单例，使 MainActivity 与 DetailActivity 共享同一份缓存（避免详情缓存永远为空）。
 */
public class CacheStore {
    private static volatile CacheStore sInstance;

    /** 获取全局共享缓存实例 */
    public static CacheStore getInstance() {
        if (sInstance == null) {
            synchronized (CacheStore.class) {
                if (sInstance == null) sInstance = new CacheStore();
            }
        }
        return sInstance;
    }

    private final Map<String, String> mem = new LinkedHashMap<>();
    public boolean broken = false;

    private CacheStore() {
    }

    public synchronized void put(String key, String value) {
        try {
            mem.put(key, value);
        } catch (Exception e) {
            broken = true;
        }
    }

    public synchronized String get(String key) {
        try {
            return mem.get(key);
        } catch (Exception e) {
            broken = true;
            return null;
        }
    }

    public synchronized boolean contains(String key) {
        try {
            return mem.containsKey(key);
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void clear() {
        try {
            mem.clear();
            broken = false;
        } catch (Exception e) {
            broken = true;
        }
    }

    /** 清理全部本地缓存（设置页调用），并复位 broken 标记 */
    public synchronized void clearAll() {
        clear();
    }

    /** 当前缓存条目数（修复中心 size>200 清理分支使用） */
    public synchronized int size() {
        return mem.size();
    }
}
