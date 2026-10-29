<template>
  <header class="topbar">
    <router-link to="/" class="brand">
      人生哲学<small>REN SHENG ZHE XUE · 离线阅读</small>
    </router-link>
    <form class="searchbox" role="search" @submit.prevent="go">
      <input
        v-model="q"
        type="search"
        name="q" enterkeyhint="search" placeholder="检索 767 条建议：押金 / 担保 / 体检…"
        aria-label="检索建议"
      />
    </form>
  </header>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
// P2-7：与路由查询词双向同步（从搜索页返回后输入框状态一致）
const q = ref(String(route.query.q || ''))
watch(() => route.query.q, (v) => { q.value = String(v || '') })
function go() {
  if (q.value.trim()) router.push({ name: 'search', query: { q: q.value.trim() } })
}
</script>

<style scoped>
.topbar {
  display: flex; align-items: center; gap: var(--s4);
  padding: calc(var(--s3) + var(--safe-t)) var(--s4) var(--s3);
  border-bottom: 1.5px solid var(--ink);
  position: sticky; top: 0; z-index: 10; background: var(--paper);
}
.brand { font-family: var(--serif); font-size: 17px; letter-spacing: 2px; color: var(--ink); text-decoration: none; white-space: nowrap; }
.brand small { display: block; color: var(--mid); font-size: 9px; letter-spacing: 3px; font-family: var(--sans); }
.searchbox { flex: 1; display: flex; justify-content: flex-end; }
input {
  width: 100%; max-width: 320px; padding: 8px var(--s3);
  border: 1px solid var(--line); background: var(--paper-2);
  font-size: 13px; color: var(--ink); border-radius: var(--r);
}
input:focus { border-color: var(--cinnabar); outline: none; box-shadow: 0 0 0 3px rgba(181, 68, 44, 0.12); }
input::placeholder { color: var(--mid); }
@media (max-width: 480px) {
  input { font-size: 16px; } /* P1-4：iOS 聚焦 <16px 会触发页面自动缩放 */
}
@media (max-width: 480px) {
  .brand small { display: none; }
  .brand { font-size: 15px; }
}
</style>
