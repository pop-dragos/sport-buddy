import { useEffect, useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'
import NavIcon from './NavIcon'
import useNavigation from './useNavigation'
import SportsSheet from './SportsSheet'
import UserMenu from './UserMenu'
import './mobile-nav.css'

export default function BottomNav() {
  const { pathname } = useLocation()
  const { user } = useAuth()
  const { sports, sportsError, todoTotal, openCalendar } = useNavigation()
  const [openPanel, setOpenPanel] = useState(null)
  // „Profil" e activ doar pe profilul meu și în setări, nu și pe profilul altcuiva
  const profileActive = pathname === `/profile/${user.id}` || pathname === '/settings'

  useEffect(() => {
    if (openPanel !== 'sports') return undefined
    const onKeyDown = (event) => {
      if (event.key === 'Escape') setOpenPanel(null)
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [openPanel])

  function selectSport(sport) {
    setOpenPanel(null)
    openCalendar(sport)
  }

  return (
    <>
      <nav className="bottom-nav" aria-label="Navigare principală">
        <NavLink to="/" end className={({ isActive }) => `bottom-nav-item${isActive ? ' active' : ''}`} aria-label="Acasă">
          <NavIcon name="home" className="bottom-nav-icon" />
          <span>Acasă</span>
        </NavLink>

        {/* aria-label înlocuiește tot textul din link, deci numărul „De făcut" trebuie pus și aici */}
        <NavLink
          to="/agenda"
          className={({ isActive }) => `bottom-nav-item${isActive ? ' active' : ''}`}
          aria-label={todoTotal > 0 ? `Agenda, ${todoTotal} de făcut` : 'Agenda'}
        >
          <NavIcon name="agenda" className="bottom-nav-icon" />
          <span>Agenda</span>
          {todoTotal > 0 && <span className="bottom-nav-badge" aria-hidden="true">{todoTotal}</span>}
        </NavLink>

        <button
          type="button"
          className={`bottom-nav-item${openPanel === 'sports' ? ' active' : ''}`}
          aria-label="Sporturi"
          aria-expanded={openPanel === 'sports'}
          onClick={() => setOpenPanel((current) => current === 'sports' ? null : 'sports')}
        >
          <NavIcon name="sports" className="bottom-nav-icon" />
          <span>Sporturi</span>
        </button>

        <NavLink to="/friends" className={({ isActive }) => `bottom-nav-item${isActive ? ' active' : ''}`} aria-label="Prieteni">
          <NavIcon name="friends" className="bottom-nav-icon" />
          <span>Prieteni</span>
        </NavLink>

        <UserMenu
          variant="bottom"
          open={openPanel === 'profile'}
          active={profileActive}
          onToggle={(open) => setOpenPanel(open ? 'profile' : null)}
        />
      </nav>

      {openPanel === 'sports' && (
        <SportsSheet
          sports={sports}
          sportsError={sportsError}
          onClose={() => setOpenPanel(null)}
          onSelect={selectSport}
        />
      )}
    </>
  )
}
