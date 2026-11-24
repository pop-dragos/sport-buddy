import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import * as authApi from '../../api/auth'
import { clearToken, getToken, saveToken, SESSION_EXPIRED, TOKEN_KEY } from '../../api/session'
import { getSettings, updateSettings } from '../../api/users'
import { getLang, setLang, takeLanguageChoice } from '../../shared/i18n'
import { setCurrency } from '../../shared/i18n/money'
import { applyTheme } from '../../shared/theme'
import { AuthContext } from './useAuth'

// Tema, limba și moneda alese în setări se aplică la fiecare conectare, indiferent de dispozitiv.
// Excepție: limba aleasă chiar înainte de conectare (comutatorul de pe login) se salvează în cont.
function loadSettings() {
  getSettings()
    .then((s) => {
      applyTheme(s.theme)
      if (takeLanguageChoice()) updateSettings({ ...s, language: getLang().toUpperCase() }).catch(() => {})
      else setLang(s.language)
      setCurrency(s.currency)
    })
    .catch(() => {})
}

export default function AuthProvider({ children }) {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)
  // loading: avem token și verificăm cu serverul; guest: nu e nimeni conectat; authed: conectat
  const [status, setStatus] = useState(getToken() ? 'loading' : 'guest')
  // cine era conectat când i-a expirat sesiunea: doar el e trimis înapoi pe pagina unde era
  const [expiredUserId, setExpiredUserId] = useState(null)

  useEffect(() => {
    if (!getToken()) return
    let cancelled = false
    authApi.getMe()
      .then((me) => {
        if (cancelled) return
        setUser(me)
        setStatus('authed')
        loadSettings()
      })
      .catch(() => {
        if (!cancelled) setStatus('guest')
      })
    return () => { cancelled = true }
  }, [])

  // Sesiune expirată (401 de la server): RequireAuth trimite la login și ține minte pagina curentă.
  useEffect(() => {
    const onExpired = () => {
      setExpiredUserId(user?.id ?? null)
      setUser(null)
      setStatus('guest')
    }
    window.addEventListener(SESSION_EXPIRED, onExpired)
    return () => window.removeEventListener(SESSION_EXPIRED, onExpired)
  }, [user])

  // Alt tab a ieșit din cont sau s-a conectat cu alt cont: tab-ul acesta nu are voie să rămână
  // cu utilizatorul vechi pe ecran și cu token-ul altcuiva în cereri.
  useEffect(() => {
    const onStorage = (e) => {
      if (e.key !== TOKEN_KEY && e.key !== null) return
      if (!getToken()) {
        if (user) endSession()
        return
      }
      authApi.getMe()
        .then((me) => {
          // alt cont: pornim de la zero, ca niciun formular sau listă să nu rămână de la cel vechi
          if (me.id !== user?.id) window.location.replace('/')
        })
        .catch(() => {})
    }
    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  })

  function startSession(res, remember) {
    saveToken(res.token, remember)
    setExpiredUserId(null)
    setUser(res.user)
    setStatus('authed')
    loadSettings()
  }

  // Ieșire voită: login-ul de după nu trebuie să ducă pe pagina celui care a ieșit.
  function endSession() {
    applyTheme('LIGHT')
    setExpiredUserId(null)
    setUser(null)
    setStatus('guest')
    navigate('/login', { replace: true })
  }

  async function login(identifier, password, remember) {
    startSession(await authApi.login(identifier, password, remember), remember)
  }

  // Cont cu parolă: întoarce { needsVerification, email, setupToken } și nu conectează (emailul trebuie confirmat).
  // Cont Google / simulat: conectează direct.
  async function register(data) {
    const res = await authApi.register(data)
    if (res.token) startSession(res, false)
    return res
  }

  // Întoarce { needsSignup, profile } când contul nu există încă; altfel conectează utilizatorul.
  async function loginWithProvider(provider) {
    const res = await authApi.social(provider)
    if (res.token) startSession(res, false)
    return res
  }

  // La fel, cu token-ul primit de la butonul Google (plus signupToken pentru terminarea înregistrării).
  async function loginWithGoogle(credential, remember) {
    const res = await authApi.google(credential, remember)
    if (res.token) startSession(res, remember)
    return res
  }

  // Linkul de confirmare din email conectează direct utilizatorul.
  async function verifyEmail(token) {
    startSession(await authApi.verifyEmail(token), false)
  }

  function logout() {
    clearToken()
    endSession()
  }

  return (
    <AuthContext.Provider
      value={{
        user, status, expiredUserId, login, register, loginWithProvider, loginWithGoogle, verifyEmail, logout,
        updateUser: setUser
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
