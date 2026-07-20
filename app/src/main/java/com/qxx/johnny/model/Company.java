/*
 * 企信查 (qixincha-android)
 * Copyright © 2026 文强哥 (Johnny520). All rights reserved.
 */

package com.qxx.johnny.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业实体：对标 Flutter 的 Company + Python 的 data_sources 详情结构。
 * 结构化字段 + extra + 扩展区块（股东/变更/高管/投资/分支），有数据才展示。
 */
public class Company {
    public String name;
    public String creditCode;
    public String legalPerson;
    public String status;
    public String registeredCapital;
    public String establishDate;
    public String regAddress;
    public Map<String, String> extra = new LinkedHashMap<>();

    // 扩展区块（Python 版 detail 结构）
    public List<String> shareholders = new ArrayList<>();
    public List<String> changes = new ArrayList<>();
    public List<String> keyPersons = new ArrayList<>();
    public List<String> investments = new ArrayList<>();
    public List<String> branches = new ArrayList<>();
    public List<String> sources = new ArrayList<>();

    public Company(String name) {
        this.name = name;
    }

    /** 是否有结构化工商字段 */
    public boolean hasStructured() {
        return creditCode != null || legalPerson != null || status != null
                || registeredCapital != null || establishDate != null
                || regAddress != null || !extra.isEmpty();
    }

    /** 是否有扩展区块 */
    public boolean hasBlocks() {
        return !shareholders.isEmpty() || !changes.isEmpty() || !keyPersons.isEmpty()
                || !investments.isEmpty() || !branches.isEmpty();
    }
}
