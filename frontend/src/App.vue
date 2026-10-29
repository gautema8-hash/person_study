<template>
  <div class="app-frame">
    <TopBar />
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
    <footer class="app-foot">
      <span>人生哲学 · {{ meta.version }}</span>
      <span :class="['net', store.online === true ? 'on' : '']">
        {{ store.online === true ? '在线 · 数据同步可用' : store.online === false ? '离线模式 · 内容已内置' : '···' }}
      </span>
      <span>AFTER eternity4719 · CC BY 4.0</span>
    </footer>
  </div>
</template>

<script setup>
import TopBar from './components/TopBar.vue'
import { store, meta } from './store'
</script>

<style scoped>
.app-frame { min-height: 100vh; min-height: 100dvh; display: flex; flex-direction: column; } /* P2-11：dvh 防移动端工具栏裁切 */
.app-frame > main { flex: 1; }
.app-foot {
  margin-top: auto;
  border-top: 1.5px solid var(--ink);
  padding: var(--s3) var(--s4) calc(var(--s3) + var(--safe-b));
  display: flex; justify-content: space-between; gap: var(--s3); flex-wrap: wrap;
  font-size: 9px; letter-spacing: 2px; color: var(--mid);
}
.net { color: var(--mid); }
.net.on { color: var(--cinnabar); }
</style>
