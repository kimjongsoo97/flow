<script setup>
defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['refresh'])
</script>

<template>
  <header class="page-header">
    <div>
      <p class="eyebrow">File Extension Blocker</p>
      <h1>파일 확장자 차단 관리</h1>
    </div>
    <button class="icon-button" type="button" :disabled="loading" title="새로고침" @click="$emit('refresh')">
      <span aria-hidden="true">↻</span>
      <span class="sr-only">새로고침</span>
    </button>
  </header>
</template>
