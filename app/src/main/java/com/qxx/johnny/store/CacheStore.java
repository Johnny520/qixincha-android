package com.qxx.johnny.store;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内存缓存：对标 Flutter 的 CacheService。全程容错，损坏时标记 broken。
 */
public class CacheStore {
    private final Map<String, String> mem = new LinkedHashMap<>();
    public boolean broken = false;

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

    public synchronized int size() {
        return mem.size();
    }
}
