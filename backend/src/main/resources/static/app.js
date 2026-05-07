const state = {
  fixedExtensions: [],
  customExtensions: [],
  acceptedFiles: [],
  savingId: null,
  deletingId: null,
  deletingFileId: null,
  submitting: false,
  uploading: false
}

function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;')
}

function normalizeExtension(value) {
  return value.trim().replace(/^\.+/, '').toLowerCase()
}

function formatFileSize(size) {
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function parseError(jqXHR) {
  let message = '요청을 처리하지 못했습니다.'
  try {
    const body = jqXHR.responseJSON || JSON.parse(jqXHR.responseText)
    message = body.message || message
  } catch {
    message = jqXHR.statusText || message
  }
  return message
}

function request(options) {
  return $.ajax(options).catch((jqXHR) => {
    throw new Error(parseError(jqXHR))
  })
}

function renderNotice(target, notice, extraClass = '') {
  if (!notice) {
    $(target).empty()
    return
  }

  const role = notice.type === 'error' ? 'alert' : 'status'
  $(target).html(`
    <div class="section-notice ${extraClass} ${notice.type}" role="${role}">
      ${escapeHtml(notice.message)}
    </div>
  `)
}

function renderSummary() {
  const checkedFixedCount = state.fixedExtensions.filter((item) => item.checked).length
  $('#fixed-summary').text(`${checkedFixedCount} / ${state.fixedExtensions.length}`)
  $('#custom-summary').text(`${state.customExtensions.length} / 200`)
  $('#custom-count-badge').text(`${state.customExtensions.length} / 200`)
}

function renderFixedExtensions() {
  if (state.fixedExtensions.length === 0) {
    $('#fixed-content').attr('class', 'empty-state').text('등록된 고정 확장자가 없습니다.')
    return
  }

  $('#fixed-content').attr('class', 'fixed-grid').html(
    state.fixedExtensions.map((extension) => `
      <label class="check-tile">
        <input
          type="checkbox"
          class="fixed-checkbox"
          data-id="${extension.id}"
          ${extension.checked ? 'checked' : ''}
          ${state.savingId === extension.id ? 'disabled' : ''}
        />
        <span class="check-mark"></span>
        <span class="extension-name">${escapeHtml(extension.extension)}</span>
      </label>
    `).join('')
  )
}

function renderCustomExtensions() {
  if (state.customExtensions.length === 0) {
    $('#custom-content').attr('class', 'empty-state').text('추가한 확장자가 없습니다.')
    return
  }

  $('#custom-content').attr('class', '').html(`
    <ul class="chip-list" aria-label="커스텀 확장자 목록">
      ${state.customExtensions.map((extension) => `
        <li class="chip">
          <span>${escapeHtml(extension.extension)}</span>
          <button
            type="button"
            class="delete-custom-button"
            data-id="${extension.id}"
            title="${escapeHtml(extension.extension)} 삭제"
            ${state.deletingId === extension.id ? 'disabled' : ''}
          >
            ×
          </button>
        </li>
      `).join('')}
    </ul>
  `)
}

function renderAcceptedFiles() {
  if (state.acceptedFiles.length === 0) {
    $('#accepted-file-content').attr('class', 'empty-state compact').text('아직 추가한 파일이 없습니다.')
    return
  }

  $('#accepted-file-content').attr('class', '').html(`
    <ul>
      ${state.acceptedFiles.map((file) => `
        <li>
          <span class="accepted-file-name">${escapeHtml(file.name)}</span>
          <span class="accepted-file-meta">${escapeHtml(file.extension)} · ${escapeHtml(file.size)}</span>
          <button
            type="button"
            class="delete-file-button"
            data-id="${file.id}"
            title="${escapeHtml(file.name)} 삭제"
            ${state.deletingFileId === file.id ? 'disabled' : ''}
          >
            ×
          </button>
        </li>
      `).join('')}
    </ul>
  `)
}

function render() {
  renderSummary()
  renderFixedExtensions()
  renderCustomExtensions()
  renderAcceptedFiles()
}

async function loadExtensions() {
  $('#global-notice').empty()
  $('#fixed-content').attr('class', 'loading-row').text('목록을 불러오는 중입니다.')
  $('#custom-content').attr('class', 'loading-row').text('목록을 불러오는 중입니다.')

  try {
    const data = await request({
      url: '/api/v1/extensions',
      method: 'GET',
      dataType: 'json'
    })
    state.fixedExtensions = data.fixed ?? []
    state.customExtensions = data.custom ?? []
    render()
  } catch (error) {
    $('#global-notice').html(`<div class="notice error" role="alert">${escapeHtml(error.message)}</div>`)
  }
}

async function toggleFixed(id) {
  const extension = state.fixedExtensions.find((item) => item.id === id)
  if (!extension) {
    return
  }

  const previous = extension.checked
  const nextChecked = !previous
  extension.checked = nextChecked
  state.savingId = id
  renderFixedExtensions()
  renderNotice('#fixed-notice', null)

  try {
    const updated = await request({
      url: `/api/v1/extensions/fixed/${id}`,
      method: 'PATCH',
      contentType: 'application/json',
      dataType: 'json',
      data: JSON.stringify({ checked: nextChecked })
    })
    state.fixedExtensions = state.fixedExtensions.map((item) => (item.id === updated.id ? updated : item))
    renderNotice('#fixed-notice', {
      type: 'success',
      message: `${updated.extension} 확장자가 ${nextChecked ? '차단되었습니다.' : '해제되었습니다.'}`
    }, 'below')
  } catch (error) {
    extension.checked = previous
    renderNotice('#fixed-notice', { type: 'error', message: error.message }, 'below')
  } finally {
    state.savingId = null
    render()
  }
}

async function addCustomExtension(value) {
  const normalized = normalizeExtension(value)
  if (!normalized || state.submitting) {
    return
  }

  state.submitting = true
  $('#custom-submit-button').prop('disabled', true)
  renderNotice('#custom-notice', null)

  try {
    const created = await request({
      url: '/api/v1/extensions/custom',
      method: 'POST',
      contentType: 'application/json',
      dataType: 'json',
      data: JSON.stringify({ extension: value })
    })
    state.customExtensions = [created, ...state.customExtensions]
    $('#custom-extension-input').val('')
    $('#custom-extension-preview').prop('hidden', true).text('')
    renderNotice('#custom-notice', { type: 'success', message: `${created.extension} 확장자를 추가했습니다.` }, 'below-form')
  } catch (error) {
    renderNotice('#custom-notice', { type: 'error', message: error.message }, 'below-form')
  } finally {
    state.submitting = false
    render()
  }
}

async function removeCustomExtension(id) {
  const extension = state.customExtensions.find((item) => item.id === id)
  if (!extension) {
    return
  }

  state.deletingId = id
  renderCustomExtensions()
  renderNotice('#custom-notice', null)

  try {
    await request({
      url: `/api/v1/extensions/custom/${id}`,
      method: 'DELETE'
    })
    state.customExtensions = state.customExtensions.filter((item) => item.id !== id)
    renderNotice('#custom-notice', { type: 'success', message: `${extension.extension} 확장자를 삭제했습니다.` }, 'below-form')
  } catch (error) {
    renderNotice('#custom-notice', { type: 'error', message: error.message }, 'below-form')
  } finally {
    state.deletingId = null
    render()
  }
}

async function uploadFile(file) {
  if (!file || state.uploading) {
    return
  }

  const formData = new FormData()
  formData.append('file', file)
  state.uploading = true
  $('#upload-title').text('검사 중')
  $('#upload-file-input').prop('disabled', true)
  renderNotice('#upload-notice', null)

  try {
    const result = await request({
      url: '/api/v1/files/upload',
      method: 'POST',
      data: formData,
      processData: false,
      contentType: false,
      dataType: 'json'
    })

    if (result.allowed) {
      state.acceptedFiles = [
        ...state.acceptedFiles,
        {
          id: result.fileId,
          name: result.originalFilename,
          extension: result.extension,
          size: formatFileSize(file.size)
        }
      ]
      renderNotice('#upload-notice', { type: 'success', message: '업로드에 성공되었습니다.' }, 'below')
    } else {
      $('#blocked-modal').prop('hidden', false)
    }
  } catch (error) {
    renderNotice('#upload-notice', { type: 'error', message: error.message }, 'below')
  } finally {
    state.uploading = false
    $('#upload-title').text('파일 선택')
    $('#upload-file-input').prop('disabled', false).val('')
    renderAcceptedFiles()
  }
}

async function removeUploadedFile(id) {
  if (!id || state.deletingFileId) {
    return
  }

  state.deletingFileId = id
  renderAcceptedFiles()
  renderNotice('#upload-notice', null)

  try {
    await request({
      url: `/api/v1/files/${id}`,
      method: 'DELETE'
    })
    state.acceptedFiles = state.acceptedFiles.filter((file) => file.id !== id)
    renderNotice('#upload-notice', { type: 'success', message: '파일이 삭제되었습니다.' }, 'below')
  } catch (error) {
    renderNotice('#upload-notice', { type: 'error', message: error.message }, 'below')
  } finally {
    state.deletingFileId = null
    renderAcceptedFiles()
  }
}

$(function () {
  $(document).on('change', '.fixed-checkbox', function () {
    toggleFixed(Number($(this).data('id')))
  })
  $('#custom-extension-input').on('input', function () {
    const normalized = normalizeExtension($(this).val())
    $('#custom-extension-preview')
      .prop('hidden', !normalized)
      .text(normalized ? `저장값: ${normalized}` : '')
    $('#custom-submit-button').prop('disabled', !normalized || state.submitting)
  })
  $('#custom-extension-form').on('submit', function (event) {
    event.preventDefault()
    addCustomExtension($('#custom-extension-input').val())
  })
  $(document).on('click', '.delete-custom-button', function () {
    removeCustomExtension(Number($(this).data('id')))
  })
  $('#upload-file-input').on('change', function () {
    const [file] = this.files
    uploadFile(file)
  })
  $(document).on('click', '.delete-file-button', function () {
    removeUploadedFile(Number($(this).data('id')))
  })
  $('#blocked-modal-close').on('click', function () {
    $('#blocked-modal').prop('hidden', true)
  })
  $('#blocked-modal').on('click', function (event) {
    if (event.target === this) {
      $('#blocked-modal').prop('hidden', true)
    }
  })

  loadExtensions()
})
