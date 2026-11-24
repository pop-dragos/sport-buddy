import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getMessages as getLobbyMessages, sendMessage as sendLobbyMessage } from '../../api/chat'
import { getMessages as getDirectMessages, sendMessage as sendDirectMessage } from '../../api/friends'
import { errorMessage } from '../../api/http'
import { getLobby } from '../../api/lobbies'
import { getProfile } from '../../api/users'
import Avatar from '../../shared/Avatar'
import { trField } from '../../shared/i18n'
import { useT } from '../../shared/i18n/useI18n'
import SportIcon from '../../shared/SportIcon'
import { useAuth } from '../auth/useAuth'
import { formatDay, formatTime } from '../home/format'
import '../chat/chat.css'
import { INBOX_CHANGED } from './inboxEvents'
import './inbox.css'

const POLL_MS = 3000

// Aduce mesajele din cele două chat-uri la aceeași formă: { id, mine, system, author, text, sentAt }.
// La chat-ul de grup se vede cine a scris; la cel direct autorul e clar din context.
function normalize(m, kind, myId) {
  if (kind === 'lobby') {
    return {
      id: m.id,
      system: m.type === 'SYSTEM',
      mine: m.senderId === myId,
      author: m.senderUsername,
      text: m.message,
      sentAt: m.sentAt
    }
  }
  return { id: m.id, system: false, mine: m.senderId === myId, author: null, text: m.message, sentAt: m.sentAt }
}

function Conversation({ kind, id }) {
  const t = useT()
  const { user } = useAuth()
  const group = kind === 'lobby'
  const [info, setInfo] = useState(null)
  const [messages, setMessages] = useState(null)
  const [draft, setDraft] = useState('')
  const [error, setError] = useState('')
  const [blocked, setBlocked] = useState(false)
  const [busy, setBusy] = useState(false)
  const lastId = useRef(null)
  const feed = useRef(null)

  function append(list) {
    if (list.length === 0) return
    const mapped = list.map((m) => normalize(m, kind, user.id))
    lastId.current = mapped[mapped.length - 1].id
    setMessages((current) => {
      const known = new Set((current ?? []).map((m) => m.id))
      return [...(current ?? []), ...mapped.filter((m) => !known.has(m.id))]
    })
  }

  // cine e și, la grup, ce lobby e
  useEffect(() => {
    let cancelled = false
    const load = group
      ? getLobby(id).then((l) => ({
          title: `${t(l.sport.name)} · ${trField(l.field.name)}`,
          subtitle: `${formatDay(l.dateTime)}, ${formatTime(l.dateTime)}`,
          sportCode: l.sport.code,
          link: `/lobbies/${l.id}`
        }))
      : getProfile(id).then((p) => ({
          title: p.username,
          subtitle: p.fullName,
          avatarUrl: p.avatarUrl,
          link: `/profile/${p.id}`
        }))
    load.then((i) => { if (!cancelled) setInfo(i) }).catch((e) => { if (!cancelled) setError(errorMessage(e)) })
    return () => { cancelled = true }
  }, [group, id, t])

  // mesajele: la deschidere ultimele, apoi la 3 secunde doar cele noi. Cât e deschisă pagina, se citesc.
  useEffect(() => {
    const getMessages = group ? getLobbyMessages : getDirectMessages
    let cancelled = false
    let timer = null
    let announced = false

    async function poll() {
      try {
        const list = await getMessages(id, lastId.current)
        if (cancelled) return
        setMessages((current) => current ?? [])
        append(list)
        // la prima încărcare mesajele s-au citit: lista și insigna din header se actualizează
        if (!announced) {
          announced = true
          window.dispatchEvent(new Event(INBOX_CHANGED))
        }
      } catch (e) {
        if (cancelled) return
        if (e.status === 403) {
          setBlocked(true)
          setError(errorMessage(e))
          return
        }
      }
      timer = setTimeout(poll, POLL_MS)
    }

    poll()
    return () => {
      cancelled = true
      clearTimeout(timer)
      // la ieșire, insigna din header se actualizează imediat
      window.dispatchEvent(new Event(INBOX_CHANGED))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [group, id])

  // mereu la ultimul mesaj, ca într-un chat
  useEffect(() => {
    if (feed.current) feed.current.scrollTop = feed.current.scrollHeight
  }, [messages])

  async function send(e) {
    e.preventDefault()
    const text = draft.trim()
    if (!text || busy) return
    setBusy(true)
    setError('')
    try {
      append([await (group ? sendLobbyMessage(id, text) : sendDirectMessage(id, text))])
      setDraft('')
    } catch (err) {
      setError(errorMessage(err))
    }
    setBusy(false)
  }

  return (
    <div className="convo">
      <div className="convo-head">
        <Link to="/messages" className="convo-back" aria-label={t('Înapoi la mesaje')} title={t('Înapoi la mesaje')}>
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.8"
               strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </Link>
        {info && (group
          ? <span className="inbox-group-ic"><SportIcon type={info.sportCode} /></span>
          : <Avatar name={info.title} url={info.avatarUrl} size={36} />)}
        <div className="convo-title">
          {info ? (
            <>
              <Link to={info.link}><b>{info.title}</b></Link>
              <span className="mu">{group ? `${t('Grup')} · ${info.subtitle}` : info.subtitle}</span>
            </>
          ) : <span className="mu">{t('Se încarcă...')}</span>}
        </div>
      </div>

      <div className="convo-feed" ref={feed}>
        {messages === null && !error && <div className="mu">{t('Se încarcă...')}</div>}
        {messages?.length === 0 && (
          <div className="mu">{group ? t('Încă nu a scris nimeni. Salută-ți colegii.') : t('Nu ai scris încă nimic.')}</div>
        )}
        {messages?.map((m) => m.system ? (
          <div key={m.id} className="convo-system">{m.text}</div>
        ) : (
          <div key={m.id} className={`convo-msg${m.mine ? ' mine' : ''}`}>
            {group && !m.mine && <span className="convo-author">{m.author}</span>}
            <div className="convo-bubble" title={m.sentAt.slice(11, 16)}>{m.text}</div>
            <span className="mu convo-time">{formatTime(m.sentAt)}</span>
          </div>
        ))}
      </div>

      {error && <div className="err convo-error">{error}</div>}
      {!blocked && (
        <form className="r convo-form" onSubmit={send}>
          <input
            value={draft}
            maxLength={group ? 500 : 1000}
            placeholder={t('Scrie un mesaj...')}
            onChange={(e) => setDraft(e.target.value)}
            autoFocus={!window.matchMedia('(max-width: 640px)').matches}
          />
          <button type="submit" className="b" disabled={busy || !draft.trim()}>{t('Trimite')}</button>
        </form>
      )}
    </div>
  )
}

// Conversația pe toată pagina, cu bara laterală și bara de sus ale aplicației. kind: direct sau lobby.
// key-ul resetează totul când se schimbă conversația.
export default function ConversationPage({ kind }) {
  const { id } = useParams()
  return <Conversation key={`${kind}-${id}`} kind={kind} id={Number(id)} />
}
