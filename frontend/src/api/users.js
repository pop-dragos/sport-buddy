import { http } from './http'

const send = (method, path, body) =>
  http(path, { method, body: body === undefined ? undefined : JSON.stringify(body) })

export const getProfile = (id) => http(`/users/${id}`)

export const updateMe = (data) => send('PUT', '/users/me', data)

export const updateSports = (sports) => send('PUT', '/users/me/sports', sports)

export const changePassword = (currentPassword, newPassword) =>
  send('PUT', '/users/me/password', { currentPassword, newPassword })

export const deleteMe = () => send('DELETE', '/users/me')

// Schimbarea emailului: linkul de confirmare merge la adresa nouă. Răspunsul e profilul, cu pendingEmail.
export const requestEmailChange = (newEmail, currentPassword) =>
  send('POST', '/users/me/email', { newEmail, currentPassword })

export const resendEmailChange = () => send('POST', '/users/me/email/resend')

export const cancelEmailChange = () => send('DELETE', '/users/me/email')

export function uploadAvatar(file) {
  const body = new FormData()
  body.append('file', file)
  return http('/users/me/avatar', { method: 'POST', body })
}

export const deleteAvatar = () => send('DELETE', '/users/me/avatar')

export const getSettings = () => http('/users/me/settings')

export const updateSettings = (settings) => send('PUT', '/users/me/settings', settings)
