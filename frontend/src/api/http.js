export async function http(path, options = {}) {
  const res = await fetch('/api' + path, {
    ...options,
    headers: { 
      'Content-Type': 'application/json', 
      ...options.headers 
    },
  })

  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || `Eroare ${res.status}`)
  }

  return res.status === 204 ? null : res.json()
}