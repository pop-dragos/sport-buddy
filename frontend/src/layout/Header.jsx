import { useState } from 'react'
import { useLocation } from 'react-router-dom'
import CitySearch from '../features/catalog/CitySearch'
import CreateLobbyModal from '../features/lobbies/CreateLobbyModal'

export default function Header() {
  const location = useLocation()
  const [creating, setCreating] = useState(false)

  const getPageTitle = () => {
    switch (location.pathname) {
      case '/': return 'Acasă'
      case '/agenda': return 'Agenda mea'
      case '/friends': return 'Prieteni'
      case '/settings': return 'Setări'
      default:
        if (location.pathname.startsWith('/profile')) return 'Profil'
        if (location.pathname.startsWith('/lobbies')) return 'Lobby'
        return 'SportBuddy'
    }
  }

  return (
    <header>
      <h2>{getPageTitle()}</h2>
      <div className="r">
        <CitySearch />
        <button className="b plus" title="Creează lobby" onClick={() => setCreating(true)}>+</button>
      </div>

      {creating && <CreateLobbyModal onClose={() => setCreating(false)} />}
    </header>
  )
}