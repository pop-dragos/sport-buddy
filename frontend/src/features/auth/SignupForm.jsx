import { useEffect, useState } from 'react'
import { getSports } from '../../api/catalog'
import { errorMessage } from '../../api/http'
import { useAuth } from './useAuth'
import PasswordInput from './PasswordInput'

const MIN_AGE = 16
const MAX_AGE = 99
const MIN_PASSWORD = 6
// Prototipul nu cere nivelul la cont nou; îl pot schimba ulterior din profil.
const DEFAULT_LEVEL = 'INTERMEDIATE'

// social: null pentru cont cu email și parolă, sau { provider, fullName, email } pentru Google / iCloud
export default function SignupForm({ social }) {
  const { register } = useAuth()

  const [fullName, setFullName] = useState(social?.fullName ?? '')
  const [email, setEmail] = useState(social?.email ?? '')
  const [password, setPassword] = useState('')
  const [age, setAge] = useState('')
  const [phone, setPhone] = useState('')
  const [sportIds, setSportIds] = useState([])
  const [sports, setSports] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  // GET /catalog/sports e public, deci merge și înainte de conectare
  useEffect(() => {
    let cancelled = false
    getSports()
      .then((s) => { if (!cancelled) setSports(s) })
      .catch(() => { if (!cancelled) setSports([]) })
    return () => { cancelled = true }
  }, [])

  function toggleSport(id) {
    setSportIds((ids) => (ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]))
  }

  function validate() {
    if (!fullName.trim()) return 'Numele este obligatoriu.'
    if (!email.trim()) return 'Emailul este obligatoriu.'
    if (!social && password.length < MIN_PASSWORD) {
      return `Parola trebuie să aibă minimum ${MIN_PASSWORD} caractere.`
    }
    const years = Number(age)
    if (!age || years < MIN_AGE) return `Vârsta minimă este ${MIN_AGE}.`
    if (years > MAX_AGE) return `Vârsta maximă este ${MAX_AGE}.`
    if (sportIds.length === 0) return 'Alege cel puțin un sport.'
    return ''
  }

  async function submit() {
    const problem = validate()
    if (problem) {
      setError(problem)
      return
    }
    setBusy(true)
    setError('')
    try {
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        ...(social ? { authProvider: social.provider } : { password }),
        age: Number(age),
        phone: phone.trim() || undefined,
        sports: sportIds.map((sportId) => ({ sportId, level: DEFAULT_LEVEL }))
      })
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  return (
    <>
      {social && (
        <div className="mu">Date preluate din contul tău. Mai completează câmpurile de mai jos.</div>
      )}

      <input placeholder="Nume complet *" value={fullName} onChange={(e) => setFullName(e.target.value)} />
      <input
        type="email"
        placeholder="Email *"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        autoComplete="email"
      />
      {!social && (
        <PasswordInput
          value={password}
          onChange={setPassword}
          placeholder={`Parolă * (minim ${MIN_PASSWORD} caractere)`}
          autoComplete="new-password"
        />
      )}
      <input
        type="number"
        min={MIN_AGE}
        max={MAX_AGE}
        placeholder={`Vârsta * (minim ${MIN_AGE} ani)`}
        value={age}
        onChange={(e) => setAge(e.target.value)}
      />

      <div>
        <b>Sporturi preferate *</b>
        <div className="r" style={{ flexWrap: 'wrap', marginTop: 6 }}>
          {sports === null && <span className="mu">Se încarcă...</span>}
          {sports?.length === 0 && <span className="mu">Sporturile nu sunt disponibile acum.</span>}
          {sports?.map((s) => (
            <button
              key={s.id}
              type="button"
              className={`chip ${sportIds.includes(s.id) ? 'on' : ''}`}
              onClick={() => toggleSport(s.id)}
            >
              {s.name}
            </button>
          ))}
        </div>
      </div>

      <div>
        <b>Opțional</b>
        <div className="mu">Le poți completa mai târziu, din setări.</div>
      </div>
      <input type="tel" placeholder="Număr de telefon" value={phone} onChange={(e) => setPhone(e.target.value)} />

      {error && <div className="err">{error}</div>}
      <button type="button" className="b" disabled={busy} onClick={submit}>Creează contul</button>
    </>
  )
}
