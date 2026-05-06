<script setup>
defineProps({
  checkedFixedCount: {
    type: Number,
    required: true
  },
  fixedCount: {
    type: Number,
    required: true
  },
  customCount: {
    type: Number,
    required: true
  }
})
</script>

<template>
  <div class="summary-grid">
    <div class="metric">
      <span>추천 확장자</span>
      <strong>{{ checkedFixedCount }} / {{ fixedCount }}</strong>
    </div>
    <div class="metric">
      <span>추가 확장자</span>
      <strong>{{ customCount }} / 200</strong>
    </div>
  </div>
</template>
