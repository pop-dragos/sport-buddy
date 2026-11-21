import { useState } from 'react'
import { errorMessage } from '../../api/http'
import { useCooldown } from './useCooldown'

// Serverul trimite cel mult un email pe minut pentru un cont, așa că butonul așteaptă la fel.
const RESEND_SECONDS = 60

// „Verifică-ți emailul”: după crearea contului sau după cererea de resetare a parolei.
export default function CheckEmail({ email, children, onResend, mailConfigured }) {
  const [left, restart] = useCooldown(RESEND_SECONDS, true)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function resend() {
    setMessage('')
    setError('')
    try {
      await onResend()
      setMessage('Am retrimis emailul.')
      restart()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <div className="ce">
      <div className="ce-ic" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="5" width="18" height="14" rx="2.5" />
          <path d="M3.5 7l8.5 6 8.5-6" />
        </svg>
      </div>
      <p>
        {children}
        {email && <> <b className="ce-mail">{email}</b>.</>}
      </p>
      <p className="mu">Nu găsești emailul? Uită-te și în dosarul Spam sau Promoții.</p>
      {mailConfigured === false && (
        <div className="note">
          Serverul nu are emailul configurat încă: linkul apare în consola backend-ului.
        </div>
      )}
      <button type="button" className="b o" disabled={left > 0} onClick={resend}>
        {left > 0 ? `Retrimite emailul (${left} s)` : 'Retrimite emailul'}
      </button>
      {message && <div className="mu">{message}</div>}
      {error && <div className="err">{error}</div>}
    </div>
  )
}
