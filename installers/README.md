# 安装包下载

## Android（直接安装）

**[人生哲学-v1.0.0-release.apk](Android/人生哲学-v1.0.0-release.apk)**（3.8MB）

- 已用正式密钥签名（CN=renshengzhexue，有效期至 2056 年），直接安装即可
- 已在小米 15（HyperOS / Android 16）实测：渲染、缓存、层级返回、双击退出全部通过
- 安装方式：下载后点击安装，或 `adb install 人生哲学-v1.0.0-release.apk`
- 首次安装如提示"未知来源"，允许一次即可

## iOS（需 Mac 生成 ipa）

**[人生哲学-iOS工程.zip](iOS/人生哲学-iOS工程.zip)**（1.1MB）

iOS 的 ipa 必须在 macOS + Xcode 环境构建（Windows 无法完成签名与编译），
已为你准备好完整工程，Mac 上三步出包：

```bash
unzip 人生哲学-iOS工程.zip && cd ios/App
pod install                        # 需要 CocoaPods（sudo gem install cocoapods）
open App.xcworkspace               # 注意打开 .xcworkspace 不是 .xcodeproj
# Xcode 中：签名选你的 Apple ID 团队 → 菜单 Product → Archive → Distribute App
```

- 免费个人 Apple ID：可在真机侧载（7 天有效期），Xcode 直接 Run 到 iPhone
- 上架 App Store：需要付费开发者账号（$99/年）走 Archive → 上传
- Web 版替代方案（免 Mac）：iPhone 与电脑同 WiFi，Safari 打开
  `http://<电脑IP>:8080` → 分享 → 添加到主屏幕，即可像 App 一样全屏使用

## 版本

- v1.0.0（2026-10-30）：34 节 767 条建议、四级导航、本机缓存、层级返回 + 双击退出
- 内容：HowToLiveBetter（CC BY 4.0）｜ 代码：MIT
