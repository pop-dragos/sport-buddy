import { http } from './http'

const send = (method, path, body) =>
  http(path, { method, body: body === undefined ? undefined : JSON.stringify(body) })

export const getProfile = (id) => http(`/users/${id}`)

export const updateMe = (data) => send('PUT', '/users/me', data)

export const updateSports = (sports) => send('PUT', '/users/me/sports', sports)

export const changePassword = (currentPassword, newPassword) =>
  send('PUT', '/users/me/password', { currentPassword, newPassword })

export const deleteMe = () => send('DELETE', '/users/me')

export const getSettings = () => http('/users/me/settings')

export const updateSettings = (settings) => send('PUT', '/users/me/settings', settings)
