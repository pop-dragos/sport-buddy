const KEY = 'sb_theme'

// theme: SYSTEM, LIGHT sau DARK (valorile din setările de pe server).
// Se ține minte și local, ca tema să fie aplicată înainte de conectare, fără licărire.
export function applyTheme(theme) {
  const root = document.documentElement
  if (theme === 'LIGHT') root.dataset.theme = 'light'
  else if (theme === 'DARK') root.dataset.theme = 'dark'
  else delete root.dataset.theme

  try {
    localStorage.setItem(KEY, theme)
  } catch {
    // fără stocare, tema se aplică doar în această sesiune
  }
}

export function applyStoredTheme() {
  try {
    applyTheme(localStorage.getItem(KEY) || 'SYSTEM')
  } catch {
    // rămâne tema sistemului
  }
}
