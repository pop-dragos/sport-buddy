import { useEffect, useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { getSports } from '../api/catalog'
import { getTodo } from '../api/lobbies'
import CalendarModal from '../features/lobbies/CalendarModal'
import SportIcon from '../shared/SportIcon'
import UserMenu from './UserMenu'

function IconHome({ color }) {
  return (
    <svg className="ic" style={{ color }} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <polyline points="9 22 9 12 15 12 15 22" />
    </svg>
  )
}

function IconAgenda({ color }) {
  return (
    <svg className="ic" style={{ color }} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
      <line x1="16" y1="2" x2="16" y2="6" />
      <line x1="8" y1="2" x2="8" y2="6" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </svg>
  )
}

function IconFriends({ color }) {
  return (
    <svg className="ic" style={{ color }} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  )
}

export default function Sidebar() {
  const [sports, setSports] = useState([])
  const [error, setError] = useState(false)
  // sportul pentru care e deschis calendarul; null = calendar închis
  const [calendarSport, setCalendarSport] = useState(null)
  // numărul „De făcut" de lângă Agenda mea; se recitește la fiecare schimbare de pagină
  const [todoTotal, setTodoTotal] = useState(0)
  const { pathname } = useLocation()

  useEffect(() => {
    getSports()
      .then(setSports)
      .catch(() => setError(true))
  }, [])

  useEffect(() => {
    let cancelled = false
    getTodo()
      .then((t) => { if (!cancelled) setTodoTotal(t.total) })
      .catch(() => {})
    return () => { cancelled = true }
  }, [pathname])

  return (
    <aside className="sidebar">
      <div className="logo">
        <b>S</b><span>portBuddy</span>
      </div>

      <div id="sbl">
        <NavLink to="/" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <IconHome color="currentColor" />
          <span>Acasă</span>
        </NavLink>

        <NavLink to="/agenda" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <IconAgenda color="currentColor" />
          <span>Agenda mea</span>
          {todoTotal > 0 && <span className="agenda-count" title="De făcut">{todoTotal}</span>}
        </NavLink>

        <NavLink to="/friends" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <IconFriends color="currentColor" />
          <span>Prieteni</span>
        </NavLink>

        <div className="mu sports-label" style={{ padding: '10px 10px 4px' }}>SPORTURI</div>

        {error && <div className="mu" style={{ padding: '4px 10px' }}>Sporturile nu sunt disponibile.</div>}

        {sports.map((s) => (
          <button key={s.id} className="si" onClick={() => setCalendarSport(s)}>
            <SportIcon type={s.code} />
            <span>{s.name}</span>
          </button>
        ))}
      </div>

      <UserMenu />

      {calendarSport && <CalendarModal sport={calendarSport} onClose={() => setCalendarSport(null)} />}
    </aside>
  )
}