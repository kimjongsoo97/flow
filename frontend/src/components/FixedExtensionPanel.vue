<script setup>
defineProps({
  extensions: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  savingId: {
    type: Number,
    default: null
  }
})

defineEmits(['toggle'])
</script>

<template>
  <section class="panel">
    <div class="panel-heading">
      <div>
        <h2>추천 확장자</h2>
        <p>자주 차단하는 확장자를 빠르게 선택할 수 있습니다.</p>
      </div>
    </div>

    <div class="guide-box">
      체크된 확장자는 업로드 차단 대상입니다. 체크를 해제하면 차단 목록에서 삭제된 상태로 저장됩니다.
    </div>

    <div v-if="loading" class="loading-row">목록을 불러오는 중입니다.</div>
    <div v-else class="fixed-grid">
      <label v-for="extension in extensions" :key="extension.id" class="check-tile">
        <input
          type="checkbox"
          :checked="extension.checked"
          :disabled="savingId === extension.id"
          @change="$emit('toggle', extension)"
        />
        <span class="check-mark"></span>
        <span class="extension-name">{{ extension.extension }}</span>
      </label>
    </div>
  </section>
</template>
