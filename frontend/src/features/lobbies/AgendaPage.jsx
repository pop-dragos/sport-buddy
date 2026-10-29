import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getMyLobbies } from '../../api/lobbies'
import { formatDay, formatTime } from '../home/format'
import './lobbies.css'

const TABS = [
  { key: 'UPCOMING', label: 'Viitoare' },
  { key: 'CREATED', label: 'Create de mine' },
  { key: 'REQUESTS', label: 'Cereri trimise' },
  { key: 'FINISHED', label: 'Încheiate' }
]

const STATUS = { OPEN: 'Deschis', LOCKED: 'Confirmat', FINISHED: 'Încheiat', CANCELLED: 'Anulat' }

export default function AgendaPage() {
  const navigate = useNavigate()
  const [tab, setTab] = useState('UPCOMING')
  // listele tuturor taburilor, încărcate o dată, ca să afișăm și numărul de pe fiecare tab
  const [lists, setLists] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    let cancelled = false
    Promise.all(TABS.map((t) => getMyLobbies(t.key)))
      .then((results) => {
        if (cancelled) return
        setLists(Object.fromEntries(TABS.map((t, i) => [t.key, results[i]])))
      })
      .catch(() => { if (!cancelled) setError(true) })
    return () => { cancelled = true }
  }, [])

  if (error) return <div className="lobby-page card">Agenda nu este disponibilă acum.</div>
  if (!lists) return <div className="lobby-page mu">Se încarcă...</div>

  const lobbies = lists[tab]

  return (
    <div className="lobby-page">
      <div className="r agenda-tabs">
        {TABS.map((t) => (
          <button key={t.key} className={`chip ${tab === t.key ? 'on' : ''}`} onClick={() => setTab(t.key)}>
            {t.label} ({lists[t.key].length})
          </button>
        ))}
      </div>

      {lobbies.length === 0 && <div className="card mu">Nu ai nimic în această listă.</div>}

      {lobbies.map((l) => (
        <div key={l.id} className="card">
          <div className="top">
            <div>
              <b>{l.sportName}</b>
              <div className="mu">{l.fieldName}, {l.cityName}</div>
              <div className="mu lobby-small">
                {formatDay(l.dateTime)}, {formatTime(l.dateTime)} · {l.currentPlayers} din {l.maxPlayers} jucători
              </div>
            </div>
            <span className="r">
              {l.isHost && <span className="tag">Organizator</span>}
              {l.pendingRequests > 0 && (
                <span className="tag">{l.pendingRequests} {l.pendingRequests === 1 ? 'cerere' : 'cereri'}</span>
              )}
              {l.status !== 'OPEN' && <span className="tag">{STATUS[l.status]}</span>}
              <button className="b o" onClick={() => navigate(`/lobbies/${l.id}`)}>Deschide</button>
            </span>
          </div>
        </div>
      ))}
    </div>
  )
}
