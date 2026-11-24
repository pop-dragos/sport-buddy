import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { confirmEmailChange, getMe } from '../../api/auth'
import { errorMessage } from '../../api/http'
import { useAuth } from './useAuth'
import AuthCard from './AuthCard'

// Pagina deschisă din linkul trimis la emailul nou (/confirm-email?token=...).
// Merge și fără conectare; dacă ești conectat, profilul din pagină se actualizează imediat.
export default function ConfirmEmailChangePage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const { status, updateUser } = useAuth()
  const [result, setResult] = useState(null)
  const [error, setError] = useState(token ? '' : 'Linkul de confirmare nu este complet.')
  // linkul merge o singură dată: nu îl trimitem de două ori (React rulează efectele de două ori în dezvoltare)
  const sent = useRef(false)

  useEffect(() => {
    if (!token || sent.current) return
    sent.current = true
    confirmEmailChange(token)
      .then((res) => setResult(res))
      .catch((err) => setError(errorMessage(err)))
  }, [token])

  // conectat în tab-ul acesta: emailul nou apare imediat în Setări
  useEffect(() => {
    if (result && status === 'authed') getMe().then(updateUser).catch(() => {})
  }, [result, status, updateUser])

  const next = status === 'authed'
    ? <Link to="/settings" className="b" style={{ textAlign: 'center', textDecoration: 'none' }}>Mergi la Setări</Link>
    : <Link to="/login" className="b" style={{ textAlign: 'center', textDecoration: 'none' }}>Mergi la conectare</Link>

  if (result) {
    return (
      <AuthCard title="Email schimbat">
        <p className="mu" style={{ margin: 0, textAlign: 'center' }}>
          {result.message} De acum te conectezi și primești emailurile pe această adresă.
        </p>
        {next}
      </AuthCard>
    )
  }

  if (!error) {
    return (
      <AuthCard title="Confirmăm emailul nou...">
        <div className="mu" style={{ textAlign: 'center' }}>O secundă.</div>
      </AuthCard>
    )
  }

  return (
    <AuthCard title="Confirmarea nu a reușit">
      <p className="mu" style={{ margin: 0, textAlign: 'center' }}>{error}</p>
      {next}
    </AuthCard>
  )
}
