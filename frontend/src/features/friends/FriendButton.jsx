import { useEffect, useState } from 'react'
import {
  acceptRequest, cancelRequest, getStatus, rejectRequest, removeFriend, sendRequest
} from '../../api/friends'
import { errorMessage } from '../../api/http'
import { useT } from '../../shared/i18n/useI18n'
import MessagesModal from './MessagesModal'

// Butoanele de pe profilul altcuiva, după starea prieteniei: NONE, PENDING_SENT, PENDING_RECEIVED, FRIENDS.
export default function FriendButton({ profile }) {
  const t = useT()
  const [status, setStatus] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [chatOpen, setChatOpen] = useState(false)

  useEffect(() => {
    let cancelled = false
    getStatus(profile.id)
      .then((s) => { if (!cancelled) setStatus(s) })
      .catch((e) => { if (!cancelled) setError(errorMessage(e)) })
    return () => { cancelled = true }
  }, [profile.id])

  // după orice acțiune cerem din nou starea, ca butoanele să arate exact ce e pe server
  async function run(action) {
    setBusy(true)
    setError('')
    try {
      await action()
      setStatus(await getStatus(profile.id))
    } catch (e) {
      setError(errorMessage(e))
    }
    setBusy(false)
  }

  if (!status) return error ? <div className="err" style={{ marginTop: 12 }}>{error}</div> : null

  return (
    <>
      <div className="r" style={{ marginTop: 12, flexWrap: 'wrap' }}>
        {status.status === 'NONE' && (
          <button type="button" className="b" disabled={busy} onClick={() => run(() => sendRequest(profile.id))}>
            {t('Adaugă prieten')}
          </button>
        )}

        {status.status === 'PENDING_SENT' && (
          <>
            <span className="tag">{t('Cerere trimisă')}</span>
            <button type="button" className="b o" disabled={busy} onClick={() => run(() => cancelRequest(status.requestId))}>
              {t('Anulează cererea')}
            </button>
          </>
        )}

        {status.status === 'PENDING_RECEIVED' && (
          <>
            <button type="button" className="b" disabled={busy} onClick={() => run(() => acceptRequest(status.requestId))}>
              {t('Acceptă cererea')}
            </button>
            <button type="button" className="b o" disabled={busy} onClick={() => run(() => rejectRequest(status.requestId))}>
              {t('Refuză')}
            </button>
          </>
        )}

        {status.status === 'FRIENDS' && (
          <>
            <button type="button" className="b" onClick={() => setChatOpen(true)}>{t('Mesaj')}</button>
            <span className="tag">{t('Prieten')}</span>
            <button type="button" className="b o" disabled={busy} onClick={() => run(() => removeFriend(profile.id))}>
              {t('Elimină din prieteni')}
            </button>
          </>
        )}
      </div>
      {error && <div className="err" style={{ marginTop: 8 }}>{error}</div>}

      {chatOpen && (
        <MessagesModal
          friend={{ userId: profile.id, username: profile.username }}
          onClose={() => setChatOpen(false)}
        />
      )}
    </>
  )
}
