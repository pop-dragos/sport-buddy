import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'
import Avatar from '../shared/Avatar'

// Iconițe liniare, ca în restul aplicației (brief: fără emoji; SVG 18 × 18, contur 1,8).
const ICONS = {
  profile: (
    <>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1" />
    </>
  ),
  settings: (
    <>
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
    </>
  ),
  logout: (
    <>
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
      <polyline points="16 17 21 12 16 7" />
      <line x1="21" y1="12" x2="9" y2="12" />
    </>
  )
}

function MenuIcon({ name }) {
  return (
    <svg className="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {ICONS[name]}
    </svg>
  )
}

const itemStyle = { display: 'flex', alignItems: 'center', gap: 10 }

export default function UserMenu() {
  const [showPop, setShowPop] = useState(false)
  const { user, logout } = useAuth()

  return (
    <div className="um">
      {showPop && (
        <div id="pop">
          <Link to={`/profile/${user.id}`} style={itemStyle} onClick={() => setShowPop(false)}>
            <MenuIcon name="profile" />Profilul meu
          </Link>
          <Link to="/settings" style={itemStyle} onClick={() => setShowPop(false)}>
            <MenuIcon name="settings" />Setări
          </Link>
          <button style={itemStyle} onClick={() => { setShowPop(false); logout() }}>
            <MenuIcon name="logout" />Ieși din cont
          </button>
        </div>
      )}

      <button className="si" onClick={() => setShowPop(!showPop)}>
        <Avatar name={user.fullName} url={user.avatarUrl} />
        <span className="nm">{user.username}</span>
      </button>
    </div>
  )
}
