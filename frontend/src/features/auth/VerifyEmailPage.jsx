import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { errorMessage } from '../../api/http'
import { useT } from '../../shared/i18n/useI18n'
import { useAuth } from './useAuth'
import AuthCard from './AuthCard'

// Pagina deschisă din emailul de confirmare (/verify-email?token=...): confirmă contul și conectează.
export default function VerifyEmailPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const { verifyEmail } = useAuth()
  const navigate = useNavigate()
  const t = useT()
  const [error, setError] = useState(token ? '' : t('Linkul de confirmare nu este complet.'))
  // linkul merge o singură dată: nu îl trimitem de două ori (React rulează efectele de două ori în dezvoltare)
  const sent = useRef(false)

  useEffect(() => {
    if (!token || sent.current) return
    sent.current = true
    verifyEmail(token)
      .then(() => navigate('/', { replace: true }))
      .catch((err) => setError(errorMessage(err)))
  }, [token, verifyEmail, navigate])

  if (!error) {
    return (
      <AuthCard title={t('Confirmăm contul...')}>
        <div className="mu" style={{ textAlign: 'center' }}>{t('O secundă.')}</div>
      </AuthCard>
    )
  }

  return (
    <AuthCard title={t('Confirmarea nu a reușit')}>
      <p className="mu" style={{ margin: 0, textAlign: 'center' }}>{error}</p>
      <p className="mu" style={{ margin: 0, textAlign: 'center' }}>
        {t('Dacă ți-ai confirmat deja contul, te poți conecta. Altfel, conectează-te cu emailul și parola și îți trimitem automat un link nou.')}
      </p>
      <Link to="/login" className="b" style={{ textAlign: 'center', textDecoration: 'none' }}>{t('Mergi la conectare')}</Link>
    </AuthCard>
  )
}
