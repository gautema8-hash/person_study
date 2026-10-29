<template>
  <main class="shell page">
    <template v-if="entry && chapter">
      <Crumbs :items="[
        { label: '首页', to: '/' },
        { label: `第 ${chapter.num} 节`, to: { name: 'chapter', params: { num: chapter.num } } },
        { label: `第 ${entry.n} 条` },
      ]" />
      <header class="e-head">
        <span class="big-no">{{ String(entry.n).padStart(3, '0') }}</span>
        <div class="e-info">
          <h2>{{ entry.title }}</h2>
          <div class="e-meta">
            <span v-for="tag in entry.tags" :key="tag.k" class="chip">{{ tag.k }}={{ tag.v }}</span>
            <GradeBadge :grade="entry.grade" />
            <button
              class="fav" :class="{ on: faved }"
              :aria-pressed="faved" aria-label="收藏本条"
              @click="toggleFav(chapter.num, entry.n)"
            >{{ faved ? '★ 已收藏' : '☆ 收藏' }}</button>
          </div>
        </div>
      </header>

      <div class="ledger">
        <section class="say"><h4>说人话</h4><p>{{ entry.plain || '—' }}</p></section>
        <section class="fld"><h4>成本</h4><p>{{ entry.cost || '—' }}</p></section>
        <section class="fld"><h4>收益</h4><p>{{ entry.benefit || '—' }}</p></section>
        <section class="fld"><h4>来源</h4><p class="src">{{ entry.source || '—' }}</p></section>
        <section v-if="entry.note" class="fld"><h4>备注</h4><p>{{ entry.note }}</p></section>
      </div>

      <div class="pager">
        <button class="btn ghost" :disabled="entry.n <= 1" @click="go(entry.n - 1)">← 上一条</button>
        <span class="pos">{{ entry.n }} / {{ chapter.count }}</span>
        <button class="btn ghost" :disabled="entry.n >= chapter.count" @click="go(entry.n + 1)">下一条 →</button>
      </div>
    </template>
    <template v-else>
      <Crumbs :items="[{ label: '首页', to: '/' }, { label: '条目' }]" />
      <EmptyState title="条目不存在" hint="可能链接有误" :to="{ name: 'toc' }" action="回目录" />
    </template>
  </main>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getChapter, getEntry } from '../api'
import { store, toggleFav, isFav, recordRead } from '../store'
import Crumbs from '../components/Crumbs.vue'
import GradeBadge from '../components/GradeBadge.vue'
import EmptyState from '../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

const chapter = computed(() => getChapter(route.params.num))
const entry = computed(() => getEntry(route.params.num, route.params.n))
const faved = computed(() => (chapter.value && entry.value) ? isFav(chapter.value.num, entry.value.n) : false)

// 本机缓存：记录阅读进度与历史（纯离线持久化）
watch(entry, (e) => {
  if (e && chapter.value) recordRead(chapter.value.num, e.n, e.title)
}, { immediate: true })

function go(n) {
  router.push({ name: 'entry', params: { num: chapter.value.num, n } })
}
</script>

<style scoped>
.e-head { display: flex; gap: var(--s4); align-items: flex-start; padding: var(--s4) 0; }
.big-no { font-family: var(--serif); font-size: 44px; color: var(--cinnabar); line-height: 1; width: auto; min-width: 88px; flex-shrink: 0; }
.e-info { min-width: 0; }
.e-head h2 { font-family: var(--serif); font-size: clamp(20px, 4.5vw, 26px); letter-spacing: 2px; line-height: 1.4; }
.e-meta { display: flex; gap: var(--s2); flex-wrap: wrap; margin-top: var(--s2); align-items: center; }
.fav { font-size: 12px; padding: 2px 10px; border: 1px solid var(--line); border-radius: 2px; color: var(--text-2); transition: all var(--tr); }
.fav.on { border-color: var(--cinnabar); color: var(--cinnabar); }
.ledger { display: flex; flex-direction: column; gap: var(--s3); }
.say, .fld { border: 1px solid var(--faint); border-radius: var(--r); padding: var(--s4); background: var(--paper-2); }
.say { background: var(--ink); color: var(--paper); }
.say h4 { color: #D98A75; }
.say h4, .fld h4 { font-size: 11px; letter-spacing: 3px; margin-bottom: var(--s2); color: var(--mid); font-weight: 600; }
.say p, .fld p { font-size: 15px; }
.src { font-size: 12px; color: var(--text-2); word-break: break-all; }
.pager { display: flex; justify-content: space-between; align-items: center; margin-top: var(--s6); gap: var(--s3); }
.pos { font-size: 11px; color: var(--mid); letter-spacing: 2px; }
</style>
