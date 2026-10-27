import { useEffect, useState } from 'react'
import * as authApi from '../../api/auth'
import { clearToken, getToken, saveToken, SESSION_EXPIRED } from '../../api/session'
import { getSettings } from '../../api/users'
import { applyTheme } from '../../shared/theme'
import { AuthContext } from './useAuth'

// Tema aleasă în setări se aplică la fiecare conectare, indiferent de dispozitiv.
function loadTheme() {
  getSettings().then((s) => applyTheme(s.theme)).catch(() => {})
}

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  // loading: avem token și verificăm cu serverul; guest: nu e nimeni conectat; authed: conectat
  const [status, setStatus] = useState(getToken() ? 'loading' : 'guest')

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

  useEffect(() => {
    const onExpired = () => {
      setUser(null)
      setStatus('guest')
    }
    window.addEventListener(SESSION_EXPIRED, onExpired)
    return () => window.removeEventListener(SESSION_EXPIRED, onExpired)
  }, [])

  function startSession(res, remember) {
    saveToken(res.token, remember)
    setUser(res.user)
    setStatus('authed')
    loadTheme()
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
    setUser(null)
    setStatus('guest')
  }

  return (
    <AuthContext.Provider value={{ user, status, login, register, loginWithProvider, logout, updateUser: setUser }}>
      {children}
    </AuthContext.Provider>
  )
}
