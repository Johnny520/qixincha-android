package com.qxx.johnny.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Company {
    public String name;
    public Map<String, String> extra = new LinkedHashMap<>();

    public Company(String name) {
        this.name = name;
    }

    public String getLine() {
        if (extra.isEmpty()) return name;
        StringBuilder sb = new StringBuilder(name);
        for (Map.Entry<String, String> e : extra.entrySet()) {
            sb.append("\n").append(e.getKey()).append("：").append(e.getValue());
        }
        return sb.toString();
    }
}
