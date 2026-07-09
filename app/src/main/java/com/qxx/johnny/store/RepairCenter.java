package com.qxx.johnny.store;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 修复中心：对标 Flutter 的 RepairService。
 * 涵盖网络/配置/缓存三类诊断 + 启动自动修复 + 手动一键修复。任何异常都不抛出。
 */
public class RepairCenter {

    public static class RepairResult {
        public final String name;
        public final boolean ok;
        public final String detail;
        public final boolean fixed;

        public RepairResult(String name, boolean ok, String detail) {
            this(name, ok, detail, false);
        }

        public RepairResult(String name, boolean ok, String detail, boolean fixed) {
            this.name = name;
            this.ok = ok;
            this.detail = detail;
            this.fixed = fixed;
        }
    }

    private final ConfigStore config;
    private final CacheStore cache;

    public RepairCenter(ConfigStore config, CacheStore cache) {
        this.config = config;
        this.cache = cache;
    }

    /** 网络连通性检测 */
    public RepairResult checkNetwork() {
        try {
            URL u = new URL("https://www.bing.com");
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestProperty("User-Agent", "Mozilla/5.0");
            c.setConnectTimeout(8000);
            c.setReadTimeout(8000);
            int code = c.getResponseCode();
            c.disconnect();
            if (code == 200) return new RepairResult("网络连通性", true, "网络正常，可访问外网。");
            return new RepairResult("网络连通性", false, "服务器返回 " + code + "，但可连通。");
        } catch (Exception e) {
            return new RepairResult("网络连通性", false, "无法连接网络，请检查 Wi-Fi/移动数据。");
        }
    }

    /** 配置完整性检测 */
    public RepairResult checkConfig() {
        try {
            config.getFollowList();
            config.getString("apibyte_key", "");
            return new RepairResult("配置文件", true, "配置读写正常。");
        } catch (Exception e) {
            return new RepairResult("配置文件", false, "配置读取失败：" + e.getMessage());
        }
    }

    /** 缓存检测 */
    public RepairResult checkCache() {
        if (cache.broken) return new RepairResult("本地缓存", false, "缓存状态异常，建议清理。");
        return new RepairResult("本地缓存", true, "缓存正常（当前 " + cache.size() + " 条）。");
    }

    public List<RepairResult> diagnose() {
        List<RepairResult> r = new ArrayList<>();
        r.add(checkNetwork());
        r.add(checkConfig());
        r.add(checkCache());
        return r;
    }

    /** 启动/出错时静默自动修复，返回真正执行了的修复项 */
    public List<RepairResult> autoRepair() {
        List<RepairResult> fixed = new ArrayList<>();
        if (!checkConfig().ok) {
            config.reset();
            fixed.add(new RepairResult("配置文件", true, "检测到配置损坏，已重置为默认设置。", true));
        }
        if (cache.broken) {
            cache.clear();
            fixed.add(new RepairResult("本地缓存", !cache.broken,
                    !cache.broken ? "检测到缓存异常，已清理本地缓存。" : "缓存清理失败。", true));
        }
        return fixed;
    }

    /** 手动一键修复：完整诊断 + 执行可修复项 */
    public List<RepairResult> runRepair() {
        List<RepairResult> report = new ArrayList<>();
        report.add(checkNetwork());

        RepairResult cfg = checkConfig();
        if (!cfg.ok) {
            config.reset();
            report.add(new RepairResult("配置文件", true, "已重置为默认设置。", true));
        } else {
            report.add(new RepairResult("配置文件", true, "无需修复。"));
        }

        if (cache.broken || cache.size() > 200) {
            cache.clear();
            report.add(new RepairResult("本地缓存", !cache.broken,
                    !cache.broken ? "已清理本地缓存（" + cache.size() + " 条）。" : "缓存清理失败。", true));
        } else {
            report.add(new RepairResult("本地缓存", true, "无需修复。"));
        }
        return report;
    }
}
