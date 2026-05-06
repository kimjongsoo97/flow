<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  createCustomExtension,
  deleteCustomExtension,
  getExtensions,
  updateFixedExtension
} from '../api/extensions'
import CustomExtensionPanel from '../components/CustomExtensionPanel.vue'
import FixedExtensionPanel from '../components/FixedExtensionPanel.vue'
import NoticeMessage from '../components/NoticeMessage.vue'
import PageHeader from '../components/PageHeader.vue'
import SummaryCards from '../components/SummaryCards.vue'

const fixedExtensions = ref([])
const customExtensions = ref([])
const loading = ref(true)
const savingId = ref(null)
const deletingId = ref(null)
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const checkedFixedCount = computed(() => fixedExtensions.value.filter((item) => item.checked).length)
const customCount = computed(() => customExtensions.value.length)

function showError(message) {
  errorMessage.value = message
  successMessage.value = ''
}

function showSuccess(message) {
  successMessage.value = message
  errorMessage.value = ''
}

async function loadExtensions() {
  loading.value = true
  errorMessage.value = ''

  try {
    const data = await getExtensions()
    fixedExtensions.value = data.fixed ?? []
    customExtensions.value = data.custom ?? []
  } catch (error) {
    showError(error.message)
  } finally {
    loading.value = false
  }
}

async function handleToggleFixed(extension) {
  const previous = extension.checked
  const nextChecked = !previous
  extension.checked = nextChecked
  savingId.value = extension.id
  errorMessage.value = ''

  try {
    const updated = await updateFixedExtension(extension.id, nextChecked)
    const index = fixedExtensions.value.findIndex((item) => item.id === updated.id)
    if (index >= 0) {
      fixedExtensions.value[index] = updated
    }

    const actionMessage = nextChecked ? '저장되었습니다.' : '삭제되었습니다.'
    showSuccess(`${updated.extension} 확장자가 ${actionMessage}`)
  } catch (error) {
    extension.checked = previous
    showError(error.message)
  } finally {
    savingId.value = null
  }
}

async function handleAddCustom(extensionValue) {
  submitting.value = true
  errorMessage.value = ''

  try {
    const created = await createCustomExtension(extensionValue)
    customExtensions.value = [created, ...customExtensions.value]
    showSuccess(`${created.extension} 확장자를 추가했습니다.`)
  } catch (error) {
    showError(error.message)
    throw error
  } finally {
    submitting.value = false
  }
}

async function handleDeleteCustom(extension) {
  deletingId.value = extension.id
  errorMessage.value = ''

  try {
    await deleteCustomExtension(extension.id)
    customExtensions.value = customExtensions.value.filter((item) => item.id !== extension.id)
    showSuccess(`${extension.extension} 확장자를 삭제했습니다.`)
  } catch (error) {
    showError(error.message)
  } finally {
    deletingId.value = null
  }
}

onMounted(loadExtensions)
</script>

<template>
  <main class="app-shell">
    <section class="workspace">
      <PageHeader :loading="loading" @refresh="loadExtensions" />

      <NoticeMessage v-if="errorMessage" type="error" :message="errorMessage" />
      <NoticeMessage v-else-if="successMessage" type="success" :message="successMessage" />

      <SummaryCards
        :checked-fixed-count="checkedFixedCount"
        :fixed-count="fixedExtensions.length"
        :custom-count="customCount"
      />

      <FixedExtensionPanel
        :extensions="fixedExtensions"
        :loading="loading"
        :saving-id="savingId"
        @toggle="handleToggleFixed"
      />

      <CustomExtensionPanel
        :extensions="customExtensions"
        :loading="loading"
        :deleting-id="deletingId"
        :submitting="submitting"
        :add-extension="handleAddCustom"
        @delete="handleDeleteCustom"
      />
    </section>
  </main>
</template>
