import { http } from './http'

const post = (path, body) => http(path, { method: 'POST', body: JSON.stringify(body) })

// { googleClientId, mailConfigured }: googleClientId gol înseamnă că butonul Google e simulat
export const getAuthConfig = () => http('/auth/config')

export const login = (identifier, password, rememberMe) =>
  post('/auth/login', { identifier, password, rememberMe })

// Cont cu parolă: { needsVerification, email, setupToken }. Cont Google: { token, expiresAt, user }.
export const register = (data) => post('/auth/register', data)

// credential: token-ul primit de la butonul Google
export const google = (credential, rememberMe) => post('/auth/google', { credential, rememberMe })

export const social = (provider) => post('/auth/social', { provider })

export const verifyEmail = (token) => post('/auth/verify-email', { token })

export const resendVerification = (identifier) => post('/auth/resend-verification', { identifier })

export const forgotPassword = (identifier) => post('/auth/forgot-password', { identifier })

export const resetPassword = (token, newPassword) => post('/auth/reset-password', { token, newPassword })

// din linkul trimis la emailul nou (schimbare cerută din Setări)
export const confirmEmailChange = (token) => post('/auth/confirm-email', { token })

// Poza aleasă la cont nou, înainte de confirmarea emailului (fără sesiune, cu setupToken).
export function uploadSignupAvatar(file, setupToken) {
  const body = new FormData()
  body.append('file', file)
  return http('/auth/signup-avatar', { method: 'POST', body, headers: { 'X-Setup-Token': setupToken } })
}

export const getMe = () => http('/users/me')
