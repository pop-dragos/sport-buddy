import { useEffect, useRef, useState } from 'react'
import { getMessages, sendMessage } from '../../api/chat'
import { errorMessage } from '../../api/http'
import './chat.css'

const POLL_MS = 3000
const MAX_LENGTH = 500

// Adaugă mesajele noi la listă, fără dubluri (un mesaj trimis poate veni și din interogare).
function merge(current, incoming) {
    const known = new Set(current.map((m) => m.id))
    const fresh = incoming.filter((m) => !known.has(m.id))
    return fresh.length ? [...current, ...fresh] : current
}

// Chat-ul lobby-ului; canChat vine din pagina lobby-ului (organizator sau jucător acceptat).
// Pagina îl folosește cu key={lobbyId}, deci la alt lobby pornește de la zero.
export default function LobbyChat({ lobbyId, canChat }) {
    const [messages, setMessages] = useState([])
    const [text, setText] = useState('')
    const [error, setError] = useState('')
    const [forbidden, setForbidden] = useState(false)
    const [busy, setBusy] = useState(false)
    const boxRef = useRef(null)
    // id-ul ultimului mesaj primit, ca interogarea să ceară doar ce e nou
    const lastIdRef = useRef(null)

    useEffect(() => {
        if (!canChat) return
        let stopped = false
        let timer = null

        async function poll() {
            try {
                const incoming = await getMessages(lobbyId, lastIdRef.current)
                if (stopped) return
                if (incoming.length) {
                    lastIdRef.current = incoming[incoming.length - 1].id
                    setMessages((current) => merge(current, incoming))
                }
            } catch (e) {
                if (stopped) return
                if (e.status === 403) {
                    setForbidden(true)
                    return
                }
                // o eroare trecătoare nu oprește chat-ul; încercăm din nou la următoarea interogare
            }
            // următoarea cerere pleacă doar după ce s-a terminat cea de acum
            timer = setTimeout(poll, POLL_MS)
        }

        poll()
        return () => {
            stopped = true
            clearTimeout(timer)
        }
    }, [lobbyId, canChat])

    // la fiecare mesaj nou, caseta coboară la ultimul
    useEffect(() => {
        if (boxRef.current) boxRef.current.scrollTop = boxRef.current.scrollHeight
    }, [messages])

    async function send() {
        const message = text.trim()
        if (!message || busy) return
        setBusy(true)
        setError('')
        try {
            const saved = await sendMessage(lobbyId, message)
            setMessages((current) => merge(current, [saved]))
            if (!lastIdRef.current || saved.id > lastIdRef.current) lastIdRef.current = saved.id
            setText('')
        } catch (e) {
            setError(errorMessage(e))
        }
        setBusy(false)
    }

    function onKeyDown(e) {
        if (e.key === 'Enter') {
            e.preventDefault()
            send()
        }
    }

    if (!canChat || forbidden) {
        return (
            <div className="card">
                <b>Chat lobby</b>
                <div className="mu chat-note">Chatul e vizibil doar participanților acceptați.</div>
            </div>
        )
    }

    return (
        <div className="card">
            <b>Chat lobby</b>
            <div className="chat" ref={boxRef}>
                {messages.length === 0 && <div className="mu">Încă nu a scris nimeni. Salută-ți colegii.</div>}
                {messages.map((m) =>
                    m.type === 'SYSTEM' ? (
                        <div key={m.id} className="chat-m chat-system">{m.message}</div>
                    ) : (
                        <div key={m.id} className="chat-m" title={m.sentAt.slice(11, 16)}>
                            <b>{m.senderUsername}:</b> {m.message}
                        </div>
                    )
                )}
            </div>
            <div className="r">
                <input
                    value={text}
                    maxLength={MAX_LENGTH}
                    placeholder="Scrie un mesaj..."
                    onChange={(e) => setText(e.target.value)}
                    onKeyDown={onKeyDown}
                />
                <button className="b" disabled={busy || !text.trim()} onClick={send}>Trimite</button>
            </div>
            {error && <div className="err chat-note">{error}</div>}
        </div>
    )
}
