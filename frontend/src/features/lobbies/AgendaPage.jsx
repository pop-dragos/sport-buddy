import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getChatUnread } from '../../api/chat'
import { getMyLobbies } from '../../api/lobbies'
import Skeleton from '../../shared/Skeleton'
import { trField } from '../../shared/i18n'
import { useI18n } from '../../shared/i18n/useI18n'
import { formatDay, formatTime } from '../home/format'
import '../chat/chat.css'
import './lobbies.css'

const TAB_KEYS = ['UPCOMING', 'CREATED', 'REQUESTS', 'FINISHED']

export default function AgendaPage() {
  const navigate = useNavigate()
  const { t, tn } = useI18n()
  const [tab, setTab] = useState('UPCOMING')
  // listele tuturor taburilor, încărcate o dată, ca să afișăm și numărul de pe fiecare tab
  const [lists, setLists] = useState(null)
  const [error, setError] = useState(false)
  // { [lobbyId]: mesaje necitite în chat }; dacă nu se încarcă, agenda merge fără insigne
  const [unread, setUnread] = useState({})

  const tabLabels = {
    UPCOMING: t('Viitoare'),
    CREATED: t('Create de mine'),
    REQUESTS: t('Cereri trimise'),
    FINISHED: t('Încheiate')
  }
  const statusLabels = { OPEN: t('Deschis'), LOCKED: t('Confirmat'), FINISHED: t('Încheiat'), CANCELLED: t('Anulat') }

  useEffect(() => {
    let cancelled = false
    Promise.all(TAB_KEYS.map((key) => getMyLobbies(key)))
      .then((results) => {
        if (cancelled) return
        setLists(Object.fromEntries(TAB_KEYS.map((key, i) => [key, results[i]])))
      })
      .catch(() => { if (!cancelled) setError(true) })
    return () => { cancelled = true }
  }, [])

  useEffect(() => {
    let cancelled = false
    getChatUnread()
      .then((res) => { if (!cancelled) setUnread(res) })
      .catch(() => {})
    return () => { cancelled = true }
  }, [])

  if (error) return <div className="lobby-page card">{t('Agenda nu este disponibilă acum.')}</div>
  if (!lists) {
    return (
      <div className="lobby-page">
        <Skeleton lines={2} />
        <Skeleton lines={2} />
      </div>
    )
  }

  const lobbies = lists[tab]

  return (
    <div className="lobby-page">
      <div className="r segmented agenda-tabs">
        {TAB_KEYS.map((key) => (
          <button key={key} className={`chip ${tab === key ? 'on' : ''}`} onClick={() => setTab(key)}>
            {tabLabels[key]} ({lists[key].length})
          </button>
        ))}
      </div>

      {lobbies.length === 0 && <div className="card mu">{t('Nu ai nimic în această listă.')}</div>}

      {lobbies.map((l) => (
        <div key={l.id} className="card">
          <div className="top">
            <div>
              <b>{t(l.sportName)}</b>
              <div className="mu">{trField(l.fieldName)}, {l.cityName}</div>
              <div className="mu lobby-small">
                {formatDay(l.dateTime)}, {formatTime(l.dateTime)} · {t('{a} din {b} jucători', { a: l.currentPlayers, b: l.maxPlayers })}
              </div>
            </div>
            <span className="r">
              {l.isHost && <span className="tag">{t('Organizator')}</span>}
              {l.pendingRequests > 0 && (
                <span className="tag">{tn(l.pendingRequests, '{n} cerere', '{n} cereri')}</span>
              )}
              {l.status !== 'OPEN' && <span className="tag">{statusLabels[l.status]}</span>}
              <button className="b o" onClick={() => navigate(`/lobbies/${l.id}`)}>
                {t('Deschide')}
                {unread[l.id] > 0 && (
                  <span className="unread-badge" aria-label={tn(unread[l.id], '{n} mesaj nou', '{n} mesaje noi')}>
                    {unread[l.id]}
                  </span>
                )}
              </button>
            </span>
          </div>
        </div>
      ))}
    </div>
  )
}
