import { useContext } from 'react'
import { NavigationContext } from './NavigationContext'

export default function useNavigation() {
  const navigation = useContext(NavigationContext)
  if (!navigation) throw new Error('useNavigation trebuie folosit în NavigationProvider.')
  return navigation
}
