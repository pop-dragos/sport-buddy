import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'

export default function UserMenu() {
  const [showPop, setShowPop] = useState(false)
  const { user, logout } = useAuth()

  return (
    <div className="um">
      {showPop && (
        <div id="pop">
          <Link to={`/profile/${user.id}`} onClick={() => setShowPop(false)}>👤 Profilul meu</Link>
          <Link to="/settings" onClick={() => setShowPop(false)}>⚙️ Setări</Link>
          <button onClick={() => { setShowPop(false); logout() }}>
            ↩ Ieși din cont
          </button>
        </div>
      )}

      <button className="si" onClick={() => setShowPop(!showPop)}>
        <span className="av">{user.fullName.charAt(0).toUpperCase()}</span>
        <span className="nm">{user.username}</span>
      </button>
    </div>
  )
}
