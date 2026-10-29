# 人生哲学

> 纯离线阅读 App：767 条可验证的人生建议，用最少的钱、时间和精力，换回寿命、金钱和自由。

一套代码，三端运行：**Android APK / iOS / 桌面浏览器（PWA）**。内容 100% 打包内置，首次启动零网络请求；本机 localStorage 缓存收藏与阅读进度；原生层实现层级返回 + 双击退出。

内容来源：[HowToLiveBetter](https://github.com/eternity4719/HowToLiveBetter)（CC BY 4.0），34 节 / 767 条 / A 级证据 492 条，每条标注成本、收益、证据等级与原始文献。

## ✨ 功能

- **四级导航**：首页 → 全书目录（8 大处境分组）→ 章节 → 条目详情
- **全文检索**：本地检索 767 条（标题/说人话/收益），离线可用
- **本机缓存**：收藏、阅读进度（首页「继续阅读」）、最近历史，localStorage 持久化
- **原生返回逻辑**：层级逐级回退；首页栈底双击（≤2s）退出 + Toast 提示，3 秒未按第二次自动重置
- **离线三通道**：内容内置 + Service Worker 缓存 + Capacitor 打包，无需网络权限
- **全机型适配**：safe-area（刘海屏/手势条）、iOS 16px 输入规范、100dvh、360~1440 视口无溢出

## 🏗 架构

```
Android / iOS / 桌面 —— Vue3 + vue-router(hash) + Vite
  ├─ 内容：book.json 打包内置（纯离线）
  ├─ 本机缓存：localStorage（收藏/进度/历史/设置）
  ├─ PWA：Service Worker（导航 network-first，发版即更新）
  └─ Capacitor 6 → APK / IPA
        │ 同源 /api/*（在线时可用，离线自动回退内置数据）
        ▼
Java 后端（零依赖，JDK 8-14，com.sun.net.httpserver）
  ├─ /api/meta /api/chapters /api/chapters/{n}
  ├─ /api/entries/{ch}/{n} /api/search?q= /api/health
  └─ 静态托管 frontend/dist（SPA 回退 + 安全响应头）
```

## 📂 项目结构

```
├── frontend/            Vue3 前端（Vite 构建）
│   ├── src/views/       5 个视图（首页/目录/章节/条目/检索）
│   ├── src/components/  6 个组件（账本主义设计语言）
│   ├── src/data/        book.json 内置内容数据
│   ├── android/         Capacitor Android 工程（gradle 构建 APK）
│   └── public/          PWA manifest + Service Worker + 图标
├── backend/             Java 后端（零依赖，3 个类）
├── docs/                测试报告 / 真机截图 / 项目档案
├── tools_md2json.py     数据管线：md 原稿 → book.json
└── e2e_test.py          Playwright E2E（23 项断言）
```

## 🚀 快速开始

**Web 模式（开发调试）**
```bash
cd frontend && npm install && npm run build
cd ../backend && javac -encoding UTF-8 -d out src/main/java/com/rszx/*.java
java -cp out com.rszx.Server 8080 data/book.json ../frontend/dist
# 浏览器打开 http://localhost:8080
```

**Android APK**
```bash
cd frontend
npm install @capacitor/core @capacitor/app @capacitor/toast @capacitor/android
npx cap sync android
cd android && ./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

**iOS**：`npx cap add ios` 后用 Xcode 构建（需 macOS）。

## 🧪 质量记录

| 项 | 结果 |
|---|---|
| 代码评审（独立 Agent） | P0×1 + P1×6 + P2×15，修复 19 项 |
| E2E 回归（Playwright） | 23/23 通过，0 控制台错误 |
| 真机（小米 15 / Android 16） | 渲染/缓存/收藏/PWA 全通过 |
| 返回退出逻辑（真机 APK） | 层级返回 3/3、双击退出、3 秒重置 4/4 |
| 机型适配 | 360/390/768/1440 四档视口零横向溢出 |

详见 [docs/真机测试报告.md](docs/真机测试报告.md)。

## 🛠 技术栈

Vue 3.4 · vue-router 4 · Vite 5 · Capacitor 6 · Java 8（纯 JDK，零第三方依赖）· Nashorn · Playwright（测试）

## 📄 许可

- **代码**：[MIT](LICENSE)
- **内容**（book.json 收录的正文）：[CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)，来自 [eternity4719/HowToLiveBetter](https://github.com/eternity4719/HowToLiveBetter)
