import { getToken, clearToken, SESSION_EXPIRED } from './session'

export class ApiError extends Error {
  constructor(status, body) {
    const firstField = body.fields && Object.values(body.fields)[0]
    super(firstField || body.message || `Eroare ${status}`)
    this.status = status
    this.code = body.code
    this.fields = body.fields
  }
}

export async function http(path, options = {}) {
  // endpoint-urile /auth nu primesc token: un token vechi nu trebuie să încurce conectarea
  const token = path.startsWith('/auth/') ? null : getToken()
  // la FormData (încărcare de fișiere) browserul pune singur Content-Type, cu separatorul corect
  const isForm = options.body instanceof FormData

  const res = await fetch('/api' + path, {
    ...options,
    headers: {
      ...(!isForm && { 'Content-Type': 'application/json' }),
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers
    },
  })

  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    if (res.status === 401 && token) {
      clearToken()
      window.dispatchEvent(new Event(SESSION_EXPIRED))
    }
    throw new ApiError(res.status, body)
  }

  return res.status === 204 ? null : res.json()
}

export function errorMessage(e) {
  return e instanceof ApiError ? e.message : 'Nu mă pot conecta la server.'
}
