import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { errorMessage } from '../../api/http'
import { getLobby } from '../../api/lobbies'
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

  useEffect(() => {
    let cancelled = false
    getLobby(id)
      .then((res) => { if (!cancelled) setLobby(res) })
      .catch((err) => { if (!cancelled) setError(errorMessage(err)) })
    return () => { cancelled = true }
  }, [id])

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
          <LobbyAction lobby={lobby} />
        </div>
      </div>

      <CostCard lobby={lobby} />

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
// Cererile de alăturare și ieșirea din lobby vin odată cu backend-ul pentru JoinRequest.
function LobbyAction({ lobby }) {
  if (lobby.isHost) return <span className="mu">Ești organizatorul</span>
  if (lobby.myState === 'ACCEPTED') return <span className="mu">Ești în lobby</span>
  if (lobby.myState === 'LEFT') return <span className="mu">Ai ieșit din lobby</span>
  if (lobby.joinClosed) {
    return <button className="b" disabled>{lobby.freeSlots === 0 ? 'Complet' : 'Înscrieri închise'}</button>
  }
  return <button className="b" disabled title="În curând">Cere să intri</button>
}

function CostCard({ lobby }) {
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
