<template>
  <div class="empty" role="status">
    <p class="glyph" aria-hidden="true">⊔</p>
    <p>{{ title }}</p>
    <p v-if="hint" class="hint">{{ hint }}</p>
    <router-link v-if="to" :to="to" class="btn ghost act">{{ action }}</router-link>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '这里还是空的' },
  hint: { type: String, default: '' },
  to: { type: Object, default: null },
  action: { type: String, default: '去逛逛' },
})
</script>

<style scoped>
.glyph { font-family: var(--serif); font-size: 28px; color: var(--faint); margin-bottom: var(--s2); }
.hint { font-size: 11px; margin-top: var(--s1); }
.act { margin-top: var(--s4); text-decoration: none; }
</style>
