<template>
  <main class="shell page">
    <template v-if="chapter">
      <Crumbs :items="[
        { label: '首页', to: '/' },
        { label: '目录', to: '/toc' },
        { label: `第 ${chapter.num} 节 ${chapter.title}` },
      ]" />
      <header class="ch-head">
        <span class="no">SECTION {{ String(chapter.num).padStart(2, '0') }} · {{ chapter.count }} 条</span>
        <h2>{{ chapter.title }}</h2>
        <p v-if="chapter.intro" class="intro">{{ chapter.intro }}</p>
        <p class="g-tag">{{ chapter.group }}</p>
      </header>
      <EntryRow v-for="e in chapter.entries" :key="e.n" :e="e" :ch="chapter.num" />
    </template>
    <template v-else>
      <Crumbs :items="[{ label: '首页', to: '/' }, { label: '目录', to: '/toc' }, { label: '章节' }]" />
      <EmptyState title="章节不存在" hint="返回目录挑一节看看" :to="{ name: 'toc' }" action="回目录" />
    </template>
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getChapter } from '../api'
import Crumbs from '../components/Crumbs.vue'
import EntryRow from '../components/EntryRow.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute()
const chapter = computed(() => getChapter(route.params.num))
</script>

<style scoped>
.ch-head { padding: var(--s4) 0 var(--s4); border-bottom: 1.5px solid var(--ink); margin-bottom: var(--s2); }
.no { font-size: 11px; letter-spacing: 4px; color: var(--cinnabar); }
h2 { font-family: var(--serif); font-size: clamp(24px, 5vw, 32px); letter-spacing: 4px; margin: var(--s1) 0; }
.intro { font-size: 13px; color: var(--text-2); max-width: 60ch; }
.g-tag { margin-top: var(--s2); font-size: 10px; letter-spacing: 2px; color: var(--mid); }
</style>
