import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  acceptRequest, cancelRequest, getFriends, getRequests, getSuggestions, rejectRequest, sendRequest
} from '../../api/friends'
import { errorMessage } from '../../api/http'
import { useCatalog } from '../profile/useCatalog'
import FriendRow from './FriendRow'
import MessagesModal from './MessagesModal'

// căutare fără diacritice și fără majuscule, ca pe server
function normalize(s) {
  return (s ?? '').normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim()
}

export default function FriendsPage() {
  const { sports } = useCatalog()
  const [friends, setFriends] = useState(null)
  const [requests, setRequests] = useState({ incoming: [], outgoing: [] })
  const [suggestions, setSuggestions] = useState([])
  const [query, setQuery] = useState('')
  const [chatWith, setChatWith] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const fetchAll = () => Promise.all([getFriends(), getRequests(), getSuggestions()])

  const show = useCallback(([f, r, s]) => {
    setFriends(f)
    setRequests(r)
    setSuggestions(s)
  }, [])

  useEffect(() => {
    let cancelled = false
    fetchAll()
      .then((data) => { if (!cancelled) show(data) })
      .catch((e) => { if (!cancelled) setError(errorMessage(e)) })
    return () => { cancelled = true }
  }, [show])

  // orice acțiune schimbă mai multe liste deodată (o cerere acceptată trece la prieteni), deci le reîncărcăm
  async function run(action) {
    setBusy(true)
    setError('')
    try {
      await action()
      show(await fetchAll())
    } catch (e) {
      setError(errorMessage(e))
    }
    setBusy(false)
  }

  const sportNames = useMemo(() => Object.fromEntries(sports.map((s) => [s.code, s.name])), [sports])

  const q = normalize(query)
  const shown = (friends ?? []).filter(
    (f) => !q || normalize(f.username).includes(q) || normalize(f.fullName).includes(q)
  )
  const hasRequests = requests.incoming.length > 0 || requests.outgoing.length > 0

  return (
    <div style={{ maxWidth: 960, margin: '0 auto' }}>
      <input
        placeholder="Caută printre prieteni"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        style={{ marginBottom: 12 }}
      />

      {error && <div className="card err">{error}</div>}

      {hasRequests && (
        <div className="card">
          <b>Cereri de prietenie</b>
          {requests.incoming.map((r) => (
            <FriendRow key={r.requestId} friend={r.user} sportNames={sportNames}>
              <button type="button" className="b" disabled={busy} onClick={() => run(() => acceptRequest(r.requestId))}>
                Acceptă
              </button>
              <button type="button" className="b o" disabled={busy} onClick={() => run(() => rejectRequest(r.requestId))}>
                Refuză
              </button>
            </FriendRow>
          ))}
          {requests.outgoing.map((r) => (
            <FriendRow key={r.requestId} friend={r.user} sportNames={sportNames}>
              <span className="tag">Cerere trimisă</span>
              <button type="button" className="b o" disabled={busy} onClick={() => run(() => cancelRequest(r.requestId))}>
                Anulează
              </button>
            </FriendRow>
          ))}
        </div>
      )}

      <div className="card">
        <b>Prieteni ({friends?.length ?? 0})</b>
        {friends === null && !error && <div className="mu" style={{ marginTop: 6 }}>Se încarcă...</div>}
        {friends !== null && shown.length === 0 && (
          <div className="mu" style={{ marginTop: 6 }}>
            {friends.length === 0 ? 'Nu ai încă prieteni.' : 'Niciun rezultat.'}
          </div>
        )}
        {shown.map((f) => (
          <FriendRow key={f.userId} friend={f} sportNames={sportNames}>
            <button type="button" className="b o" onClick={() => setChatWith(f)}>Mesaj</button>
          </FriendRow>
        ))}
      </div>

      <div className="card">
        <b>Posibili cunoscuți</b>
        <div className="mu">Jucători cu sporturi în comun cu tine</div>
        {suggestions.length === 0 && <div className="mu">Nimeni momentan.</div>}
        {suggestions.map((s) => (
          <FriendRow key={s.userId} friend={s} sportNames={sportNames}>
            <button type="button" className="b o" disabled={busy} onClick={() => run(() => sendRequest(s.userId))}>
              Adaugă
            </button>
          </FriendRow>
        ))}
      </div>

      {chatWith && <MessagesModal friend={chatWith} onClose={() => setChatWith(null)} />}
    </div>
  )
}
