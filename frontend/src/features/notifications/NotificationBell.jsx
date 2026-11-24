import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { errorMessage } from '../../api/http'
import { getNotifications, getUnreadCount, markAllRead, markRead } from '../../api/notifications'
import { useI18n } from '../../shared/i18n/useI18n'
import { formatDay, formatTime } from '../home/format'
import './notifications.css'

const POLL_MS = 30000

// Textul vine din tip și câmpuri, ca să respecte limba aleasă în aplicație.
function describe(n, t) {
  const name = n.actorName ?? ''
  switch (n.type) {
    case 'JOIN_REQUESTED': return t('{name} vrea să se alăture lobby-ului tău', { name })
    case 'JOIN_ACCEPTED': return t('Cererea ta de alăturare a fost acceptată')
    case 'JOIN_REJECTED': return t('Cererea ta de alăturare a fost refuzată')
    case 'LOBBY_LOCKED': return t('Lobby-ul a fost confirmat')
    case 'LOBBY_CANCELLED': return t('{name} a anulat lobby-ul', { name })
    case 'FRIEND_REQUEST': return t('{name} ți-a trimis o cerere de prietenie', { name })
    case 'FRIEND_ACCEPTED': return t('{name} a acceptat cererea ta de prietenie', { name })
    default: return t('Notificare nouă')
  }
}

// Clopoțelul din header: numărul de necitite se reîmprospătează la 30 de secunde (fără WebSocket),
// iar lista se încarcă la deschidere. Un clic pe o notificare o marchează citită și duce la lobby sau la prieteni.
export default function NotificationBell() {
  const { t, tn } = useI18n()
  const navigate = useNavigate()
  const [count, setCount] = useState(0)
  const [open, setOpen] = useState(false)
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const box = useRef(null)

  const refreshCount = useCallback(() => {
    getUnreadCount().then((res) => setCount(res.count)).catch(() => {})
  }, [])

  const loadList = useCallback(() => {
    getNotifications()
      .then((list) => {
        setItems(list)
        setError('')
      })
      .catch((e) => setError(errorMessage(e)))
  }, [])

  // la fiecare interogare reîmprospătăm și lista, cât timp panoul e deschis
  useEffect(() => {
    refreshCount()
    const timer = setInterval(() => {
      refreshCount()
      if (open) loadList()
    }, POLL_MS)
    return () => clearInterval(timer)
  }, [open, refreshCount, loadList])

  useEffect(() => {
    if (!open) return undefined
    const onPointerDown = (e) => {
      if (!box.current?.contains(e.target)) setOpen(false)
    }
    const onKeyDown = (e) => {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open])

  function toggle() {
    if (!open) {
      loadList()
      refreshCount()
    }
    setOpen(!open)
  }

  function openNotification(n) {
    if (!n.read) {
      markRead(n.id).catch(() => {})
      setItems((list) => list.map((x) => (x.id === n.id ? { ...x, read: true } : x)))
      setCount((c) => Math.max(0, c - 1))
    }
    setOpen(false)
    navigate(n.lobbyId ? `/lobbies/${n.lobbyId}` : '/friends')
  }

  async function readAll() {
    try {
      await markAllRead()
      setItems((list) => list.map((x) => ({ ...x, read: true })))
      setCount(0)
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <div className="notif" ref={box}>
      <button
        type="button"
        className="notif-btn"
        title={t('Notificări')}
        aria-label={count > 0
          ? `${t('Notificări')}, ${tn(count, '{n} necitită', '{n} necitite')}`
          : t('Notificări')}
        aria-haspopup="true"
        aria-expanded={open}
        onClick={toggle}
      >
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.8"
             strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        {count > 0 && <span className="notif-badge">{count > 99 ? '99+' : count}</span>}
      </button>

      {open && (
        <div className="notif-panel" role="dialog" aria-label={t('Notificări')}>
          <div className="notif-head">
            <b>{t('Notificări')}</b>
            {count > 0 && (
              <button type="button" className="notif-all" onClick={readAll}>{t('Marchează tot ca citit')}</button>
            )}
          </div>

          {error && <div className="err notif-empty">{error}</div>}
          {!error && items === null && <div className="mu notif-empty">{t('Se încarcă')}</div>}
          {!error && items !== null && items.length === 0 && (
            <div className="mu notif-empty">{t('Nicio notificare.')}</div>
          )}

          <div className="notif-list">
            {(items ?? []).map((n) => (
              <button
                key={n.id}
                type="button"
                className={`notif-item${n.read ? '' : ' unread'}`}
                onClick={() => openNotification(n)}
              >
                <span className="notif-text">{describe(n, t)}</span>
                <span className="mu notif-meta">
                  {n.subject ? `${t(n.subject)} · ` : ''}{formatDay(n.createdAt)}, {formatTime(n.createdAt)}
                </span>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
