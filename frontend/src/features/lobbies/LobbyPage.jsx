import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { errorMessage } from '../../api/http'
import {
  acceptRequest, cancelLobby, getLobby, leaveLobby, lockLobby, rejectRequest, requestToJoin, saveAttendance,
  simulateFinish, simulatePassDeadline
} from '../../api/lobbies'
import AttendanceCard from './AttendanceCard'
import LobbyChat from '../chat/LobbyChat'
import Avatar from '../../shared/Avatar'
import { useAuth } from '../auth/useAuth'
import { formatDay, formatRating, formatTime } from '../home/format'
import './lobbies.css'

const STATUS = {
  OPEN: 'Deschis',
  LOCKED: 'Confirmat',
  FINISHED: 'Încheiat',
  CANCELLED: 'Anulat'
}

export default function LobbyPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [lobby, setLobby] = useState(null)
  const [error, setError] = useState(null)
  // eroarea ultimei acțiuni (cerere, acceptare...), afișată sub antet
  const [actionError, setActionError] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let cancelled = false
    getLobby(id)
      .then((res) => { if (!cancelled) setLobby(res) })
      .catch((err) => { if (!cancelled) setError(errorMessage(err)) })
    return () => { cancelled = true }
  }, [id])

  // Rulează o acțiune, apoi afișează lobby-ul actualizat.
  // Acceptarea și refuzul întorc direct lobby-ul; la celelalte îl cerem din nou.
  async function run(action) {
    setActionError(null)
    setBusy(true)
    try {
      const res = await action()
      setLobby(res?.id ? res : await getLobby(id))
    } catch (err) {
      setActionError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  function leave() {
    const late = lobby.status === 'LOCKED'
    if (late && !window.confirm('Lobby-ul e confirmat. Dacă ieși acum, primești o penalizare la rating. Continui?')) {
      return
    }
    run(() => leaveLobby(lobby.id))
  }

  function cancel() {
    const message = lobby.status === 'LOCKED'
      ? 'Lobby-ul e confirmat. Dacă îl anulezi acum, primești o penalizare la rating. Continui?'
      : 'Sigur anulezi lobby-ul?'
    if (window.confirm(message)) run(() => cancelLobby(lobby.id))
  }

  const back = <button className="lobby-back" onClick={() => navigate(-1)}>‹ Înapoi</button>

  if (error) {
    return (
      <div className="lobby-page">
        {back}
        <div className="card">{error}</div>
      </div>
    )
  }
  if (!lobby) return <div className="lobby-page mu">Se încarcă...</div>

  return (
    <div className="lobby-page">
      {back}

      <div className="card">
        <div className="top">
          <h3 className="lobby-title">{lobby.sport.name}</h3>
          <span className="tag">{STATUS[lobby.status]}</span>
        </div>
        <div className="mu">{lobby.field.name}, {lobby.city.name}</div>

        <div className="lobby-facts">
          <Fact label="Data" value={formatDay(lobby.dateTime)} />
          <Fact label="Ora" value={formatTime(lobby.dateTime)} />
          <Fact label="Organizator" value={lobby.host.username} />
          <Fact label="Locuri" value={`${lobby.currentPlayers} din ${lobby.maxPlayers}`} />
        </div>

        <div className="bar">
          <i style={{ width: `${(lobby.currentPlayers / lobby.maxPlayers) * 100}%` }} />
        </div>

        <div className="top">
          <span className="mu">
            {lobby.freeSlots === 0 ? 'Lobby complet' : `${lobby.freeSlots} locuri libere`}, nivel mediu {formatRating(lobby.averageRating)}
          </span>
          <LobbyAction
            lobby={lobby}
            busy={busy}
            onJoin={() => run(() => requestToJoin(lobby.id))}
            onLeave={leave}
            onCancel={cancel}
          />
        </div>
        {actionError && <div className="err lobby-note">{actionError}</div>}
      </div>

      {lobby.isHost && lobby.pendingRequests.length > 0 && (
        <div className="card">
          <b>Cereri de alăturare</b>
          <div className="lobby-list">
            {lobby.pendingRequests.map((r) => (
              <div key={r.requestId} className="pl">
                <span className="lobby-player">
                  <Avatar name={r.username} size={28} />
                  <span>
                    <Link className="lk" to={`/profile/${r.userId}`}>{r.username}</Link>
                    <div className="mu lobby-small">
                      rating {formatRating(r.rating)}
                      {r.reliabilityPercent != null && `, ${r.reliabilityPercent}% prezență`}
                    </div>
                  </span>
                </span>
                <span className="r">
                  <button className="b" disabled={busy} onClick={() => run(() => acceptRequest(lobby.id, r.requestId))}>
                    Acceptă
                  </button>
                  <button className="b o" disabled={busy} onClick={() => run(() => rejectRequest(lobby.id, r.requestId))}>
                    Refuză
                  </button>
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {lobby.isHost && lobby.status === 'FINISHED' && !lobby.attendanceSaved && (
        <AttendanceCard
          lobby={lobby}
          busy={busy}
          onSave={(attendance) => run(() => saveAttendance(lobby.id, attendance))}
        />
      )}

      <CostCard lobby={lobby} busy={busy} onLock={() => run(() => lockLobby(lobby.id))} />

      {/* import.meta.env.DEV e true doar cu `npm run dev`, deci panoul nu ajunge într-un build real */}
      {import.meta.env.DEV && lobby.isHost && (lobby.status === 'OPEN' || lobby.status === 'LOCKED') && (
        <div className="card lobby-sim">
          <b>Simulare</b>
          <div className="mu lobby-small">Pentru testare: grăbește ce s-ar întâmpla în timp real.</div>
          <div className="r lobby-note">
            {lobby.status === 'OPEN' && (
              <button className="b o" disabled={busy} onClick={() => run(() => simulatePassDeadline(lobby.id))}>
                Trece termenul de confirmare
              </button>
            )}
            {lobby.status === 'LOCKED' && (
              <button className="b o" disabled={busy} onClick={() => run(() => simulateFinish(lobby.id))}>
                Încheie meciul
              </button>
            )}
          </div>
        </div>
      )}

      <div className="card">
        <b>Participanți</b>
        <div className="lobby-list">
          {lobby.participants.map((p) => (
            <div key={p.userId} className="pl">
              <span className="lobby-player">
                <Avatar name={p.username} size={28} />
                <span>
                  <Link className="lk" to={`/profile/${p.userId}`}>{p.username}</Link>
                  {p.userId === user.id && <span className="mu"> (tu)</span>}
                  {p.isHost && <div className="mu lobby-small">organizator</div>}
                  {p.attended === false && <div className="mu lobby-small">absent</div>}
                </span>
              </span>
              <span className="lobby-rating">
                {formatRating(p.rating)}
                {p.reliabilityPercent != null && <span className="mu lobby-small">{p.reliabilityPercent}% prezență</span>}
              </span>
            </div>
          ))}
          {lobby.broughtPlayers > 0 && (
            <div className="pl mu">
              +{lobby.broughtPlayers} {lobby.broughtPlayers === 1 ? 'jucător adus' : 'jucători aduși'} de organizator
            </div>
          )}
        </div>
      </div>

      <LobbyChat key={lobby.id} lobbyId={lobby.id} canChat={lobby.isHost || lobby.myState === 'ACCEPTED'} />
    </div>
  )
}

function Fact({ label, value }) {
  return (
    <div>
      <div className="mu lobby-small">{label}</div>
      <div>{value}</div>
    </div>
  )
}

// Butonul din dreapta, după relația mea cu lobby-ul.
function LobbyAction({ lobby, busy, onJoin, onLeave, onCancel }) {
  const active = lobby.status === 'OPEN' || lobby.status === 'LOCKED'

  if (lobby.isHost) {
    return active
      ? <button className="b o" disabled={busy} onClick={onCancel}>Anulează lobby-ul</button>
      : <span className="mu">Ești organizatorul</span>
  }

  if (lobby.myState === 'ACCEPTED') {
    return active
      ? <button className="b o" disabled={busy} onClick={onLeave}>Ieși din lobby</button>
      : <span className="mu">Ai participat</span>
  }

  if (lobby.myState === 'PENDING') {
    return (
      <span className="r">
        <span className="mu">Cerere trimisă</span>
        <button className="b o" disabled={busy} onClick={onLeave}>Retrage</button>
      </span>
    )
  }

  if (lobby.joinClosed) {
    return <button className="b" disabled>{lobby.freeSlots === 0 ? 'Complet' : 'Înscrieri închise'}</button>
  }

  return (
    <span className="r">
      {lobby.myState === 'REJECTED' && <span className="mu">Cererea anterioară a fost refuzată</span>}
      <button className="b" disabled={busy} onClick={onJoin}>
        {lobby.myState === 'NONE' ? 'Cere să intri' : 'Cere din nou'}
      </button>
    </span>
  )
}

function CostCard({ lobby, busy, onLock }) {
  const free = lobby.fieldCost === 0
  const deadline = `${formatDay(lobby.confirmDeadline)}, ${formatTime(lobby.confirmDeadline)}`

  return (
    <div className="card">
      <b>Cost</b>
      <div className="lobby-list">
        <Row label="Teren" value={free ? 'Gratuit' : `${lobby.fieldCost} RON`} />
        {!free && lobby.status === 'OPEN' && (
          <>
            <Row label={`Acum, la ${lobby.currentPlayers} jucători`} value={`${lobby.estimatedPrice} RON / persoană`} />
            <Row label="Cu lobby-ul plin" value={`${lobby.priceAtFull} RON / persoană`} />
          </>
        )}
        {!free && lobby.finalPrice != null && <Row label="Preț final" value={`${lobby.finalPrice} RON / persoană`} />}
        <Row label="Confirmare" value={lobby.status === 'OPEN' ? `până ${deadline}` : STATUS[lobby.status]} />
        <Row label="Minim jucători" value={lobby.minPlayers} />
      </div>

      {lobby.minNotReached && <div className="err lobby-note">Termenul a trecut și minimul nu a fost atins.</div>}
      {!free && lobby.status === 'OPEN' && (
        <div className="mu lobby-note lobby-small">Prețul devine final la confirmare. Până atunci poți ieși fără penalizare.</div>
      )}
      {lobby.status === 'LOCKED' && (
        <div className="mu lobby-note lobby-small">De acum, ieșirea din lobby contează ca anulare târzie.</div>
      )}

      {lobby.isHost && lobby.status === 'OPEN' && (
        <div className="lobby-note">
          <button className="b" disabled={busy} onClick={onLock}>Confirmă lobby-ul</button>
        </div>
      )}
    </div>
  )
}

function Row({ label, value }) {
  return (
    <div className="pl">
      <span className="mu">{label}</span>
      <span>{value}</span>
    </div>
  )
}
