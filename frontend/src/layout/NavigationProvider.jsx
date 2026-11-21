import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { getSports } from '../api/catalog'
import { getTodo } from '../api/lobbies'
import CalendarModal from '../features/lobbies/CalendarModal'
import { NavigationContext } from './NavigationContext'

export function NavigationProvider({ children }) {
  const { pathname } = useLocation()
  const [sports, setSports] = useState([])
  const [sportsError, setSportsError] = useState(false)
  const [todoTotal, setTodoTotal] = useState(0)
  const [calendarSport, setCalendarSport] = useState(null)

  useEffect(() => {
    getSports()
      .then(setSports)
      .catch(() => setSportsError(true))
  }, [])

  useEffect(() => {
    let cancelled = false
    getTodo()
      .then((todo) => { if (!cancelled) setTodoTotal(todo.total) })
      .catch(() => {})
    return () => { cancelled = true }
  }, [pathname])

  const value = {
    sports,
    sportsError,
    todoTotal,
    openCalendar: setCalendarSport
  }

  return (
    <NavigationContext.Provider value={value}>
      {children}
      {calendarSport && (
        <CalendarModal sport={calendarSport} onClose={() => setCalendarSport(null)} />
      )}
    </NavigationContext.Provider>
  )
}
