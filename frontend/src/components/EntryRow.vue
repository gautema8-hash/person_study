<template>
  <button class="entry-row" @click="$router.push({ name: 'entry', params: { num: ch, n: e.n } })">
    <span class="no">{{ String(e.n).padStart(3, '0') }}</span>
    <span class="t">{{ e.title }}</span>
    <span v-for="tag in (e.tags || []).slice(0, 3)" :key="tag.k" class="chip">{{ tag.k }}={{ tag.v }}</span>
    <GradeBadge :grade="e.grade" />
  </button>
</template>

<script setup>
import GradeBadge from './GradeBadge.vue'
defineProps({
  e: { type: Object, required: true },
  ch: { type: [Number, String], required: true },
})
</script>

<style scoped>
.entry-row { display: flex; align-items: center; gap: var(--s3); width: 100%; padding: 12px var(--s3); border-bottom: 1px solid var(--faint); transition: background var(--tr); }
.entry-row:hover { background: var(--paper-2); }
.no { font-family: var(--serif); color: var(--cinnabar); font-size: 14px; width: 36px; flex-shrink: 0; }
.t { font-size: 14px; font-weight: 600; flex: 1; min-width: 0; }
.chip { display: none; }
@media (min-width: 640px) { .chip { display: inline-block; } }
</style>
