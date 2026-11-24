import { useCallback, useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { getUnreadMessages } from '../../api/inbox'
import { useI18n } from '../../shared/i18n/useI18n'
import { INBOX_CHANGED } from './inboxEvents'
import './inbox.css'

const POLL_MS = 15000

// Iconița de mesaje din header: duce la pagina cu toate conversațiile și arată câte mesaje necitite am.
// Numărul se reîmprospătează la 15 secunde (fără WebSocket), la schimbarea paginii și când pagina de
// mesaje anunță că s-a citit ceva.
export default function InboxButton() {
  const { t, tn } = useI18n()
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const [count, setCount] = useState(0)

  const refresh = useCallback(() => {
    getUnreadMessages().then((res) => setCount(res.count)).catch(() => {})
  }, [])

  useEffect(() => {
    refresh()
    const timer = setInterval(refresh, POLL_MS)
    window.addEventListener(INBOX_CHANGED, refresh)
    return () => {
      clearInterval(timer)
      window.removeEventListener(INBOX_CHANGED, refresh)
    }
  }, [refresh, pathname])

  return (
    <button
      type="button"
      className="inbox-btn"
      title={t('Mesaje')}
      aria-label={count > 0
        ? `${t('Mesaje')}, ${tn(count, '{n} necitit', '{n} necitite')}`
        : t('Mesaje')}
      onClick={() => navigate('/messages')}
    >
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.8"
           strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
      </svg>
      {count > 0 && <span className="inbox-badge">{count > 99 ? '99+' : count}</span>}
    </button>
  )
}
