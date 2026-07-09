package com.qxx.johnny.net;

import com.qxx.johnny.model.Company;

import java.io.BufferedReader;
import java.io.InputStream;
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
 * 企业数据抓取：默认走 Bing 网页抓取兜底，保证无 API Key 也能用。
 * 所有异常都被吞掉降级，绝不让调用方崩溃。
 */
public class CompanyFetcher {
    private static final String UA =
            "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile";
    private static final int TIMEOUT = 12000;

    private String getHtml(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", UA);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            int code = conn.getResponseCode();
            if (code != 200) {
                conn.disconnect();
                return null;
            }
            InputStream in = conn.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
            r.close();
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String stripTags(String s) {
        return s.replaceAll("<[^>]+>", "")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private boolean looksLikeCompany(String t) {
        if (t == null || t.length() < 2 || t.length() > 40) return false;
        if (t.contains(" ")) return false;
        return t.matches(".*(公司|集团|企业|厂|店|银行|股份|有限公司|责任|合伙|事务所|医院|学校|大学|学院).*");
    }

    public List<Company> search(String q) {
        List<Company> result = new ArrayList<>();
        try {
            String url = "https://www.bing.com/search?q=" +
                    URLEncoder.encode(q + " 企业 工商信息 天眼查", "UTF-8");
            String html = getHtml(url);
            if (html == null) return result;
            Set<String> names = new LinkedHashSet<>();
            Matcher m = Pattern.compile("<h2>(.*?)</h2>", Pattern.DOTALL).matcher(html);
            while (m.find()) {
                String t = stripTags(m.group(1));
                if (looksLikeCompany(t)) names.add(t);
            }
            for (String n : names) {
                if (result.size() >= 20) break;
                result.add(new Company(n));
            }
        } catch (Exception ignored) {
            // 降级为空结果
        }
        return result;
    }

    public Company getDetail(String name) {
        Company c = new Company(name);
        c.extra.put("提示", "未配置 API 密钥，仅展示基础信息。可在「设置」中填入免费 API 密钥以获取完整工商数据。");
        try {
            String url = "https://www.bing.com/search?q=" +
                    URLEncoder.encode(name + " 法定代表人 注册资本 成立日期", "UTF-8");
            String html = getHtml(url);
            if (html != null) extract(html, c);
        } catch (Exception ignored) {
            // 降级为基础对象
        }
        return c;
    }

    private void extract(String html, Company c) {
        Matcher credit = Pattern.compile("([0-9A-Z]{18})").matcher(html);
        if (credit.find()) c.extra.put("统一社会信用代码", credit.group(1));

        Matcher legal = Pattern.compile("法定代表人[：:]\\s*([一-龥]{2,4})").matcher(html);
        if (legal.find()) c.extra.put("法定代表人", legal.group(1));

        Matcher cap = Pattern.compile("注册资本[：:]\\s*([0-9.]+[万千万亿]?元?)").matcher(html);
        if (cap.find()) c.extra.put("注册资本", cap.group(1));

        Matcher date = Pattern.compile("成立日期[：:]\\s*([0-9]{4}[-年][0-9]{1,2}[-月][0-9]{1,2}日?)").matcher(html);
        if (date.find()) c.extra.put("成立日期", date.group(1));
    }
}
