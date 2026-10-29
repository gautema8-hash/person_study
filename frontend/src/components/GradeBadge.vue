<template><span class="grade" :class="'grade-' + grade" :aria-label="'证据等级 ' + grade">{{ grade }}</span></template>
<script setup>
defineProps({ grade: { type: String, required: true } })
</script>
