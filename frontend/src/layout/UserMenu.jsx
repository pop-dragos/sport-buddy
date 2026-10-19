import { useState } from 'react'
import { Link } from 'react-router-dom'

export default function UserMenu() {
  const [showPop, setShowPop] = useState(false)

  return (
    <div className="um">
      {showPop && (
        <div id="pop">
          <Link to="/profile/demo" onClick={() => setShowPop(false)}>👤 Profilul meu</Link>
          <Link to="/settings" onClick={() => setShowPop(false)}>⚙️ Setări</Link>
          <button onClick={() => { setShowPop(false); alert('Deconectare...'); }}>
            ↩ Ieși din cont
          </button>
        </div>
      )}

      <button className="si" onClick={() => setShowPop(!showPop)}>
        <span className="av">A</span>
        <span className="nm">Alex Popescu</span>
      </button>
    </div>
  )
}