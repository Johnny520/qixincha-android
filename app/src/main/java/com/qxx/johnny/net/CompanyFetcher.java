/*
 * 企信查 (qixincha-android)
 * Copyright © 2026 文强哥 (Johnny520). All rights reserved.
 */

package com.qxx.johnny.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import com.qxx.johnny.R;
import com.qxx.johnny.model.Company;
import com.qxx.johnny.store.CacheStore;
import com.qxx.johnny.store.ConfigStore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 企业数据抓取：默认走 Bing 网页抓取兜底（对标 Flutter 的 ApiService 兜底逻辑）。
 * 修复点：
 *  - 桌面 UA，避免被跳 m.bing.com 导致结构与老正则对不上；
 *  - 剥离「天眼查/爱企查/百度百科」等站点后缀；
 *  - 放宽公司名识别（去掉"不能含空格"的死规则）；
 *  - 详情从 Bing 摘要正则抽取信用代码/法人/资本/成立日期/状态/地址/股东。
 * 所有异常都被吞掉降级，绝不让调用方崩溃。
 */
public class CompanyFetcher {
    private static final String UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";
    private static final int TIMEOUT = 12000;

    private final ConfigStore config;
    private final CacheStore cache;
    private final Context appCtx;
    private final String defaultTip;

    public CompanyFetcher(Context ctx, ConfigStore config, CacheStore cache) {
        this.config = config;
        this.cache = cache;
        this.appCtx = ctx.getApplicationContext();
        // 详情默认提示文案与 strings.xml 的 detail_tip_default 引用同一份，消除硬编码不一致
        String tip;
        try {
            tip = ctx.getApplicationContext().getString(R.string.detail_tip_default);
        } catch (Exception e) {
            tip = "未配置 API 密钥，仅展示基础信息（来自网页抓取）。可在「设置」中填入免费 API 密钥以获取完整工商数据。";
        }
        this.defaultTip = tip;
    }

