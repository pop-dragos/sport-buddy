import { NavLink } from 'react-router-dom'
import UserMenu from './UserMenu'

const SPORTS = [
  { name: 'Fotbal', color: '#16a34a' },
  { name: 'Baschet', color: '#ea580c' },
  { name: 'Tenis', color: '#ca8a04' },
  { name: 'Padel', color: '#0891b2' },
  { name: 'Volei', color: '#db2777' },
  { name: 'Handbal', color: '#7c3aed' },
  { name: 'Badminton', color: '#0d9488' },
  { name: 'Tenis de masă', color: '#dc2626' },
  { name: 'Squash', color: '#2563eb' },
  { name: 'Alergare', color: '#65a30d' },
  { name: 'Ciclism', color: '#4f46e5' },
  { name: 'Înot', color: '#0284c7' }
]

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

function SportIcon({ type, color }) {
  return (
    <svg className="ic" style={{ color }} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      {type === 'Fotbal' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M12 8l3.5 2.5-1.3 4h-4.4l-1.3-4z" />
        </>
      )}
      {type === 'Baschet' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <line x1="3" y1="12" x2="21" y2="12" />
          <line x1="12" y1="3" x2="12" y2="21" />
          <path d="M5.5 5.5c4 3 4 10 0 13" />
          <path d="M18.5 5.5c-4 3-4 10 0 13" />
        </>
      )}
      {type === 'Tenis' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M5 5c4 3 4 11 0 14" />
          <path d="M19 5c-4 3-4 11 0 14" />
        </>
      )}
      {type === 'Padel' && (
        <>
          <circle cx="10" cy="10" r="6" />
          <line x1="14" y1="14" x2="20" y2="20" />
        </>
      )}
      {type === 'Volei' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M12 3c0 6-3 9-9 9" />
          <path d="M12 3c3 3 6 4 9 4" />
          <path d="M21 12c-6 0-9 3-9 9" />
        </>
      )}
      {type === 'Handbal' && (
        <>
          <circle cx="12" cy="12" r="9" />
          <circle cx="12" cy="12" r="3" />
        </>
      )}
      {type === 'Badminton' && (
        <>
          <path d="M8 4l-2 8h12l-2-8z" />
          <path d="M12 12v8" />
        </>
      )}
      {type === 'Tenis de masă' && (
        <>
          <circle cx="10" cy="10" r="6" />
          <line x1="14" y1="14" x2="20" y2="20" />
        </>
      )}
      {type === 'Squash' && (
        <>
          <circle cx="10" cy="10" r="6" />
          <line x1="14" y1="14" x2="20" y2="20" />
        </>
      )}
      {type === 'Alergare' && (
        <>
          <circle cx="13" cy="4" r="2" />
          <path d="M7 21l3-4 2 1 3 4" />
          <path d="M11 13l-2 4" />
          <path d="M12 8l-3 2 1 3h4l2-3" />
        </>
      )}
      {type === 'Ciclism' && (
        <>
          <circle cx="5.5" cy="17.5" r="3.5" />
          <circle cx="18.5" cy="17.5" r="3.5" />
          <path d="M12 17.5l-3-6 4-3 3.5 4H20" />
        </>
      )}
      {type === 'Înot' && (
        <>
          <path d="M2 18c2.5 0 2.5-1.5 5-1.5s2.5 1.5 5 1.5 2.5-1.5 5-1.5 2.5 1.5 5 1.5" />
          <path d="M2 14c2.5 0 2.5-1.5 5-1.5s2.5 1.5 5 1.5 2.5-1.5 5-1.5 2.5 1.5 5 1.5" />
        </>
      )}
    </svg>
  )
}

export default function Sidebar() {
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
        </NavLink>

        <NavLink to="/friends" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <IconFriends color="currentColor" />
          <span>Prieteni</span>
        </NavLink>

        <div className="mu" style={{ padding: '10px 10px 4px' }}>SPORTURI</div>

        {SPORTS.map((s) => (
          <button key={s.name} className="si" onClick={() => alert(`Ai selectat sportul ${s.name}`)}>
            <SportIcon type={s.name} color={s.color} />
            <span>{s.name}</span>
          </button>
        ))}
      </div>

      <UserMenu />
    </aside>
  )
}