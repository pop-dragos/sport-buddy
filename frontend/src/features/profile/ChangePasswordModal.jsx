import { useState } from 'react'
import { errorMessage } from '../../api/http'
import { replaceToken } from '../../api/session'
import { changePassword } from '../../api/users'
import Modal from '../../shared/Modal'
import { useT } from '../../shared/i18n/useI18n'
import NewPasswordFields from '../auth/NewPasswordFields'
import PasswordInput from '../auth/PasswordInput'
import { passwordProblem } from '../auth/passwordRules'

export default function ChangePasswordModal({ onClose, onDone }) {
  const t = useT()
  const [current, setCurrent] = useState('')
  const [next, setNext] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e) {
    e.preventDefault()
    if (!current) return setError(t('Introdu parola curentă.'))
    const problem = passwordProblem(next)
    if (problem) return setError(problem)
    if (next !== confirm) return setError(t('Parolele noi nu coincid.'))
    if (next === current) return setError(t('Parola nouă trebuie să fie diferită de cea curentă.'))

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
    <Modal title={t('Schimbă parola')} onClose={onClose}>
      <form onSubmit={submit} className="cpw">
        <PasswordInput value={current} onChange={setCurrent} autoComplete="current-password" placeholder={t('Parola curentă')} />
        <NewPasswordFields
          password={next}
          confirm={confirm}
          onPassword={setNext}
          onConfirm={setConfirm}
          labels={{ password: t('Parola nouă'), confirm: t('Repetă parola nouă') }}
        />
        <div className="mu">{t('Vei fi deconectat de pe celelalte dispozitive.')}</div>
        {error && <div className="err">{error}</div>}
        <div className="r" style={{ justifyContent: 'flex-end' }}>
          <button type="button" className="b o" onClick={onClose}>{t('Anulează')}</button>
          <button type="submit" className="b" disabled={busy}>{t('Salvează')}</button>
        </div>
      </form>
    </Modal>
  )
}