    /** 判断当前是否有可用网络（需 ACCESS_NETWORK_STATE 权限，已在 Manifest 声明） */
    public static boolean isNetworkAvailable(Context ctx) {
        if (ctx == null) return false;
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network n = cm.getActiveNetwork();
                if (n == null) return false;
                NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                return ni != null && ni.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String getHtml(String urlStr) {
        if (!isNetworkAvailable(appCtx)) return null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", UA);
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            int code = conn.getResponseCode();
            if (code != 200) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            // try-with-resources 确保流自动关闭，finally 中 disconnect 释放底层连接
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ignore) {
                    // 忽略关闭异常
                }
            }
        }
    }

    private String stripTags(String s) {
        return s.replaceAll("<[^>]+>", "")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
                .trim();
    }

    /** 去掉「 - 天眼查」「_百度百科」等站点后缀 */
    private String cleanTitle(String t) {
        if (t == null) return "";
        String[] seps = {" - ", " _ ", " | ", " — ", "－", "–"};
        for (String sep : seps) {
            int idx = t.lastIndexOf(sep);
            if (idx > 0) {
                String right = t.substring(idx + sep.length());
                if (right.matches(".*(天眼查|爱企查|企查查|百度百科|百度知道|维基百科|官网|市场监督管理局|启信宝|水滴信用|企业预警通).*")) {
                    t = t.substring(0, idx);
                }
            }
        }
        return t.trim();
    }

    private boolean looksLikeCompany(String t) {
        if (t == null) return false;
        if (t.length() < 2 || t.length() > 40) return false;
        if (!t.matches(".*[\\u4e00-\\u9fa5]+.*")) return false; // 必须含中文
        return t.matches(".*(公司|集团|企业|厂|店|银行|股份|有限公司|责任|合伙|事务所|医院|学校|大学|学院|中心|合作社|工作室).*");
    }

    public List<Company> search(String q) {
        List<Company> result = new ArrayList<>();
        try {
            q = q.trim();
            if (q.isEmpty()) return result;

            String cacheKey = "search:" + q;
            if (cache.contains(cacheKey)) {
                String cached = cache.get(cacheKey);
                if (cached != null && !cached.isEmpty()) {
                    for (String n : cached.split("\u0001")) {
                        if (!n.isEmpty()) result.add(new Company(n));
                    }
                    if (!result.isEmpty()) return result;
                }
            }

            if (config.getBool("use_scrape_fallback", true)) {
                String url = "https://www.bing.com/search?q=" +
                        URLEncoder.encode(q + " 企业 工商信息 天眼查", "UTF-8");
                String html = getHtml(url);
                if (html != null) {
                    Set<String> names = new LinkedHashSet<>();
                    Matcher m = Pattern.compile("<h2>(.*?)</h2>", Pattern.DOTALL).matcher(html);
                    while (m.find()) {
                        String t = cleanTitle(stripTags(m.group(1)));
                        if (looksLikeCompany(t)) names.add(t);
                    }
                    // 兜底：直接抽 b_algo 内的标题链接
                    if (names.isEmpty()) {
                        Matcher m2 = Pattern.compile(
                                "class=\"b_algo\"[^>]*>.*?<a[^>]+href=\"[^\"]+\"[^>]*>(.*?)</a>",
                                Pattern.DOTALL).matcher(html);
                        while (m2.find()) {
                            String t = cleanTitle(stripTags(m2.group(1)));
                            if (looksLikeCompany(t)) names.add(t);
                        }
                    }
                    for (String n : names) {
                        if (result.size() >= 20) break;
                        result.add(new Company(n));
                    }
                    if (!result.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Company c : result) sb.append(c.name).append("\u0001");
                        cache.put(cacheKey, sb.toString());
                    }
                }
            }
        } catch (Exception ignored) {
            // 降级为空结果
        }
        return result;
    }

    public Company getDetail(String name) {
        Company c = new Company(name);
        c.extra.put("提示", defaultTip);
        try {
            String cacheKey = "detail:" + name;
            if (cache.contains(cacheKey)) {
                String raw = cache.get(cacheKey);
                if (raw != null && applyCached(c, raw)) return c;
            }
            if (config.getBool("use_scrape_fallback", true)) {
                String url = "https://www.bing.com/search?q=" +
                        URLEncoder.encode(name + " 法定代表人 注册资本 成立日期 登记状态 注册地址 股东", "UTF-8");
                String html = getHtml(url);
                if (html != null) extract(html, c);
                cache.put(cacheKey, serialize(c));
            }
        } catch (Exception ignored) {
            // 降级为基础对象
        }
        return c;
    }

    private void extract(String html, Company c) {
        Matcher credit = Pattern.compile("([0-9A-Z]{18})").matcher(html);
        if (credit.find()) c.creditCode = credit.group(1);

        Matcher legal = Pattern.compile("法定代表人[：:]\\s*([\u4e00-\u9fa5·]{2,5})").matcher(html);
        if (legal.find()) c.legalPerson = legal.group(1);

        Matcher cap = Pattern.compile("注册资本[：:]\\s*([0-9.]+[万千万亿]?元?)").matcher(html);
        if (cap.find()) c.registeredCapital = cap.group(1);

        Matcher date = Pattern.compile("成立日期[：:]\\s*([0-9]{4}[-年][0-9]{1,2}[-月][0-9]{1,2}日?)").matcher(html);
        if (date.find()) c.establishDate = date.group(1);

        Matcher status = Pattern.compile("登记状态[：:]\\s*([\u4e00-\u9fa5]{2,6})").matcher(html);
        if (status.find()) c.status = status.group(1);

        Matcher addr = Pattern.compile("注册地址[：:]\\s*([\u4e00-\u9fa50-9a-zA-Z（）()号路街区栋室楼层.#\\-]{4,40})").matcher(html);
        if (addr.find()) c.regAddress = addr.group(1);

        // 股东（尽力而为，多匹配）
        Matcher sh = Pattern.compile("股东[：:：]?\\s*([\u4e00-\u9fa5·]{2,5}(?:、[\u4e00-\u9fa5·]{2,5}){0,5})").matcher(html);
        while (sh.find() && c.shareholders.size() < 10) c.shareholders.add(sh.group(1));
    }

    private String serialize(Company c) {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(c.name).append("\n");
        if (c.creditCode != null) sb.append("creditCode=").append(c.creditCode).append("\n");
        if (c.legalPerson != null) sb.append("legalPerson=").append(c.legalPerson).append("\n");
        if (c.status != null) sb.append("status=").append(c.status).append("\n");
        if (c.registeredCapital != null) sb.append("registeredCapital=").append(c.registeredCapital).append("\n");
        if (c.establishDate != null) sb.append("establishDate=").append(c.establishDate).append("\n");
        if (c.regAddress != null) sb.append("regAddress=").append(c.regAddress).append("\n");
        return sb.toString();
    }

    private boolean applyCached(Company c, String raw) {
        try {
            for (String line : raw.split("\n")) {
                int i = line.indexOf('=');
                if (i < 0) continue;
                String k = line.substring(0, i);
                String v = line.substring(i + 1);
                switch (k) {
                    case "creditCode": c.creditCode = v; break;
                    case "legalPerson": c.legalPerson = v; break;
                    case "status": c.status = v; break;
                    case "registeredCapital": c.registeredCapital = v; break;
                    case "establishDate": c.establishDate = v; break;
                    case "regAddress": c.regAddress = v; break;
                    default: break;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
