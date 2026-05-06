<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  extensions: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  deletingId: {
    type: Number,
    default: null
  },
  submitting: {
    type: Boolean,
    default: false
  },
  addExtension: {
    type: Function,
    required: true
  }
})

defineEmits(['delete'])
const extensionInput = ref('')

const normalizedPreview = computed(() => normalizeExtension(extensionInput.value))
const canSubmit = computed(() => normalizedPreview.value.length > 0 && !props.submitting)

function normalizeExtension(value) {
  return value.trim().replace(/^\.+/, '').toLowerCase()
}

async function submitCustomExtension() {
  if (!canSubmit.value) {
    return
  }

  try {
    await props.addExtension(extensionInput.value)
    extensionInput.value = ''
  } catch {
    // Parent component already exposes the API error message.
  }
}
</script>

<template>
  <section class="panel">
    <div class="panel-heading">
      <div>
        <h2>추가 확장자</h2>
        <p>추가한 확장자는 항상 차단 대상으로 관리됩니다.</p>
      </div>
      <span class="count-badge">{{ extensions.length }} / 200</span>
    </div>

    <div class="guide-box">
      앞의 점과 공백은 자동으로 정리됩니다. 영문과 숫자만 입력할 수 있고, 최대 20자까지 저장됩니다.
    </div>

    <form class="add-form" @submit.prevent="submitCustomExtension">
      <div class="input-wrap">
        <input
          v-model="extensionInput"
          type="text"
          maxlength="40"
          placeholder="예: zip"
          aria-label="추가 확장자"
        />
        <span v-if="normalizedPreview" class="preview">저장값: {{ normalizedPreview }}</span>
      </div>
      <button class="primary-button" type="submit" :disabled="!canSubmit">추가</button>
    </form>

    <div v-if="loading" class="loading-row">목록을 불러오는 중입니다.</div>
    <div v-else-if="extensions.length === 0" class="empty-state">
      추가한 확장자가 없습니다.
    </div>
    <ul v-else class="chip-list" aria-label="추가 확장자 목록">
      <li v-for="extension in extensions" :key="extension.id" class="chip">
        <span>{{ extension.extension }}</span>
        <button
          type="button"
          :disabled="deletingId === extension.id"
          :title="`${extension.extension} 삭제`"
          @click="$emit('delete', extension)"
        >
          ×
        </button>
      </li>
    </ul>
  </section>
</template>
