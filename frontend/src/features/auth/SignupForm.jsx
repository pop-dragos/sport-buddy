import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCities, getSports } from '../../api/catalog'
import { errorMessage } from '../../api/http'
import { uploadSignupAvatar } from '../../api/auth'
import { uploadAvatar } from '../../api/users'
import { AVATAR_TYPES, avatarProblem } from '../profile/avatarRules'
import { useT } from '../../shared/i18n/useI18n'
import { useAuth } from './useAuth'
import NewPasswordFields from './NewPasswordFields'
import { passwordProblem } from './passwordRules'

const MIN_AGE = 16
const MAX_AGE = 99
// Prototipul nu cere nivelul la cont nou; îl pot schimba ulterior din profil.
const DEFAULT_LEVEL = 'INTERMEDIATE'

// social: null pentru cont cu email și parolă, sau { provider, fullName, email, signupToken? } pentru Google / iCloud.
// Cu signupToken (Google real), emailul e cel confirmat de Google și nu se poate schimba.
// onVerificationSent(email): contul cu parolă a fost creat și așteaptă confirmarea emailului.
export default function SignupForm({ social, onVerificationSent }) {
  const { register, updateUser } = useAuth()
  const t = useT()

  const [fullName, setFullName] = useState(social?.fullName ?? '')
  const [email, setEmail] = useState(social?.email ?? '')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [age, setAge] = useState('')
  const [phone, setPhone] = useState('')
  const [cityId, setCityId] = useState('')
  const [cities, setCities] = useState([])
  // poza aleasă și previzualizarea ei (URL local, din browser)
  const [photo, setPhoto] = useState(null)
  const [preview, setPreview] = useState(null)
  const photoInput = useRef(null)
  const [sportIds, setSportIds] = useState([])
  const [sports, setSports] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  // GET /catalog/cities e public, ca și sporturile
  useEffect(() => {
    let cancelled = false
    getCities().then((c) => { if (!cancelled) setCities(c) }).catch(() => {})
    return () => { cancelled = true }
  }, [])

  // URL-ul previzualizării ocupă memorie până e eliberat
  useEffect(() => () => { if (preview) URL.revokeObjectURL(preview) }, [preview])

  function pickPhoto(e) {
    const file = e.target.files[0]
    e.target.value = ''
    if (!file) return
    const problem = avatarProblem(file)
    if (problem) return setError(problem)
    setError('')
    setPhoto(file)
    setPreview(URL.createObjectURL(file))
  }

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
    if (!fullName.trim()) return t('Numele este obligatoriu.')
    if (!email.trim()) return t('Emailul este obligatoriu.')
    if (!social) {
      const problem = passwordProblem(password)
      if (problem) return problem
      if (!confirm) return t('Confirmă parola.')
      if (confirm !== password) return t('Parolele nu coincid.')
    }
    const years = Number(age)
    if (!age || years < MIN_AGE) return t('Vârsta minimă este {n}.', { n: MIN_AGE })
    if (years > MAX_AGE) return t('Vârsta maximă este {n}.', { n: MAX_AGE })
    if (sportIds.length === 0) return t('Alege cel puțin un sport.')
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
      const res = await register({
        fullName: fullName.trim(),
        email: email.trim(),
        ...(social ? { authProvider: social.provider, googleSignupToken: social.signupToken } : { password }),
        age: Number(age),
        phone: phone.trim() || undefined,
        ...(cityId && { cityId: Number(cityId) }),
        sports: sportIds.map((sportId) => ({ sportId, level: DEFAULT_LEVEL }))
      })
      // Poza se încarcă după crearea contului. Tipul și mărimea au fost verificate la alegere;
      // dacă totuși nu reușește, contul rămâne creat și poza se poate pune din Setări.
      if (res.needsVerification) {
        // încă nu există sesiune: poza merge cu setupToken-ul primit
        if (photo) await uploadSignupAvatar(photo, res.setupToken).catch(() => {})
        onVerificationSent(res.email)
        return
      }
      if (photo) uploadAvatar(photo).then(updateUser).catch(() => {})
    } catch (err) {
      setError(errorMessage(err))
      setBusy(false)
    }
  }

  return (
    <>
      {social && (
        <div className="mu">{t('Date preluate din contul tău. Mai completează câmpurile de mai jos.')}</div>
      )}

      <input placeholder={t('Nume complet *')} value={fullName} onChange={(e) => setFullName(e.target.value)} />
      <input
        type="email"
        placeholder={t('Email *')}
        aria-label={t('Email')}
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        autoComplete="email"
        readOnly={Boolean(social?.signupToken)}
        title={social?.signupToken ? t('Emailul contului Google') : undefined}
      />
      {!social && (
        <NewPasswordFields password={password} confirm={confirm} onPassword={setPassword} onConfirm={setConfirm} />
      )}
      <input
        type="number"
        min={MIN_AGE}
        max={MAX_AGE}
        placeholder={t('Vârsta * (minim {n} ani)', { n: MIN_AGE })}
        value={age}
        onChange={(e) => setAge(e.target.value)}
      />

      <div>
        <b>{t('Sporturi preferate *')}</b>
        <div className="r" style={{ flexWrap: 'wrap', marginTop: 6 }}>
          {sports === null && <span className="mu">{t('Se încarcă...')}</span>}
          {sports?.length === 0 && <span className="mu">{t('Sporturile nu sunt disponibile acum.')}</span>}
          {sports?.map((s) => (
            <button
              key={s.id}
              type="button"
              className={`chip ${sportIds.includes(s.id) ? 'on' : ''}`}
              onClick={() => toggleSport(s.id)}
            >
              {t(s.name)}
            </button>
          ))}
        </div>
      </div>

      <div>
        <b>{t('Opțional')}</b>
        <div className="mu">{t('Le poți completa mai târziu, din setări.')}</div>
      </div>
      <input type="tel" placeholder={t('Număr de telefon')} value={phone} onChange={(e) => setPhone(e.target.value)} />
      <select value={cityId} onChange={(e) => setCityId(e.target.value)} aria-label={t('Oraș')}>
        <option value="">{t('Oraș')}</option>
        {cities.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
      </select>
      <div className="r" style={{ flexWrap: 'nowrap' }}>
        {preview ? (
          <img className="av" src={preview} alt="" style={{ width: 44, height: 44, objectFit: 'cover' }} />
        ) : (
          <span className="av" style={{ width: 44, height: 44 }}>+</span>
        )}
        <button type="button" className="b o" onClick={() => photoInput.current.click()}>
          {photo ? t('Schimbă poza') : t('Alege o poză')}
        </button>
        {photo && (
          <button type="button" className="b o" onClick={() => { setPhoto(null); setPreview(null) }}>
            {t('Scoate poza')}
          </button>
        )}
        <input ref={photoInput} type="file" accept={AVATAR_TYPES.join(',')} className="hide" onChange={pickPhoto} />
      </div>

      {error && <div className="err">{error}</div>}
      <div className="mu">
        {t('Creând contul, accepți')} <Link to="/termeni" className="al">{t('Termenii de utilizare')}</Link>{' '}
        {t('și')} <Link to="/confidentialitate" className="al">{t('Politica de confidențialitate')}</Link>.
      </div>
      <button type="button" className="b" disabled={busy} onClick={submit}>{t('Creează contul')}</button>
    </>
  )
}
