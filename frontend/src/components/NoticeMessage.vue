<script setup>
defineProps({
  message: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'success',
    validator: (value) => ['success', 'error'].includes(value)
  }
})
</script>

<template>
  <div class="notice" :class="type" :role="type === 'error' ? 'alert' : 'status'">
    {{ message }}
  </div>
</template>
