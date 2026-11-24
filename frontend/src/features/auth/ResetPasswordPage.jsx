import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { resetPassword } from '../../api/auth'
import { errorMessage } from '../../api/http'
import { useT } from '../../shared/i18n/useI18n'
import AuthCard from './AuthCard'
import NewPasswordFields from './NewPasswordFields'
import { passwordProblem } from './passwordRules'

// Pasul 2 al resetării: pagina deschisă din linkul primit pe email (/reset-password?token=...).
export default function ResetPasswordPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const navigate = useNavigate()
  const t = useT()
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  // linkul a expirat sau a fost deja folosit: singura ieșire e un link nou
  const [linkInvalid, setLinkInvalid] = useState(!token)
  const [busy, setBusy] = useState(false)

  async function submit(e) {
    e.preventDefault()
    const problem = passwordProblem(password)
    if (problem) return setError(problem)
    if (confirm !== password) return setError(t('Parolele nu coincid.'))
    setBusy(true)
    setError('')
    try {
      const res = await resetPassword(token, password)
      navigate('/login', { replace: true, state: { notice: res.message } })
    } catch (err) {
      if (err.code === 'INVALID_LINK') setLinkInvalid(true)
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  if (linkInvalid) {
    return (
      <AuthCard title={t('Link expirat')}>
        <p className="mu" style={{ margin: 0, textAlign: 'center' }}>
          {error || t('Linkul de resetare nu este complet.')} {t('Linkurile de resetare sunt valabile o oră și merg o singură dată.')}
        </p>
        <Link to="/forgot-password" className="b" style={{ textAlign: 'center', textDecoration: 'none' }}>
          {t('Cere un link nou')}
        </Link>
        <div style={{ textAlign: 'center' }}>
          <Link to="/login" className="al" style={{ textDecoration: 'none' }}>{t('Înapoi la conectare')}</Link>
        </div>
      </AuthCard>
    )
  }

  return (
    <AuthCard
      title={t('Alege o parolă nouă')}
      subtitle={t('După schimbare vei fi deconectat de pe celelalte dispozitive.')}
      onSubmit={submit}
    >
      <NewPasswordFields
        password={password}
        confirm={confirm}
        onPassword={setPassword}
        onConfirm={setConfirm}
        labels={{ password: t('Parola nouă'), confirm: t('Confirmă parola nouă') }}
        autoFocus
      />
      {error && <div className="err" role="alert">{error}</div>}
      <button type="submit" className="b" disabled={busy}>{t('Schimbă parola')}</button>
    </AuthCard>
  )
}
