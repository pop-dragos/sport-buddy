import { useState } from 'react'
import { simulateCancel, simulateFinish, simulateJoin, simulatePassDeadline } from '../../api/lobbies'

// Panoul de simulare din prototip: doar pentru organizator și doar cu `npm run dev`.
// run vine din LobbyPage: trimite cererea și afișează lobby-ul actualizat.
export default function SimulationCard({ lobby, busy, run }) {
  const players = lobby.participants.filter((p) => !p.isHost)
  const [playerId, setPlayerId] = useState('')
  // dacă jucătorul ales a ieșit între timp, se ia primul din listă
  const selected = players.some((p) => String(p.userId) === playerId) ? playerId : String(players[0]?.userId ?? '')

  return (
    <div className="card lobby-sim">
      <b>Simulare</b>
      <div className="mu lobby-small">Pentru testare: grăbește ce s-ar întâmpla în timp real.</div>

      <div className="r lobby-note lobby-sim-row">
        {lobby.status === 'OPEN' && (
          <button className="b o" disabled={busy || lobby.freeSlots === 0} onClick={() => run(() => simulateJoin(lobby.id))}>
            Se înscrie un jucător
          </button>
        )}
        {players.length > 0 && (
          <>
            <select value={selected} onChange={(e) => setPlayerId(e.target.value)}>
              {players.map((p) => <option key={p.userId} value={p.userId}>{p.username}</option>)}
            </select>
            <button className="b o" disabled={busy} onClick={() => run(() => simulateCancel(lobby.id, Number(selected)))}>
              Anulează jucătorul
            </button>
          </>
        )}
      </div>

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
  )
}
