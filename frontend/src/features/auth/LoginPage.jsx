import { useState } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { forgotPassword } from '../../api/auth'
import { errorMessage } from '../../api/http'
import { useAuth } from './useAuth'
import PasswordInput from './PasswordInput'
import SocialButtons from './SocialButtons'
import SignupForm from './SignupForm'

const REMEMBERED_KEY = 'sb_u'

function readRemembered() {
  try {
    return localStorage.getItem(REMEMBERED_KEY) || ''
  } catch {
    return ''
  }
}

function writeRemembered(identifier, remember) {
  try {
    if (remember) localStorage.setItem(REMEMBERED_KEY, identifier)
    else localStorage.removeItem(REMEMBERED_KEY)
  } catch {
    // fără stocare, doar nu ținem minte utilizatorul
  }
}

export default function LoginPage() {
  const { status, login, loginWithProvider } = useAuth()
  const location = useLocation()

  const [mode, setMode] = useState('in')
  const [socialSignup, setSocialSignup] = useState(null)
  const [identifier, setIdentifier] = useState(readRemembered)
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(() => readRemembered() !== '')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [busy, setBusy] = useState(false)

  if (status === 'authed') {
    return <Navigate to={location.state?.from?.pathname || '/'} replace />
  }

  function switchMode(next) {
    setMode(next)
    setSocialSignup(null)
    setError('')
    setInfo('')
  }

  async function submit(e) {
    e.preventDefault()
    setInfo('')
    if (!identifier.trim() || !password) {
      setError('Completează utilizatorul și parola.')
      return
    }
    setBusy(true)
    setError('')
    try {
      await login(identifier.trim(), password, remember)
      writeRemembered(identifier.trim(), remember)
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  async function forgot() {
    setInfo('')
    if (!identifier.trim()) {
      setError('Scrie întâi utilizatorul sau emailul.')
      return
    }
    setError('')
    try {
      const res = await forgotPassword(identifier.trim())
      setInfo(res.message)
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  async function social(provider) {
    setBusy(true)
    setError('')
    try {
      const res = await loginWithProvider(provider)
      if (res.needsSignup) {
        setSocialSignup({ provider, ...res.profile })
        setMode('up')
        setBusy(false)
      }
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  return (
    <div className="login-page">
      <form className="lg" onSubmit={mode === 'in' ? submit : (e) => e.preventDefault()}>
        <div style={{ textAlign: 'center' }}>
          <span className="mk">S</span>
          <h2 style={{ margin: '12px 0 0', fontSize: 22 }}>
            {mode === 'in' ? 'Conectează-te la SportBuddy' : 'Creează-ți contul'}
          </h2>
        </div>

        {mode === 'in' ? (
          <>
            <div>
              <div className="lb2">Utilizator sau email</div>
              <input
                style={{ marginTop: 6 }}
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                autoComplete="username"
              />
            </div>

            <div>
              <div className="lb2">Parolă</div>
              <div style={{ marginTop: 6 }}>
                <PasswordInput value={password} onChange={setPassword} autoComplete="current-password" />
              </div>
              <div style={{ textAlign: 'right', marginTop: 6 }}>
                <button type="button" className="al" onClick={forgot}>Ai uitat parola?</button>
              </div>
            </div>

            <label className="r" style={{ gap: 8 }}>
              <input
                type="checkbox"
                style={{ width: 'auto' }}
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
              />
              Ține-mă minte
            </label>

            {error && <div className="mu err">{error}</div>}
            {info && <div className="mu">{info}</div>}

            <button type="submit" className="b" disabled={busy}>Conectează-te</button>

            <div className="mu">
              Prin conectare accepți Termenii de utilizare și Politica de confidențialitate.
            </div>
            <div style={{ textAlign: 'center' }}>
              Nu ai cont încă?{' '}
              <button type="button" className="al" onClick={() => switchMode('up')}>Creează cont</button>
            </div>

            <SocialButtons label="sau conectează-te cu" onPick={social} disabled={busy} />
          </>
        ) : (
          <>
            <SignupForm social={socialSignup} />

            <div style={{ textAlign: 'center' }}>
              Ai deja cont?{' '}
              <button type="button" className="al" onClick={() => switchMode('in')}>Conectează-te</button>
            </div>

            {!socialSignup && (
              <>
                {error && <div className="mu err">{error}</div>}
                <SocialButtons label="sau creează cont cu" onPick={social} disabled={busy} />
              </>
            )}
          </>
        )}
      </form>
    </div>
  )
}
