<template>
  <main class="shell page">
    <Crumbs :items="[{ label: '首页', to: '/' }, { label: '全书目录' }]" />
    <div class="head">
      <span class="no">CONTENTS · 34 节 {{ meta.entries }} 条</span>
      <h2>全书目录</h2>
    </div>
    <section v-for="g in groups" :key="g.name" class="group">
      <h3>{{ g.name }}</h3>
      <div class="list">
        <button
          v-for="c in g.chapters" :key="c.num"
          class="ch-row" @click="$router.push({ name: 'chapter', params: { num: c.num } })"
        >
          <span class="ch-num">{{ String(c.num).padStart(2, '0') }}</span>
          <span class="ch-title">{{ c.title }}</span>
          <TicksBar :count="c.count" :width="46" />
          <span class="ch-count">{{ c.count }} 条</span>
          <span class="arrow" aria-hidden="true">→</span>
        </button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { localBook, meta } from '../api'
import Crumbs from '../components/Crumbs.vue'
import TicksBar from '../components/TicksBar.vue'

const groups = computed(() => {
  const map = new Map()
  for (const c of localBook.chapters) {
    if (!map.has(c.group)) map.set(c.group, [])
    map.get(c.group).push(c)
  }
  return [...map.entries()].map(([name, chapters]) => ({ name, chapters }))
})
</script>

<style scoped>
.head { padding: var(--s4) 0 var(--s4); border-bottom: 1.5px solid var(--ink); }
.no { font-size: 11px; letter-spacing: 4px; color: var(--cinnabar); }
.head h2 { font-family: var(--serif); font-size: 28px; letter-spacing: 4px; margin-top: 4px; }
.group h3 { font-size: 12px; letter-spacing: 3px; color: var(--mid); margin: var(--s6) 0 var(--s2); font-weight: 600; }
.ch-row { display: flex; align-items: center; gap: var(--s3); width: 100%; padding: 10px var(--s3); border-bottom: 1px solid var(--faint); transition: background var(--tr); }
.ch-row:hover { background: var(--paper-2); }
.ch-num { font-family: var(--serif); font-size: 15px; color: var(--cinnabar); width: 32px; flex-shrink: 0; }
.ch-title { font-size: 14px; font-weight: 600; flex: 1; min-width: 0; }
.ch-count { font-size: 11px; color: var(--text-2); white-space: nowrap; }
.arrow { color: var(--mid); font-size: 12px; }
@media (max-width: 640px) { .ch-row .ticks { display: none; } }
</style>
