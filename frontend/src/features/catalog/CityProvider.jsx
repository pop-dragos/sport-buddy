import { useEffect, useState } from 'react'
import { getCities } from '../../api/catalog'
import { useAuth } from '../auth/useAuth'
import { CityContext } from './useCity'

const DEFAULT_CITY = 'Baia Mare'

export default function CityProvider({ children }) {
  const { user } = useAuth()
  const [cities, setCities] = useState([])
  const [selectedId, setSelectedId] = useState(null)

  useEffect(() => {
    getCities()
      .then(setCities)
      .catch(() => setCities([]))
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
