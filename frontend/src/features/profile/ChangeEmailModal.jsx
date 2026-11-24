import { useState } from 'react'
import { errorMessage } from '../../api/http'
import { requestEmailChange } from '../../api/users'
import Modal from '../../shared/Modal'
import PasswordInput from '../auth/PasswordInput'

// Emailul nou primește un link de confirmare; contul trece pe el abia după confirmare.
// hasPassword: conturile create cu Google fără parolă confirmă doar prin link.
export default function ChangeEmailModal({ hasPassword, onClose, onDone }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e) {
    e.preventDefault()
    if (!email.trim()) return setError('Scrie emailul nou.')
    if (hasPassword && !password) return setError('Introdu parola curentă.')
    setBusy(true)
    setError('')
    try {
      onDone(await requestEmailChange(email.trim(), hasPassword ? password : undefined))
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  return (
    <Modal title="Schimbă emailul" onClose={onClose}>
      <form onSubmit={submit} className="cpw" noValidate>
        <div className="mu">
          Îți trimitem un link de confirmare la adresa nouă. Până îl deschizi, contul rămâne pe emailul actual.
        </div>
        <input
          type="email"
          placeholder="Emailul nou"
          aria-label="Emailul nou"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          autoFocus
        />
        {hasPassword && (
          <PasswordInput value={password} onChange={setPassword} autoComplete="current-password" placeholder="Parola curentă" />
        )}
        {error && <div className="err">{error}</div>}
        <div className="r" style={{ justifyContent: 'flex-end' }}>
          <button type="button" className="b o" onClick={onClose}>Anulează</button>
          <button type="submit" className="b" disabled={busy}>Trimite linkul</button>
        </div>
      </form>
    </Modal>
  )
}
