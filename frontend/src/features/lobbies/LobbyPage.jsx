import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getStatuses } from '../../api/friends'
import { errorMessage } from '../../api/http'
import {
  acceptRequest, cancelLobby, getLobby, leaveLobby, lockLobby, rejectRequest, requestToJoin, saveAttendance
} from '../../api/lobbies'
import AttendanceCard from './AttendanceCard'
import SimulationCard from './SimulationCard'
import PaymentModal from '../payments/PaymentModal'
import LobbyChat from '../chat/LobbyChat'
import ReviewCard from '../ratings/ReviewCard'
import MiniFriendButton from '../friends/MiniFriendButton'
import Avatar from '../../shared/Avatar'
import Skeleton from '../../shared/Skeleton'
import { tr, trField } from '../../shared/i18n'
import { useI18n, useT } from '../../shared/i18n/useI18n'
import { useMoney } from '../../shared/i18n/money'
import { useAuth } from '../auth/useAuth'
import { formatDay, formatRating, formatTime } from '../home/format'
import './lobbies.css'

function statusLabel(status) {
  return { OPEN: tr('Deschis'), LOCKED: tr('Confirmat'), FINISHED: tr('Încheiat'), CANCELLED: tr('Anulat') }[status]
}

export default function LobbyPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { t, tn } = useI18n()
  const [lobby, setLobby] = useState(null)
  const [error, setError] = useState(null)
  // eroarea ultimei acțiuni (cerere, acceptare...), afișată sub antet
  const [actionError, setActionError] = useState(null)
  const [busy, setBusy] = useState(false)
  // fereastra de plată online e deschisă
  const [paying, setPaying] = useState(false)
  // starea prieteniei cu fiecare participant: { [userId]: { status, requestId?, canAdd? } }
  const [friendStatuses, setFriendStatuses] = useState({})

  useEffect(() => {
    let cancelled = false
    getLobby(id)
      .then((res) => { if (!cancelled) setLobby(res) })
      .catch((err) => { if (!cancelled) setError(errorMessage(err)) })
    return () => { cancelled = true }
  }, [id])

  // o singură cerere pentru toți participanții; se reface doar când se schimbă cine participă
  const othersKey = lobby?.participants.map((p) => p.userId).filter((uid) => uid !== user.id).join(',') ?? ''
  useEffect(() => {
    if (!othersKey) return
    let cancelled = false
    getStatuses(othersKey.split(','))
      .then((res) => { if (!cancelled) setFriendStatuses(res) })
      .catch(() => {})
    return () => { cancelled = true }
  }, [othersKey])

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
    if (late && !window.confirm(t('Lobby-ul e confirmat. Dacă ieși acum, primești o penalizare la rating. Continui?'))) {
      return
    }
    run(() => leaveLobby(lobby.id))
  }

  function cancel() {
    const message = lobby.status === 'LOCKED'
      ? t('Lobby-ul e confirmat. Dacă îl anulezi acum, primești o penalizare la rating. Continui?')
      : t('Sigur anulezi lobby-ul?')
    if (window.confirm(message)) run(() => cancelLobby(lobby.id))
  }

  const back = <button className="lobby-back" onClick={() => navigate(-1)}>‹ {t('Înapoi')}</button>

  if (error) {
    return (
      <div className="lobby-page">
        {back}
        <div className="card">{error}</div>
      </div>
    )
  }
  if (!lobby) {
    return (
      <div className="lobby-page">
        {back}
        <Skeleton lines={4} />
        <Skeleton lines={3} />
      </div>
    )
  }

  return (
    <div className="lobby-page">
      {back}

      {/* pe ecran lat: detaliile și acțiunile în stânga, oamenii și chat-ul în dreapta; sub 900px, o coloană */}
      <div className="lobby-grid">
        <div>
          <div className="card">
            <div className="top">
              <h3 className="lobby-title">{t(lobby.sport.name)}</h3>
              <span className="tag">{statusLabel(lobby.status)}</span>
            </div>
            <div className="mu">{trField(lobby.field.name)}, {lobby.city.name}</div>

            <div className="lobby-facts">
              <Fact label={t('Data')} value={formatDay(lobby.dateTime)} />
              <Fact label={t('Ora')} value={formatTime(lobby.dateTime)} />
              <Fact label={t('Organizator')} value={lobby.host.username} />
              <Fact label={t('Locuri')} value={t('{a} din {b}', { a: lobby.currentPlayers, b: lobby.maxPlayers })} />
            </div>

            <div className="bar">
              <i style={{ width: `${(lobby.currentPlayers / lobby.maxPlayers) * 100}%` }} />
            </div>

            <div className="top">
              <span className="mu">
                {lobby.freeSlots === 0 ? t('Lobby complet') : tn(lobby.freeSlots, '{n} loc liber', '{n} locuri libere')}, {t('nivel mediu {n}', { n: formatRating(lobby.averageRating) })}
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
              <b>{t('Cereri de alăturare')}</b>
              <div className="lobby-list">
                {lobby.pendingRequests.map((r) => (
                  <div key={r.requestId} className="pl">
                    <span className="lobby-player">
                      <Avatar name={r.username} size={28} />
                      <span>
                        <Link className="lk" to={`/profile/${r.userId}`}>{r.username}</Link>
                        <div className="mu lobby-small">
                          {t('rating {n}', { n: formatRating(r.rating) })}
                          {r.reliabilityPercent != null && `, ${t('{n}% prezență', { n: r.reliabilityPercent })}`}
                        </div>
                      </span>
                    </span>
                    <span className="r">
                      <button className="b" disabled={busy} onClick={() => run(() => acceptRequest(lobby.id, r.requestId))}>
                        {t('Acceptă')}
                      </button>
                      <button className="b o" disabled={busy} onClick={() => run(() => rejectRequest(lobby.id, r.requestId))}>
                        {t('Refuză')}
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

          <CostCard
            lobby={lobby}
            me={lobby.participants.find((p) => p.userId === user.id)}
            busy={busy}
            onLock={() => run(() => lockLobby(lobby.id))}
            onPay={() => setPaying(true)}
          />

          {paying && (
            <PaymentModal
              lobby={lobby}
              onClose={() => setPaying(false)}
              onPaid={() => {
                setPaying(false)
                run(() => getLobby(lobby.id))
              }}
            />
          )}

          {/* import.meta.env.DEV e true doar cu `npm run dev`, deci panoul nu ajunge într-un build real */}
          {import.meta.env.DEV && lobby.isHost && (lobby.status === 'OPEN' || lobby.status === 'LOCKED') && (
            <SimulationCard lobby={lobby} busy={busy} run={run} />
          )}
        </div>

        <div>
          <div className="card">
            <b>{t('Participanți')}</b>
            <div className="lobby-list">
              {lobby.participants.map((p) => (
                <div key={p.userId} className="pl">
                  <span className="lobby-player">
                    <Avatar name={p.username} size={28} />
                    <span>
                      <Link className="lk" to={`/profile/${p.userId}`}>{p.username}</Link>
                      {p.userId === user.id && <span className="mu"> {t('(tu)')}</span>}
                      {p.isHost && <div className="mu lobby-small">{t('organizator')}</div>}
                      {p.attended === false && <div className="mu lobby-small">{t('absent')}</div>}
                      {p.userId !== user.id && (
                        <div className="lobby-friend">
                          <MiniFriendButton
                            userId={p.userId}
                            status={friendStatuses[p.userId]}
                            onChange={(s) => setFriendStatuses((all) => ({ ...all, [p.userId]: s }))}
                          />
                        </div>
                      )}
                    </span>
                  </span>
                  <span className="lobby-rating">
                    {formatRating(p.rating)}
                    {p.reliabilityPercent != null && <span className="mu lobby-small">{t('{n}% prezență', { n: p.reliabilityPercent })}</span>}
                  </span>
                </div>
              ))}
              {lobby.broughtPlayers > 0 && (
                <div className="pl mu">
                  {tn(lobby.broughtPlayers, '+{n} jucător adus de organizator', '+{n} jucători aduși de organizator')}
                </div>
              )}
            </div>
          </div>

          {/* după meci, cei care au venit își notează colegii; apoi ratingurile de mai sus se reîncarcă */}
          {lobby.status === 'FINISHED' && lobby.participants.some((p) => p.userId === user.id && p.attended !== false) && (
            <ReviewCard key={lobby.id} lobbyId={lobby.id} onSubmitted={() => getLobby(lobby.id).then(setLobby).catch(() => {})} />
          )}

          <LobbyChat key={lobby.id} lobbyId={lobby.id} canChat={lobby.isHost || lobby.myState === 'ACCEPTED'} />
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
function LobbyAction({ lobby, busy, onJoin, onLeave, onCancel }) {
  const t = useT()
  const active = lobby.status === 'OPEN' || lobby.status === 'LOCKED'

  if (lobby.isHost) {
    return active
      ? <button className="b o" disabled={busy} onClick={onCancel}>{t('Anulează lobby-ul')}</button>
      : <span className="mu">{t('Ești organizatorul')}</span>
  }

  if (lobby.myState === 'ACCEPTED') {
    return active
      ? <button className="b o" disabled={busy} onClick={onLeave}>{t('Ieși din lobby')}</button>
      : <span className="mu">{t('Ai participat')}</span>
  }

  if (lobby.myState === 'PENDING') {
    return (
      <span className="r">
        <span className="mu">{t('Cerere trimisă')}</span>
        <button className="b o" disabled={busy} onClick={onLeave}>{t('Retrage')}</button>
      </span>
    )
  }

  if (lobby.joinClosed) {
    return <button className="b" disabled>{lobby.freeSlots === 0 ? t('Complet') : t('Înscrieri închise')}</button>
  }

  return (
    <span className="r">
      {lobby.myState === 'REJECTED' && <span className="mu">{t('Cererea anterioară a fost refuzată')}</span>}
      <button className="b" disabled={busy} onClick={onJoin}>
        {lobby.myState === 'NONE' ? t('Cere să intri') : t('Cere din nou')}
      </button>
    </span>
  )
}

function CostCard({ lobby, me, busy, onLock, onPay }) {
  const { t, tn } = useI18n()
  const { money } = useMoney()
  const free = lobby.fieldCost === 0
  // plata online: doar participanții, după confirmare, dacă există un preț
  const canPay = me && lobby.status === 'LOCKED' && lobby.finalPrice > 0
  const deadline = `${formatDay(lobby.confirmDeadline)}, ${formatTime(lobby.confirmDeadline)}`

  return (
    <div className="card">
      <b>{t('Cost')}</b>
      <div className="lobby-list">
        <Row label={t('Teren')} value={free ? t('Gratuit') : money(lobby.fieldCost)} />
        {!free && lobby.status === 'OPEN' && (
          <>
            <Row label={tn(lobby.currentPlayers, 'Acum, la {n} jucător', 'Acum, la {n} jucători')} value={t('{price} / persoană', { price: money(lobby.estimatedPrice) })} />
            <Row label={t('Cu lobby-ul plin')} value={t('{price} / persoană', { price: money(lobby.priceAtFull) })} />
          </>
        )}
        {!free && lobby.finalPrice != null && <Row label={t('Preț final')} value={t('{price} / persoană', { price: money(lobby.finalPrice) })} />}
        <Row label={t('Confirmare')} value={lobby.status === 'OPEN' ? t('până {when}', { when: deadline }) : statusLabel(lobby.status)} />
        <Row label={t('Minim jucători')} value={lobby.minPlayers} />
      </div>

      {lobby.minNotReached && <div className="err lobby-note">{t('Termenul a trecut și minimul nu a fost atins.')}</div>}
      {!free && lobby.status === 'OPEN' && (
        <div className="mu lobby-note lobby-small">{t('Prețul devine final la confirmare. Până atunci poți ieși fără penalizare.')}</div>
      )}
      {lobby.status === 'LOCKED' && (
        <div className="mu lobby-note lobby-small">{t('De acum, ieșirea din lobby contează ca anulare târzie.')}</div>
      )}

      {lobby.isHost && lobby.status === 'OPEN' && (
        <div className="lobby-note">
          <button className="b" disabled={busy} onClick={onLock}>{t('Confirmă lobby-ul')}</button>
        </div>
      )}

      {canPay && me.paymentMethod === 'ONLINE' && <div className="mu lobby-note">{t('Ai plătit online.')}</div>}
      {canPay && me.paymentMethod !== 'ONLINE' && (
        <div className="r lobby-note">
          <button className="b" disabled={busy} onClick={onPay}>{t('Plătește online')}</button>
          <span className="mu lobby-small">{t('sau la fața locului')}</span>
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
