import { useState } from 'react'
import { useLocation } from 'react-router-dom'

const CITIES = [
  'Baia Mare', 'Cluj-Napoca', 'București', 'Satu Mare', 'Oradea', 'Timișoara', 
  'Iași', 'Brașov', 'Constanța', 'Sibiu', 'Craiova', 'Târgu Mureș'
]

export default function Header() {
  const [selectedCity, setSelectedCity] = useState('Baia Mare')
  const location = useLocation()

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
        <input 
          list="cl" 
          style={{ width: '190px' }} 
          value={selectedCity} 
          onChange={(e) => setSelectedCity(e.target.value)}
          placeholder="Caută oraș" 
        />
        <datalist id="cl">
          {CITIES.map((c) => (
            <option key={c} value={c} />
          ))}
        </datalist>
        <button className="b plus" title="Creează lobby" onClick={() => alert('Creează lobby')}>+</button>
      </div>
    </header>
  )
}