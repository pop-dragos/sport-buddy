import { useState } from 'react'
import { errorMessage } from '../../api/http'
import { replaceToken } from '../../api/session'
import { changePassword } from '../../api/users'
import Modal from '../../shared/Modal'
import NewPasswordFields from '../auth/NewPasswordFields'
import PasswordInput from '../auth/PasswordInput'
import { passwordProblem } from '../auth/passwordRules'

export default function ChangePasswordModal({ onClose, onDone }) {
  const [current, setCurrent] = useState('')
  const [next, setNext] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e) {
    e.preventDefault()
    if (!current) return setError('Introdu parola curentă.')
    const problem = passwordProblem(next)
    if (problem) return setError(problem)
    if (next !== confirm) return setError('Parolele noi nu coincid.')
    if (next === current) return setError('Parola nouă trebuie să fie diferită de cea curentă.')

    setBusy(true)
    setError('')
    try {
      // celelalte dispozitive sunt deconectate; acesta primește un token nou și rămâne conectat
      const res = await changePassword(current, next)
      if (res?.token) replaceToken(res.token)
      onDone()
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  return (
    <Modal title="Schimbă parola" onClose={onClose}>
      <form onSubmit={submit} className="cpw">
        <PasswordInput value={current} onChange={setCurrent} autoComplete="current-password" placeholder="Parola curentă" />
        <NewPasswordFields
          password={next}
          confirm={confirm}
          onPassword={setNext}
          onConfirm={setConfirm}
          labels={{ password: 'Parola nouă', confirm: 'Repetă parola nouă' }}
        />
        <div className="mu">Vei fi deconectat de pe celelalte dispozitive.</div>
        {error && <div className="err">{error}</div>}
        <div className="r" style={{ justifyContent: 'flex-end' }}>
          <button type="button" className="b o" onClick={onClose}>Anulează</button>
          <button type="submit" className="b" disabled={busy}>Salvează</button>
        </div>
      </form>
    </Modal>
  )
}
