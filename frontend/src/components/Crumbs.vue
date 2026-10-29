<template>
  <nav class="crumbs" aria-label="面包屑">
    <template v-for="(item, i) in items" :key="i">
      <router-link v-if="item.to" :to="item.to" class="crumb-btn">{{ item.label }}</router-link>
      <span v-else class="here">{{ item.label }}</span>
      <span v-if="i < items.length - 1" aria-hidden="true">/</span>
    </template>
  </nav>
</template>

<script setup>
defineProps({ items: { type: Array, required: true } })
</script>

<style scoped>
.crumb-btn { text-decoration: none; font-size: 12px; }
</style>
