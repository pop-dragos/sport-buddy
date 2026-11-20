import { useEffect, useState } from 'react'
import { getCities } from '../../api/catalog'
import { useAuth } from '../auth/useAuth'
import { CityContext } from './useCity'

const DEFAULT_CITY = 'Baia Mare'

export default function CityProvider({ children }) {
  const { user } = useAuth()
  const [cities, setCities] = useState([])
  // Orașul ales în header, ținut împreună cu cine l-a ales. CityProvider nu se demontează la logout,
  // așa că alegerea anterioară se folosește doar pentru utilizatorul care a făcut-o.
  const [choice, setChoice] = useState(null) // { userId, cityId }
  const selectedId = choice && choice.userId === user?.id ? choice.cityId : null

  function selectCity(cityId) {
    setChoice({ userId: user?.id, cityId })
  }

  // Orașele trebuie doar în aplicație (header, prima pagină), deci le cerem după conectare;
  // pe login și pe pagina de prezentare nu facem nicio cerere.
  // Dacă backend-ul nu răspunde încă (de exemplu abia pornește), mai încercăm la 3 secunde.
  const signedIn = user != null
  useEffect(() => {
    if (!signedIn) return
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
  }, [signedIn])

  // Orașul ales în header; până alege ceva: orașul din profil, apoi Baia Mare
  const city = cities.find((c) => c.id === selectedId)
    ?? cities.find((c) => c.id === user?.cityId)
    ?? cities.find((c) => c.name === DEFAULT_CITY)
    ?? null

  return (
    <CityContext.Provider value={{ cities, city, selectCity }}>
      {children}
    </CityContext.Provider>
  )
}
