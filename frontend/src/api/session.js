export const TOKEN_KEY = 'sb_token'

export const SESSION_EXPIRED = 'sb:session-expired'

function read(store) {
  try {
    return store.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

// Întâi token-ul tab-ului (fără „Ține-mă minte”), apoi cel comun tuturor tab-urilor:
// așa, o conectare cu alt cont în alt tab nu înlocuiește contul din tab-ul acesta.
export function getToken() {
  return read(sessionStorage) ?? read(localStorage)
}

// Cu "Ține-mă minte" token-ul rămâne și după închiderea browserului, altfel doar cât ține tab-ul.
export function saveToken(token, remember) {
  clearToken()
  try {
    ;(remember ? localStorage : sessionStorage).setItem(TOKEN_KEY, token)
  } catch {
    // browser fără stocare: sesiunea ține doar cât e pagina deschisă
  }
}

export function clearToken() {
  try {
    localStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(TOKEN_KEY)
  } catch {
    // nimic de șters
  }
}
