import { useState } from 'react'
import { useLocation } from 'react-router-dom'
import CitySearch from '../features/catalog/CitySearch'
import CreateLobbyModal from '../features/lobbies/CreateLobbyModal'
import NotificationBell from '../features/notifications/NotificationBell'
import { useT } from '../shared/i18n/useI18n'

export default function Header() {
  const location = useLocation()
  const t = useT()
  const [creating, setCreating] = useState(false)

  const getPageTitle = () => {
    switch (location.pathname) {
      case '/': return t('Acasă')
      case '/agenda': return t('Agenda mea')
      case '/friends': return t('Prieteni')
      case '/settings': return t('Setări')
      default:
        if (location.pathname.startsWith('/profile')) return t('Profil')
        if (location.pathname.startsWith('/lobbies')) return t('Lobby')
        return 'SportBuddy'
    }
  }

  return (
    <header>
      <h2>{getPageTitle()}</h2>
      <div className="r">
        <CitySearch />
        <NotificationBell />
        <button className="b plus" title={t('Creează lobby')} onClick={() => setCreating(true)}>+</button>
      </div>

      {creating && <CreateLobbyModal onClose={() => setCreating(false)} />}
    </header>
  )
}