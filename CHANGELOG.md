# 更新日志 (CHANGELOG)

本项目遵循「改动即记录」原则，版本号 `主.次.修订`，与 `app/build.gradle` 的 `versionName` 对齐。

## [1.4.0] - 2026-xx-xx

> 作者署名统一为 **文强哥 / Johnny520**，并进行了代码审查与若干修复、优化、美化。

### 署名整改（style）
- 新增 `README.md`，声明作者 / 版权人：文强哥 (Johnny520)，包名 `com.qxx.johnny` 与应用 ID 保持不变。
- 新增 `MIT LICENSE`，版权人 文强哥 (Johnny520)，年份 2026。
- 全部 13 个 Java 源文件顶部补充版权注释：`Copyright © 2026 文强哥 (Johnny520). All rights reserved.`
- `res/values/strings.xml` 新增作者相关字段：`app_author`、`about_developer`、`about_copyright`。
- 设置页「关于」卡片新增「开发者：文强哥（Johnny520）」与版权行，明确展示开发者署名。

### 缺陷修复（fix）
- **MainActivity 旋转/配置变更后底部导航回到「搜索」页**：新增 `selectedNavId` 并借助 `onSaveInstanceState` 保存/恢复当前选中的 Tab，重建后正确还原用户所在页面（`nav.setSelectedItemId`）。
- **CompanyFetcher 网络连接泄漏**：`getHtml` 改用 try-with-resources 自动关闭流，并在 `finally` 中 `disconnect()` 释放底层连接；非 200 时也直接返回，避免悬挂连接。
- **网络异常提示未利用**：`SearchFragment` 在发起请求前先做网络可用性预检，离线时直接展示 `net_error`（网络异常，请检查网络连接），不再做无意义的网络尝试。

### 优化（perf / refactor）
- `CompanyFetcher` 新增 `isNetworkAvailable(Context)` 静态方法（基于 `ConnectivityManager`，兼容 Android 6+），统一网络可用性判断。
- 移除 `CompanyFetcher` 中已不再使用的 `java.io.InputStream` 导入，保持 import 整洁。

### 美化（style / ui）
- 设置页「关于」卡片调整文案层级：简介增加行距，新增「开发者」加粗主色行与版权小字行，信息分区更清晰。
- 版本号对齐至 `1.4.0`（versionCode 4）。

## [1.3.0] - 历史版本
- 修复生命周期闪退、新增修复中心、优化启动图标，并补齐缺失 import / 字符串资源。

## [1.0.0] - 历史版本
- 企信查 Java 原生 Android 版初始发布（4 Tab / 关注 / 对比 / 修复中心 / 详情 / 法律文档）。
