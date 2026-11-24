import { useState } from 'react'
import { acceptRequest, sendRequest } from '../../api/friends'
import { errorMessage } from '../../api/http'
import { useT } from '../../shared/i18n/useI18n'

// Butonul mic de prietenie din listele de jucători (participanții unui lobby).
// status vine din /friends/statuses; onChange primește starea nouă după acțiune.
// Fără buton: prieteni deja, sau setările celuilalt nu permit cereri de la mine.
export default function MiniFriendButton({ userId, status, onChange }) {
  const t = useT()
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function run(action) {
    setBusy(true)
    setError('')
    try {
      onChange(await action())
    } catch (e) {
      setError(errorMessage(e))
    }
    setBusy(false)
  }

  if (!status) return null
  if (status.status === 'FRIENDS') return <span className="fr-mini-label">✓ {t('Prieten')}</span>
  if (status.status === 'PENDING_SENT') return <span className="fr-mini-label">{t('Cerere trimisă')}</span>

  if (status.status === 'PENDING_RECEIVED') {
    return (
      <>
        <button
          type="button"
          className="fr-mini on"
          disabled={busy}
          onClick={() => run(async () => {
            await acceptRequest(status.requestId)
            return { status: 'FRIENDS' }
          })}
        >
          {t('Acceptă cererea')}
        </button>
        {error && <span className="err lobby-small">{error}</span>}
      </>
    )
  }

  if (!status.canAdd) return null
  return (
    <>
      {/* dacă celălalt îmi trimisese deja o cerere, serverul o acceptă direct și întoarce FRIENDS */}
      <button type="button" className="fr-mini" disabled={busy} onClick={() => run(() => sendRequest(userId))}>
        + {t('Prieten')}
      </button>
      {error && <span className="err lobby-small">{error}</span>}
    </>
  )
}
