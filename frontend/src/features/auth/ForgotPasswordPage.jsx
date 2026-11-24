import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { forgotPassword } from '../../api/auth'
import { errorMessage } from '../../api/http'
import { useT } from '../../shared/i18n/useI18n'
import { useAuthConfig } from './useAuthConfig'
import AuthCard from './AuthCard'
import CheckEmail from './CheckEmail'

// Pasul 1 al resetării: cere linkul pe email. Răspunsul nu spune dacă există contul.
export default function ForgotPasswordPage() {
  const location = useLocation()
  const config = useAuthConfig()
  const t = useT()
  const [identifier, setIdentifier] = useState(location.state?.identifier ?? '')
  const [sent, setSent] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e) {
    e.preventDefault()
    if (!identifier.trim()) return setError(t('Scrie utilizatorul sau emailul contului.'))
    setBusy(true)
    setError('')
    try {
      await forgotPassword(identifier.trim())
      setSent(true)
    } catch (err) {
      setError(errorMessage(err))
    }
    setBusy(false)
  }

  const back = (
    <div style={{ textAlign: 'center' }}>
      <Link to="/login" className="al" style={{ textDecoration: 'none' }}>{t('Înapoi la conectare')}</Link>
    </div>
  )

  if (sent) {
    return (
      <AuthCard title={t('Verifică-ți emailul')}>
        <CheckEmail mailConfigured={config?.mailConfigured} onResend={() => forgotPassword(identifier.trim())}>
          {t('Dacă există un cont pentru')} <b>{identifier.trim()}</b>
          {t(', ți-am trimis un link de resetare a parolei. Linkul este valabil o oră.')}
        </CheckEmail>
        {back}
      </AuthCard>
    )
  }

  return (
    <AuthCard
      title={t('Ai uitat parola?')}
      subtitle={t('Îți trimitem pe email un link cu care îți alegi o parolă nouă.')}
      onSubmit={submit}
    >
      <div>
        <div className="lb2">{t('Utilizator sau email')}</div>
        <input
          style={{ marginTop: 6 }}
          value={identifier}
          onChange={(e) => setIdentifier(e.target.value)}
          autoComplete="username"
          aria-label={t('Utilizator sau email')}
          autoFocus
        />
      </div>
      {error && <div className="err" role="alert">{error}</div>}
      <button type="submit" className="b" disabled={busy}>{t('Trimite linkul')}</button>
      {back}
    </AuthCard>
  )
}
