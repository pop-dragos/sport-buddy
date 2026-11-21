import { useEffect, useState } from 'react'
import { getAuthConfig } from '../../api/auth'

let cached = null

// { googleClientId, mailConfigured }, cerut o singură dată. null cât timp se încarcă.
// Dacă serverul nu răspunde, Google rămâne simulat și nu arătăm nimic despre email.
export function useAuthConfig() {
  const [config, setConfig] = useState(cached)

  useEffect(() => {
    if (cached) return
    let cancelled = false
    getAuthConfig()
      .then((c) => { cached = c })
      .catch(() => ({ googleClientId: '', mailConfigured: true }))
      .then((c) => { if (!cancelled) setConfig(cached ?? c) })
    return () => { cancelled = true }
  }, [])

  return config
}
