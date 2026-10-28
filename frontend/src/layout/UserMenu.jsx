import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'
import Avatar from '../shared/Avatar'

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
        <Avatar name={user.fullName} url={user.avatarUrl} />
        <span className="nm">{user.username}</span>
      </button>
    </div>
  )
}
