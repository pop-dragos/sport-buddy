import { useEffect, useState } from 'react'
import { getCities } from '../../api/catalog'
import { useAuth } from '../auth/useAuth'
import { CityContext } from './useCity'

const DEFAULT_CITY = 'Baia Mare'

export default function CityProvider({ children }) {
  const { user } = useAuth()
  const [cities, setCities] = useState([])
  const [selectedId, setSelectedId] = useState(null)

  // Dacă backend-ul nu răspunde încă (de exemplu abia pornește), mai încercăm la 3 secunde.
  useEffect(() => {
    let cancelled = false
    let timer
    function load() {
      getCities()
        .then((res) => { if (!cancelled) setCities(res) })
        .catch(() => { if (!cancelled) timer = setTimeout(load, 3000) })
    }
    load()
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [])

  // Orașul ales în header; până alege ceva: orașul din profil, apoi Baia Mare
  const city = cities.find((c) => c.id === selectedId)
    ?? cities.find((c) => c.id === user?.cityId)
    ?? cities.find((c) => c.name === DEFAULT_CITY)
    ?? null

  return (
    <CityContext.Provider value={{ cities, city, selectCity: setSelectedId }}>
      {children}
    </CityContext.Provider>
  )
}
