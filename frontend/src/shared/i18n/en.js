import auth from './en/auth.js'
import catalog from './en/catalog.js'
import home from './en/home.js'
import landing from './en/landing.js'
import layout from './en/layout.js'
import lobbies from './en/lobbies.js'
import lobbies2 from './en/lobbies2.js'
import profile from './en/profile.js'
import social from './en/social.js'

// Dicționarul românesc -> englezesc, împărțit pe zone, ca fiecare fișier să rămână scurt.
export default {
  ...auth,
  ...catalog,
  ...home,
  ...landing,
  ...layout,
  ...lobbies,
  ...lobbies2,
  ...profile,
  ...social
}
