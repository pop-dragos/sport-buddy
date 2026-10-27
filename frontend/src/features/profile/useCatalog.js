import { useEffect, useState } from 'react'
import { getCities, getSports } from '../../api/catalog'

// Sporturile și orașele din catalog; până se încarcă, listele sunt goale.
export function useCatalog() {
  const [sports, setSports] = useState([])
  const [cities, setCities] = useState([])

  useEffect(() => {
    let cancelled = false
    getSports().then((s) => { if (!cancelled) setSports(s) }).catch(() => {})
    getCities().then((c) => { if (!cancelled) setCities(c) }).catch(() => {})
    return () => { cancelled = true }
  }, [])

  return { sports, cities }
}
