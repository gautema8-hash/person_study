<template>
  <main class="shell page">
    <Crumbs :items="[{ label: '首页', to: '/' }, { label: '检索' }]" />
    <header class="s-head">
      <h2>检索「{{ q }}」</h2>
      <p class="s-sub">{{ hits.length ? `${hits.length} 条命中（本地全文检索 · 离线可用）` : '' }}</p>
    </header>
    <div v-if="hits.length" class="hits">
      <button v-for="h in hits" :key="h.ch + ':' + h.n" class="hit" @click="open(h)">
        <span class="loc">第 {{ h.ch }} 节 · {{ String(h.n).padStart(3, '0') }}</span>
        <span class="t">{{ h.title }}</span>
        <span class="p">{{ h.plain }}</span>
        <GradeBadge :grade="h.grade" />
      </button>
    </div>
    <EmptyState v-else :title="q ? '没有命中的建议' : '输入关键词开始检索'" :hint="q ? '换个关键词试试，如：押金、担保、体检、社保' : '如：押金、担保、体检、社保'" :to="{ name: 'toc' }" action="回目录" />
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchLocal } from '../api'
import Crumbs from '../components/Crumbs.vue'
import GradeBadge from '../components/GradeBadge.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const q = computed(() => String(route.query.q || ''))
const hits = computed(() => searchLocal(q.value))
function open(h) {
  router.push({ name: 'entry', params: { num: h.ch, n: h.n } })
}
</script>

<style scoped>
.s-head { padding: var(--s4) 0; }
h2 { font-family: var(--serif); font-size: 22px; letter-spacing: 2px; }
.s-sub { font-size: 12px; color: var(--mid); margin-top: 2px; }
.hit { display: flex; flex-direction: column; gap: 4px; width: 100%; padding: var(--s3) var(--s3); border-bottom: 1px solid var(--faint); transition: background var(--tr); align-items: flex-start; }
.hit:hover { background: var(--paper-2); }
.loc { font-size: 10px; letter-spacing: 2px; color: var(--cinnabar); }
.t { font-size: 14px; font-weight: 600; }
.p { font-size: 12px; color: var(--text-2); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>
