# 企信查 (qixincha-android)

企业工商信息查询 App（Android / Java 原生，Material 组件），类天眼查/爱企查风格的移动端工具。

- **包名 / 应用 ID**：`com.qxx.johnny`（保持不变，改了会破坏全部引用）
- **作者 / 版权人**：文强哥 (Johnny520)
- **兼容范围**：Android 7.0（API 24）~ Android 16（官方 SDK，无第三方运行时，闪退风险最低）
- **技术栈**：Android + Java 17 + Material Components + AndroidX

## 功能特性

- **搜索**：输入企业名称，通过公开搜索引擎兜底抓取，识别公司名 / 法人 / 注册资本 / 状态等。
- **详情**：统一社会信用代码、法定代表人、登记状态、注册资本、成立日期、注册地址、股东等结构化字段。
- **关注**：收藏常用企业，一键查看详情。
- **对比**：并排对比两家企业的工商信息。
- **设置**：可选填入免费 API 密钥（ApiByte / 聚合 / 极速 / XXApi）获取更完整数据；无密钥时走网页兜底抓取。
- **修复中心**：一键诊断网络 / 配置 / 缓存并自动修复。
- **法律文档**：内置免责声明、用户协议、隐私政策。

## 构建

```bash
# 需要 Android SDK（compileSdk 34）、JDK 17、Gradle 8.x
./gradlew assembleRelease
```

Release 包使用仓库 Secrets 中的正式签名密钥（SIGNING_KEY / KEY_ALIAS / KEY_STORE_PASSWORD / KEY_PASSWORD）签名，生成的 APK 可直接安装。

## 权限说明

- `INTERNET`：用于搜索/详情的网络请求（直接发往公开搜索引擎）。
- `ACCESS_NETWORK_STATE`：用于判断网络可用性，给出明确的网络异常提示。

> 本软件所展示的企业信息来自公开网页的自动抓取，数据准确性、完整性、及时性均不作保证，请以国家企业信用信息公示系统或企业官方披露为准。

## 作者与版权

Copyright © 2026 文强哥 (Johnny520). 保留所有权利。

- GitHub: https://github.com/Johnny520
- 本项目以 MIT 协议开源，详见 [LICENSE](LICENSE)。

## 许可证

[MIT](LICENSE) © 2026 文强哥 (Johnny520)
