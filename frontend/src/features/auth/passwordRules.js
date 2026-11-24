import { tr } from '../../shared/i18n'

// Aceleași reguli ca în backend (PasswordPolicy): minimum 8 caractere, cel puțin o literă și o cifră.
export const MIN_PASSWORD = 8
export const MAX_PASSWORD = 72

export function passwordChecks(pw) {
  return [
    { ok: pw.length >= MIN_PASSWORD, label: tr('Minimum {n} caractere', { n: MIN_PASSWORD }) },
    { ok: /\p{L}/u.test(pw), label: tr('O literă') },
    { ok: /\d/.test(pw), label: tr('O cifră') }
  ]
}

// Mesajul pentru primul criteriu neîndeplinit, sau '' dacă parola e acceptată.
export function passwordProblem(pw) {
  if (pw.length < MIN_PASSWORD) return tr('Parola trebuie să aibă minimum {n} caractere.', { n: MIN_PASSWORD })
  if (pw.length > MAX_PASSWORD) return tr('Parola poate avea maximum {n} de caractere.', { n: MAX_PASSWORD })
  if (!/\p{L}/u.test(pw)) return tr('Parola trebuie să conțină cel puțin o literă.')
  if (!/\d/.test(pw)) return tr('Parola trebuie să conțină cel puțin o cifră.')
  return ''
}

// Parole pe care le încearcă oricine primul; o parolă care le conține nu e mai mult decât slabă.
const COMMON = ['parola', 'password', 'qwerty', 'azerty', 'sportbuddy', 'admin', 'welcome', 'iloveyou',
  'abc123', '123456', '111111', 'football', 'fotbal', 'letmein']

// Scor 0–4 pentru bara de putere: 0 = nimic scris, 1 = slabă, 2 = acceptabilă, 3 = bună, 4 = puternică.
// O parolă care nu respectă regulile e mereu slabă.
export function passwordStrength(pw) {
  if (!pw) return 0
  if (passwordProblem(pw)) return 1
  const lower = pw.toLowerCase()
  if (COMMON.some((w) => lower.includes(w)) || /^(.)\1+$/.test(pw) || new Set(pw).size < 4) return 1

  let score = 2
  if (pw.length >= 12) score++
  const kinds = [/[a-z]/, /[A-Z]/, /\d/, /[^\p{L}\d]/u].filter((r) => r.test(pw)).length
  if (kinds >= 3) score++
  if (pw.length >= 16 && kinds >= 2) score++
  return Math.min(score, 4)
}

export function strengthLabel(score) {
  return [null, tr('Slabă'), tr('Acceptabilă'), tr('Bună'), tr('Puternică')][score] ?? ''
}
