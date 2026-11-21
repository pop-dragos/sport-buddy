import { useEffect, useRef, useState } from 'react'

// Butonul oficial „Continuă cu Google” (Google Identity Services). La apăsare, Google deschide o fereastră
// cu conturile din browser și ne dă un token semnat (credential), pe care îl verifică backend-ul.
const SCRIPT = 'https://accounts.google.com/gsi/client'
let scriptPromise = null

function loadGoogle() {
  if (window.google?.accounts?.id) return Promise.resolve(window.google)
  if (!scriptPromise) {
    scriptPromise = new Promise((resolve, reject) => {
      const s = document.createElement('script')
      s.src = SCRIPT
      s.async = true
      s.onload = () => resolve(window.google)
      s.onerror = () => {
        scriptPromise = null
        reject(new Error('gsi'))
      }
      document.head.appendChild(s)
    })
  }
  return scriptPromise
}

function darkMode() {
  const forced = document.documentElement.dataset.theme
  if (forced) return forced === 'dark'
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export default function GoogleButton({ clientId, onCredential, text = 'continue_with', disabled }) {
  const box = useRef(null)
  const callback = useRef(onCredential)
  const [failed, setFailed] = useState(false)

  // butonul Google e desenat o singură dată; funcția apelată la răspuns poate să se schimbe între timp
  useEffect(() => { callback.current = onCredential })

  useEffect(() => {
    let cancelled = false
    loadGoogle()
      .then((google) => {
        if (cancelled || !box.current) return
        google.accounts.id.initialize({
          client_id: clientId,
          callback: (res) => callback.current(res.credential),
          ux_mode: 'popup',
          auto_select: false,
          cancel_on_tap_outside: true
        })
        google.accounts.id.renderButton(box.current, {
          type: 'standard',
          theme: darkMode() ? 'filled_black' : 'outline',
          size: 'large',
          shape: 'pill',
          text,
          logo_alignment: 'center',
          locale: 'ro',
          width: Math.min(400, Math.max(200, Math.round(box.current.offsetWidth)))
        })
      })
      .catch(() => { if (!cancelled) setFailed(true) })
    return () => { cancelled = true }
  }, [clientId, text])

  if (failed) {
    return <div className="mu err">Nu s-a putut încărca butonul Google. Verifică conexiunea la internet.</div>
  }
  return <div ref={box} className={`gbtn ${disabled ? 'off' : ''}`} />
}
