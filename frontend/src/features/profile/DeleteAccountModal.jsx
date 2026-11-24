import { useState } from 'react'
import { errorMessage } from '../../api/http'
import { deleteMe } from '../../api/users'
import Modal from '../../shared/Modal'
import { useT } from '../../shared/i18n/useI18n'
import { useAuth } from '../auth/useAuth'

export default function DeleteAccountModal({ onClose }) {
  const { logout } = useAuth()
  const t = useT()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function confirm() {
    setBusy(true)
    setError('')
    try {
      await deleteMe()
      logout()
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  return (
    <Modal title={t('Șterge contul')} onClose={onClose}>
      <p style={{ marginTop: 0 }}>
        {t('Contul tău devine inactiv și datele personale sunt șterse. Nu mai poți intra în el. Acțiunea nu se poate anula.')}
      </p>
      {error && <div className="err" style={{ marginBottom: 10 }}>{error}</div>}
      <div className="r" style={{ justifyContent: 'flex-end' }}>
        <button type="button" className="b o" onClick={onClose}>{t('Anulează')}</button>
        <button type="button" className="b" disabled={busy} onClick={confirm}>{t('Șterge contul')}</button>
      </div>
    </Modal>
  )
}
