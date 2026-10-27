import { http } from './http'

const post = (path, body) => http(path, { method: 'POST', body: JSON.stringify(body) })

export const login = (identifier, password, rememberMe) =>
  post('/auth/login', { identifier, password, rememberMe })

export const register = (data) => post('/auth/register', data)

export const forgotPassword = (identifier) => post('/auth/forgot-password', { identifier })

export const social = (provider) => post('/auth/social', { provider })

export const getMe = () => http('/users/me')
