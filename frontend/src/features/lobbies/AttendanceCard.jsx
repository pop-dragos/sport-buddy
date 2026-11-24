import { useState } from 'react'
import Avatar from '../../shared/Avatar'
import { useT } from '../../shared/i18n/useI18n'

// După meci, organizatorul bifează cine a venit. Implicit toți sunt bifați prezenți.
export default function AttendanceCard({ lobby, busy, onSave }) {
  const t = useT()
  const players = lobby.participants.filter((p) => !p.isHost)
  const [absent, setAbsent] = useState(() => new Set())

  function toggle(userId) {
    setAbsent((prev) => {
      const next = new Set(prev)
      if (next.has(userId)) next.delete(userId)
      else next.add(userId)
      return next
    })
  }

  function save() {
    onSave(players.map((p) => ({ userId: p.userId, present: !absent.has(p.userId) })))
  }

  return (
    <div className="card">
      <b>{t('Cine a venit?')}</b>
      <div className="mu lobby-small">{t('Debifează jucătorii care au lipsit fără să anunțe. Se poate salva o singură dată.')}</div>

      <div className="lobby-list">
        {players.length === 0 && <div className="mu lobby-note">{t('Nu ai alți jucători cu cont în lobby.')}</div>}
        {players.map((p) => (
          <label key={p.userId} className="pl lobby-check">
            <span className="lobby-player">
              <Avatar name={p.username} size={28} />
              {p.username}
            </span>
            <input type="checkbox" checked={!absent.has(p.userId)} onChange={() => toggle(p.userId)} />
          </label>
        ))}
      </div>

      <div className="lobby-note">
        <button className="b" disabled={busy} onClick={save}>{t('Salvează prezența')}</button>
      </div>
    </div>
  )
}
