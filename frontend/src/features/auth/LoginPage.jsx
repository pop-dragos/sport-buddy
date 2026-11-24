import { useState } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { resendVerification } from '../../api/auth'
import { errorMessage } from '../../api/http'
import { useAuth } from './useAuth'
import { useAuthConfig } from './useAuthConfig'
import { useCooldown } from './useCooldown'
import AuthCard from './AuthCard'
import CheckEmail from './CheckEmail'
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

// Înapoi pe pagina de dinainte doar dacă e a ta (sesiunea ți-a expirat) sau nu știm a cui e
// (ai deschis direct un link). Pagina altui utilizator, rămasă după ce el a ieșit, nu contează.
function returnPath(state, user) {
  const from = state?.from
  if (!from) return '/'
  const owner = state.fromUserId
  if (owner != null && owner !== user.id) return '/'
  return from.pathname + (from.search ?? '')
}

export default function LoginPage() {
  const { status, user, login, loginWithProvider, loginWithGoogle } = useAuth()
  const location = useLocation()
  const config = useAuthConfig()

  // „Creează cont" de pe pagina de prezentare deschide direct formularul de cont nou
  const [mode, setMode] = useState(() => (location.state?.mode === 'up' ? 'up' : 'in'))
  const [socialSignup, setSocialSignup] = useState(null)
  // emailul la care s-a trimis linkul de confirmare, după crearea contului
  const [sentTo, setSentTo] = useState(null)
  const [identifier, setIdentifier] = useState(readRemembered)
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(() => readRemembered() !== '')
  const [error, setError] = useState('')
  // mesaj venit de pe altă pagină (de exemplu „Parola a fost schimbată”)
  const [info, setInfo] = useState(location.state?.notice ?? '')
  // contul există și parola e bună, dar emailul nu e confirmat
  const [unverified, setUnverified] = useState(false)
  const [resendLeft, restartResend] = useCooldown(60)
  const [busy, setBusy] = useState(false)

  if (status === 'authed') {
    return <Navigate to={returnPath(location.state, user)} replace />
  }

  function switchMode(next) {
    setMode(next)
    setSocialSignup(null)
    setSentTo(null)
    setError('')
    setInfo('')
    setUnverified(false)
  }

  async function submit(e) {
    e.preventDefault()
    setInfo('')
    setUnverified(false)
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
      if (err.code === 'EMAIL_NOT_VERIFIED') {
        setUnverified(true)
        // serverul tocmai a trimis linkul; butonul de retrimitere așteaptă un minut
        restartResend()
      }
      setBusy(false)
    }
  }

  async function resend() {
    setError('')
    try {
      await resendVerification(identifier.trim())
      setInfo('Am retrimis emailul de confirmare.')
      restartResend()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  // răspunsul de la Google sau de la conectarea simulată: ori conectat, ori trebuie terminat contul
  function afterSocial(provider, res) {
    if (res.needsSignup) {
      setSocialSignup({ provider, ...res.profile, signupToken: res.signupToken })
      setMode('up')
      setError('')
      setBusy(false)
    }
  }

  async function social(provider) {
    setBusy(true)
    setError('')
    try {
      afterSocial(provider, await loginWithProvider(provider))
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  async function google(credential) {
    setBusy(true)
    setError('')
    try {
      afterSocial('GOOGLE', await loginWithGoogle(credential, remember))
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  if (sentTo) {
    return (
      <AuthCard title="Verifică-ți emailul">
        <CheckEmail
          email={sentTo}
          mailConfigured={config?.mailConfigured}
          onResend={() => resendVerification(sentTo)}
        >
          Contul a fost creat. Ca să-l activezi, deschide linkul pe care ți l-am trimis la
        </CheckEmail>
        <div style={{ textAlign: 'center' }}>
          <button type="button" className="al" onClick={() => switchMode('in')}>Înapoi la conectare</button>
        </div>
      </AuthCard>
    )
  }

  const socialProps = {
    onPick: social,
    onGoogleCredential: google,
    // până vine configurarea, nu arătăm butonul simulat ca să nu se schimbe sub cursor
    googleClientId: config?.googleClientId,
    disabled: busy || !config
  }

  return (
    <AuthCard
      title={mode === 'in' ? 'Conectează-te la SportBuddy' : socialSignup ? 'Termină-ți contul' : 'Creează-ți contul'}
      onSubmit={mode === 'in' ? submit : (e) => e.preventDefault()}
    >
      {mode === 'in' ? (
        <>
          <div>
            <div className="lb2">Utilizator sau email</div>
            <input
              style={{ marginTop: 6 }}
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              autoComplete="username"
              aria-label="Utilizator sau email"
            />
          </div>

          <div>
            <div className="lb2">Parolă</div>
            <div style={{ marginTop: 6 }}>
              <PasswordInput value={password} onChange={setPassword} autoComplete="current-password" label="Parolă" />
            </div>
            <div style={{ textAlign: 'right', marginTop: 6 }}>
              <Link
                to="/forgot-password"
                state={{ identifier: identifier.trim() }}
                className="al"
                style={{ textDecoration: 'none' }}
              >
                Ai uitat parola?
              </Link>
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

          {error && <div className="mu err" role="alert">{error}</div>}
          {unverified && (
            <button type="button" className="b o" disabled={resendLeft > 0} onClick={resend}>
              {resendLeft > 0 ? `Retrimite emailul de confirmare (${resendLeft} s)` : 'Retrimite emailul de confirmare'}
            </button>
          )}
          {info && <div className="note" role="status">{info}</div>}

          <button type="submit" className="b" disabled={busy}>Conectează-te</button>

          <div className="mu">
            Prin conectare accepți <Link to="/termeni" className="al">Termenii de utilizare</Link> și{' '}
            <Link to="/confidentialitate" className="al">Politica de confidențialitate</Link>.
          </div>
          <div style={{ textAlign: 'center' }}>
            Nu ai cont încă?{' '}
            <button type="button" className="al" onClick={() => switchMode('up')}>Creează cont</button>
          </div>

          <SocialButtons label="sau conectează-te cu" googleText="signin_with" {...socialProps} />

          <div style={{ textAlign: 'center' }}>
            <Link to="/bun-venit" className="al" style={{ textDecoration: 'none' }}>Vezi cum funcționează</Link>
          </div>
        </>
      ) : (
        <>
          <SignupForm key={socialSignup?.email ?? 'local'} social={socialSignup} onVerificationSent={setSentTo} />

          <div style={{ textAlign: 'center' }}>
            Ai deja cont?{' '}
            <button type="button" className="al" onClick={() => switchMode('in')}>Conectează-te</button>
          </div>

          {!socialSignup && (
            <>
              {error && <div className="mu err" role="alert">{error}</div>}
              <SocialButtons label="sau creează cont cu" googleText="signup_with" {...socialProps} />
            </>
          )}
        </>
      )}
    </AuthCard>
  )
}
