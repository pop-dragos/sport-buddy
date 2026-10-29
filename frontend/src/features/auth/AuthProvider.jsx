import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import * as authApi from '../../api/auth'
import { clearToken, getToken, saveToken, SESSION_EXPIRED, TOKEN_KEY } from '../../api/session'
import { getSettings } from '../../api/users'
import { applyTheme } from '../../shared/theme'
import { AuthContext } from './useAuth'

// Tema aleasă în setări se aplică la fiecare conectare, indiferent de dispozitiv.
function loadTheme() {
  getSettings().then((s) => applyTheme(s.theme)).catch(() => {})
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
        loadTheme()
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
    loadTheme()
  }

  // Ieșire voită: login-ul de după nu trebuie să ducă pe pagina celui care a ieșit.
  function endSession() {
    applyTheme('SYSTEM')
    setExpiredUserId(null)
    setUser(null)
    setStatus('guest')
    navigate('/login', { replace: true })
  }

  async function login(identifier, password, remember) {
    startSession(await authApi.login(identifier, password, remember), remember)
  }

  async function register(data) {
    startSession(await authApi.register(data), false)
  }

  // Întoarce { needsSignup, profile } când contul nu există încă; altfel conectează utilizatorul.
  async function loginWithProvider(provider) {
    const res = await authApi.social(provider)
    if (res.token) startSession(res, false)
    return res
  }

  function logout() {
    clearToken()
    endSession()
  }

  return (
    <AuthContext.Provider
      value={{ user, status, expiredUserId, login, register, loginWithProvider, logout, updateUser: setUser }}
    >
      {children}
    </AuthContext.Provider>
  )
}
