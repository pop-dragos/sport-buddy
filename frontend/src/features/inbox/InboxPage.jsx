import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { getConversations } from '../../api/inbox'
import { errorMessage } from '../../api/http'
import Avatar from '../../shared/Avatar'
import { trField } from '../../shared/i18n'
import { useT } from '../../shared/i18n/useI18n'
import SportIcon from '../../shared/SportIcon'
import { formatDay, formatTime } from '../home/format'
import '../chat/chat.css'
import { INBOX_CHANGED } from './inboxEvents'
import './inbox.css'

const POLL_MS = 10000

// azi: doar ora; mai demult: ziua
function when(sentAt) {
  const today = new Date().toLocaleDateString('sv')
  return sentAt.slice(0, 10) === today ? formatTime(sentAt) : formatDay(sentAt)
}

function conversationPath(c) {
  return c.type === 'GROUP' ? `/messages/lobby/${c.id}` : `/messages/direct/${c.id}`
}

// Lista conversațiilor, cea mai recentă prima: cele cu prietenii și chat-urile lobby-urilor.
// Un clic deschide conversația în panoul din stânga; cea deschisă e evidențiată.
export default function InboxPage() {
  const t = useT()
  const { pathname } = useLocation()
  const [conversations, setConversations] = useState(null)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    getConversations()
      .then((list) => {
        setConversations(list)
        setError('')
      })
      .catch((e) => setError(errorMessage(e)))
  }, [])

  useEffect(() => {
    load()
    const timer = setInterval(load, POLL_MS)
    // conversația deschisă anunță când s-au citit mesajele, ca insigna din listă să dispară imediat
    window.addEventListener(INBOX_CHANGED, load)
    return () => {
      clearInterval(timer)
      window.removeEventListener(INBOX_CHANGED, load)
    }
  }, [load])

  return (
    <div className="inbox">
      {error && <div className="err">{error}</div>}
      {conversations === null && !error && <div className="mu">{t('Se încarcă...')}</div>}

      {conversations !== null && conversations.length === 0 && (
        <div className="card inbox-empty">
          <b>{t('Nu ai nicio conversație.')}</b>
          <div className="mu">{t('Trimite un mesaj unui prieten sau scrie în chat-ul unui lobby.')}</div>
          <Link to="/friends" className="b o">{t('Prieteni')}</Link>
        </div>
      )}

      {conversations !== null && conversations.length > 0 && (
        <div className="card inbox-list">
          {conversations.map((c) => {
            const group = c.type === 'GROUP'
            // la grup: cine a scris; la conversația directă contează doar dacă ultimul mesaj e al meu
            const prefix = c.lastFromMe ? `${t('Tu')}: ` : group && c.lastSender ? `${c.lastSender}: ` : ''
            return (
              <Link
                key={`${c.type}-${c.id}`}
                to={conversationPath(c)}
                className={`inbox-row${c.unread > 0 ? ' unread' : ''}${pathname === conversationPath(c) ? ' active' : ''}`}
              >
                {group
                  ? <span className="inbox-group-ic"><SportIcon type={c.sportCode} /></span>
                  : <Avatar name={c.title} url={c.avatarUrl} size={40} />}
                <span className="inbox-text">
                  <span className="inbox-title">
                    <b>{group ? `${t(c.title)} · ${trField(c.place)}` : c.title}</b>
                    {group && <span className="inbox-kind">{t('Grup')}</span>}
                  </span>
                  <span className="mu inbox-preview">{prefix}{c.lastMessage}</span>
                </span>
                <span className="inbox-meta">
                  <span className="mu">{when(c.sentAt)}</span>
                  {c.unread > 0 && (
                    <span className="unread-badge" aria-label={`${c.unread}`}>{c.unread}</span>
                  )}
                </span>
              </Link>
            )
          })}
        </div>
      )}
    </div>
  )
}
