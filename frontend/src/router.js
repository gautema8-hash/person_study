import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from './views/HomeView.vue'
import TocView from './views/TocView.vue'
import ChapterView from './views/ChapterView.vue'
import EntryView from './views/EntryView.vue'
import SearchView from './views/SearchView.vue'

// hash 路由：file:// 与 Capacitor WebView 下零配置可跑，离线最稳
export const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/toc', name: 'toc', component: TocView },
    { path: '/chapter/:num(\\d+)', name: 'chapter', component: ChapterView },
    { path: '/entry/:num(\\d+)/:n(\\d+)', name: 'entry', component: EntryView },
    { path: '/search', name: 'search', component: SearchView },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior() { return { top: 0 } },
})

// P2-14：路由级标题，利于历史记录与无障碍
router.afterEach((to) => {
  const titles = { home: '人生哲学', toc: '全书目录 · 人生哲学', chapter: '章节 · 人生哲学', entry: '条目 · 人生哲学', search: '检索 · 人生哲学' }
  document.title = titles[to.name] || '人生哲学'
})
