import { useEffect, useState } from 'react'
import { errorMessage } from '../../api/http'
import { getSettings, updateSettings } from '../../api/users'
import { setLang } from '../../shared/i18n'
import { setCurrency } from '../../shared/i18n/money'
import { applyTheme } from '../../shared/theme'

export function useSettings() {
  const [settings, setSettings] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    getSettings()
      .then((s) => { if (!cancelled) setSettings(s) })
      .catch((e) => { if (!cancelled) setError(errorMessage(e)) })
    return () => { cancelled = true }
  }, [])

  // Aplică imediat schimbarea și o trimite la server; dacă pică, revine la valoarea veche.
  async function save(patch) {
    const previous = settings
    setSettings({ ...settings, ...patch })
    setError('')
    try {
      const saved = await updateSettings({ ...settings, ...patch })
      setSettings(saved)
      if (patch.theme) applyTheme(saved.theme)
      // limba și moneda se aplică imediat în toată aplicația
      if (patch.language) setLang(saved.language)
      if (patch.currency) setCurrency(saved.currency)
    } catch (e) {
      setSettings(previous)
      setError(errorMessage(e))
    }
  }

  return { settings, error, save }
}
