import { NavLink } from 'react-router-dom'
import SportIcon from '../shared/SportIcon'
import NavIcon from './NavIcon'
import useNavigation from './useNavigation'
import UserMenu from './UserMenu'

export default function Sidebar() {
  const { sports, sportsError, todoTotal, openCalendar } = useNavigation()

  return (
    <aside className="sidebar">
      <div className="logo">
        <b>S</b><span>portBuddy</span>
      </div>

      <div id="sbl">
        <NavLink to="/" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <NavIcon name="home" />
          <span>Acasă</span>
        </NavLink>

        <NavLink to="/agenda" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <NavIcon name="agenda" />
          <span>Agenda mea</span>
          {todoTotal > 0 && <span className="agenda-count" title="De făcut">{todoTotal}</span>}
        </NavLink>

        <NavLink to="/friends" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <NavIcon name="friends" />
          <span>Prieteni</span>
        </NavLink>

        <div className="mu sports-label" style={{ padding: '10px 10px 4px' }}>SPORTURI</div>

        {sportsError && <div className="mu" style={{ padding: '4px 10px' }}>Sporturile nu sunt disponibile.</div>}

        {sports.map((s) => (
          <button key={s.id} className="si" onClick={() => openCalendar(s)}>
            <SportIcon type={s.code} />
            <span>{s.name}</span>
          </button>
        ))}
      </div>

      <UserMenu />
    </aside>
  )
}