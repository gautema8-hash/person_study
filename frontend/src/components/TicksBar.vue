<template>
  <span class="ticks" :style="{ width: width + 'px' }" aria-hidden="true">
    <i v-for="k in shown" :key="k" :style="{ height: k % 2 === 1 ? '10px' : '6px' }" />
  </span>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  count: { type: Number, default: 0 },
  width: { type: Number, default: 46 },
  color: { type: String, default: 'var(--line)' },
})
const shown = computed(() => Math.min(props.count, 16))
</script>

<style scoped>
.ticks { display: inline-flex; align-items: flex-end; gap: 2px; flex-shrink: 0; }
.ticks i { width: 2px; background: v-bind(color); display: block; }
</style>
