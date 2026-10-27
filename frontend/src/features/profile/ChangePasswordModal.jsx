import { useState } from 'react'
import { errorMessage } from '../../api/http'
import { changePassword } from '../../api/users'
import Modal from '../../shared/Modal'
import PasswordInput from '../auth/PasswordInput'

const MIN_PASSWORD = 6

export default function ChangePasswordModal({ onClose, onDone }) {
  const [current, setCurrent] = useState('')
  const [next, setNext] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e) {
    e.preventDefault()
    if (!current) return setError('Introdu parola curentă.')
    if (next.length < MIN_PASSWORD) return setError(`Parola nouă trebuie să aibă minimum ${MIN_PASSWORD} caractere.`)
    if (next !== confirm) return setError('Parolele noi nu coincid.')

    setBusy(true)
    setError('')
    try {
      await changePassword(current, next)
      onDone()
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  return (
    <Modal title="Schimbă parola" onClose={onClose}>
      <form onSubmit={submit}>
        <div className="f">
          <label>Parola curentă</label>
          <PasswordInput value={current} onChange={setCurrent} autoComplete="current-password" />
        </div>
        <div className="f">
          <label>Parola nouă</label>
          <PasswordInput value={next} onChange={setNext} autoComplete="new-password" />
        </div>
        <div className="f">
          <label>Repetă parola nouă</label>
          <PasswordInput value={confirm} onChange={setConfirm} autoComplete="new-password" />
        </div>
        {error && <div className="err" style={{ marginBottom: 10 }}>{error}</div>}
        <div className="r" style={{ justifyContent: 'flex-end' }}>
          <button type="button" className="b o" onClick={onClose}>Anulează</button>
          <button type="submit" className="b" disabled={busy}>Salvează</button>
        </div>
      </form>
    </Modal>
  )
}
