import { createApp } from 'vue'
import App from './App.vue'
import { router } from './router'
import { Capacitor } from '@capacitor/core'
import { probeBackend } from './api'
import { store } from './store'
import './styles/tokens.css'
import './styles/base.css'

const app = createApp(App)
app.use(router)
app.mount('#app')

// 探测在线状态（不阻塞渲染）
probeBackend().then((ok) => { store.online = ok })
window.addEventListener('online', () => { probeBackend().then((ok) => { store.online = ok }) }) // P2-6
window.addEventListener('offline', () => { store.online = false })

// PWA：仅 http/https 环境注册；file:// 与部分 WebView 内跳过
if ('serviceWorker' in navigator && /^https?:$/.test(location.protocol)) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js').catch(() => {})
  })
}

/* ═══ 原生 App：硬件返回键逻辑（仅 Capacitor 原生环境生效）═══
 * 1. 层级返回：条目→章节→目录/检索→首页，一级一级回退
 * 2. 首页（栈底）双击退出：两次返回键间隔 ≤2s 才真正退出
 * 3. 超时重置：第一次按下后停留超过 3s 自动取消确认并重置计时
 */
if (Capacitor.isNativePlatform()) {
  import('@capacitor/app').then(({ App: CapApp }) => {
    import('@capacitor/toast').then(({ Toast }) => {
      let lastBackAt = 0
      const EXIT_WINDOW = 2000   // 双击退出判定窗口：2 秒
      const RESET_DELAY = 3000   // 超过 3 秒未按第二次 → 重置

      // 层级父路由：条目→章节→目录→首页
      const parentOf = (route) => {
        if (route.name === 'entry') return { name: 'chapter', params: { num: route.params.num } }
        if (route.name === 'chapter') return { name: 'toc' }
        if (route.name === 'search' || route.name === 'toc') return { name: 'home' }
        return null // 首页 = 栈底
      }

      CapApp.addListener('backButton', async () => {
        const current = router.currentRoute.value
        const parent = parentOf(current)
        if (parent) {
          lastBackAt = 0
          router.push(parent)
          return
        }
        // 已在首页（栈底）：双击退出
        const now = Date.now()
        if (lastBackAt && now - lastBackAt <= EXIT_WINDOW) {
          await CapApp.exitApp()
          return
        }
        lastBackAt = now
        Toast.show({ text: '再按一次退出人生哲学', duration: 'short' }).catch(() => {})
        setTimeout(() => {
          if (Date.now() - lastBackAt >= EXIT_WINDOW) lastBackAt = 0 // 3s 后确认重置
        }, RESET_DELAY)
      })
    })
  })
}
