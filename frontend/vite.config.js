import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// base './'：相对路径，兼容 Capacitor（androidx/webview 与 WKWebView）与任意子路径离线部署
export default defineConfig({
  base: './',
  plugins: [vue()],
  build: { outDir: 'dist', assetsInlineLimit: 4096 },
})
