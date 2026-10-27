import { createContext, useContext } from 'react'

export const CityContext = createContext(null)

export function useCity() {
  return useContext(CityContext)
}
