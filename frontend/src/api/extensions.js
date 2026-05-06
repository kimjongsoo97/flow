const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')}/api/v1`

function apiUrl(path) {
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}

async function request(path, options = {}) {
  const response = await fetch(apiUrl(path), {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  })

  if (!response.ok) {
    let message = '요청을 처리하지 못했습니다.'
    try {
      const body = await response.json()
      message = body.message || message
    } catch {
      message = response.statusText || message
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

export function getExtensions() {
  return request('/extensions')
}

export function updateFixedExtension(id, checked) {
  return request(`/extensions/fixed/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ checked })
  })
}

export function createCustomExtension(extension) {
  return request('/extensions/custom', {
    method: 'POST',
    body: JSON.stringify({ extension })
  })
}

export function deleteCustomExtension(id) {
  return request(`/extensions/custom/${id}`, { method: 'DELETE' })
}
