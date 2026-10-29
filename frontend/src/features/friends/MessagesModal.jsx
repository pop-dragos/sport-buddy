import { useEffect, useRef, useState } from 'react'
import { getMessages, sendMessage } from '../../api/friends'
import { errorMessage } from '../../api/http'
import Modal from '../../shared/Modal'
import { useAuth } from '../auth/useAuth'
import './friends.css'

const POLL_MS = 3000

// Fereastra de mesaje cu un prieten. Fără WebSocket: cât e deschisă, întreabă serverul la 3 secunde
// doar de mesajele cu id mai mare decât ultimul primit.
export default function MessagesModal({ friend, onClose }) {
  const { user } = useAuth()
  const [messages, setMessages] = useState(null)
  const [draft, setDraft] = useState('')
  const [error, setError] = useState('')
  const lastId = useRef(null)
  const box = useRef(null)
  const input = useRef(null)

  function append(list) {
    if (list.length === 0) return
    lastId.current = list[list.length - 1].id
    setMessages((current) => {
      const known = new Set((current ?? []).map((m) => m.id))
      return [...(current ?? []), ...list.filter((m) => !known.has(m.id))]
    })
  }

  useEffect(() => {
    let cancelled = false
    getMessages(friend.userId)
      .then((list) => {
        if (cancelled) return
        setMessages([])
        append(list)
      })
      .catch((e) => { if (!cancelled) setError(errorMessage(e)) })

    const timer = setInterval(() => {
      getMessages(friend.userId, lastId.current)
        .then((list) => { if (!cancelled) append(list) })
        .catch(() => {})
    }, POLL_MS)

    input.current?.focus()
    return () => {
      cancelled = true
      clearInterval(timer)
    }
  }, [friend.userId])

  // mereu la ultimul mesaj, ca într-un chat
  useEffect(() => {
    if (box.current) box.current.scrollTop = box.current.scrollHeight
  }, [messages])

  async function send(e) {
    e.preventDefault()
    if (!draft.trim()) return
    setError('')
    try {
      append([await sendMessage(friend.userId, draft.trim())])
      setDraft('')
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <Modal title={friend.username} onClose={onClose}>
      <div className="chat" ref={box}>
        {messages === null && !error && <div className="mu">Se încarcă...</div>}
        {messages?.length === 0 && <div className="mu">Nu ai scris încă nimic.</div>}
        {messages?.map((m) => (
          <div className="m" key={m.id}>
            <b>{m.senderId === user.id ? user.username : friend.username}:</b> {m.message}
          </div>
        ))}
      </div>
      <form className="r" onSubmit={send}>
        <input ref={input} placeholder="Mesaj" maxLength={1000} value={draft} onChange={(e) => setDraft(e.target.value)} />
        <button type="submit" className="b">Trimite</button>
      </form>
      {error && <div className="err" style={{ marginTop: 8 }}>{error}</div>}
      <div style={{ marginTop: 12 }}>
        <button type="button" className="b o" onClick={onClose}>Închide</button>
      </div>
    </Modal>
  )
}
