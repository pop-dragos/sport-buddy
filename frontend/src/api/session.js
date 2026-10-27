const KEY = 'sb_token'

export const SESSION_EXPIRED = 'sb:session-expired'

function read(store) {
  try {
    return store.getItem(KEY)
  } catch {
    return null
  }
}

export function getToken() {
  return read(localStorage) ?? read(sessionStorage)
}

// Cu "Ține-mă minte" token-ul rămâne și după închiderea browserului, altfel doar cât ține tab-ul.
export function saveToken(token, remember) {
  clearToken()
  try {
    ;(remember ? localStorage : sessionStorage).setItem(KEY, token)
  } catch {
    // browser fără stocare: sesiunea ține doar cât e pagina deschisă
  }
}

export function clearToken() {
  try {
    localStorage.removeItem(KEY)
    sessionStorage.removeItem(KEY)
  } catch {
    // nimic de șters
  }
}
