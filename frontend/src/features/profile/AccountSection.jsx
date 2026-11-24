import { useState } from 'react'
import { forgotPassword } from '../../api/auth'
import { errorMessage } from '../../api/http'
import { cancelEmailChange, resendEmailChange, updateMe, updateSports } from '../../api/users'
import { useAuth } from '../auth/useAuth'
import { useCooldown } from '../auth/useCooldown'
import AvatarField from './AvatarField'
import ChangeEmailModal from './ChangeEmailModal'
import ChangePasswordModal from './ChangePasswordModal'
import DeleteAccountModal from './DeleteAccountModal'
import { LEVELS } from './levels'

const MIN_AGE = 16
const MAX_AGE = 99

export default function AccountSection({ sports, cities }) {
  const { user, updateUser } = useAuth()

  const [fullName, setFullName] = useState(user.fullName)
  const [phone, setPhone] = useState(user.phone ?? '')
  const [age, setAge] = useState(String(user.age ?? ''))
  const [cityId, setCityId] = useState(user.cityId ? String(user.cityId) : '')
  const [description, setDescription] = useState(user.description ?? '')
  const [message, setMessage] = useState({ text: '', error: false })
  const [busy, setBusy] = useState(false)

  const [sportsMessage, setSportsMessage] = useState('')
  const [modal, setModal] = useState(null)
  // mesajul de lângă butonul de parolă (schimbată / link de setare trimis)
  const [passwordNote, setPasswordNote] = useState('')
  // mesajul de sub email (link retrimis / eroare)
  const [emailNote, setEmailNote] = useState('')
  const [resendLeft, restartResend] = useCooldown(60)

  // Contul Google fără parolă: parola se setează din linkul trimis pe email, ca la resetare.
  async function sendSetPasswordLink() {
    setPasswordNote('')
    try {
      await forgotPassword(user.email)
      setPasswordNote(`Ți-am trimis pe ${user.email} un link pentru setarea parolei.`)
    } catch (e) {
      setPasswordNote(errorMessage(e))
    }
  }

  async function resendEmail() {
    setEmailNote('')
    try {
      await resendEmailChange()
      setEmailNote('Am retrimis linkul de confirmare.')
      restartResend()
    } catch (e) {
      setEmailNote(errorMessage(e))
    }
  }

  async function cancelEmail() {
    setEmailNote('')
    try {
      updateUser(await cancelEmailChange())
    } catch (e) {
      setEmailNote(errorMessage(e))
    }
  }

  async function save() {
    const years = Number(age)
    if (!fullName.trim() || !age || years < MIN_AGE || years > MAX_AGE) {
      setMessage({ text: `Numele și vârsta (${MIN_AGE}–${MAX_AGE}) sunt obligatorii.`, error: true })
      return
    }
    setBusy(true)
    setMessage({ text: '', error: false })
    try {
      // emailul nu se trimite de aici: se schimbă separat, cu confirmare
      updateUser(await updateMe({
        fullName, phone, age: years, description,
        ...(cityId && { cityId: Number(cityId) })
      }))
      setMessage({ text: 'Modificările au fost salvate.', error: false })
    } catch (e) {
      setMessage({ text: errorMessage(e), error: true })
    }
    setBusy(false)
  }

  // Selectul arată mereu nivelul de pe server; dacă salvarea pică, revine singur.
  async function changeLevel(sportId, level) {
    const next = user.sports.filter((s) => s.sportId !== sportId)
    if (level) next.push({ sportId, level })
    setSportsMessage('')
    try {
      updateUser(await updateSports(next))
    } catch (e) {
      setSportsMessage(errorMessage(e))
    }
  }

  return (
    <>
      <div className="card">
        <div className="f">
          <label>Nume afișat</label>
          <input value={fullName} onChange={(e) => setFullName(e.target.value)} />
        </div>
        <div className="f">
          <label>Email</label>
          <div className="r" style={{ flexWrap: 'nowrap' }}>
            <input type="email" value={user.email} readOnly aria-label="Email" />
            <button type="button" className="b o" onClick={() => setModal('email')}>Schimbă</button>
          </div>
          {user.pendingEmail && (
            <div className="note" role="status">
              Așteaptă confirmarea la <b className="ce-mail">{user.pendingEmail}</b>. Deschide linkul trimis acolo;
              până atunci, contul rămâne pe adresa de mai sus.
              <div className="r" style={{ marginTop: 8 }}>
                <button type="button" className="b o" disabled={resendLeft > 0} onClick={resendEmail}>
                  {resendLeft > 0 ? `Retrimite (${resendLeft} s)` : 'Retrimite linkul'}
                </button>
                <button type="button" className="b o" onClick={cancelEmail}>Anulează schimbarea</button>
              </div>
            </div>
          )}
          {emailNote && <span className="mu">{emailNote}</span>}
        </div>
        <div className="f">
          <label>Telefon</label>
          <input type="tel" placeholder="07xx xxx xxx" value={phone} onChange={(e) => setPhone(e.target.value)} />
        </div>
        <div className="f">
          <label>Vârstă *</label>
          <input type="number" min={MIN_AGE} max={MAX_AGE} value={age} onChange={(e) => setAge(e.target.value)} />
        </div>
        <AvatarField />
        <div className="f">
          <label>Oraș implicit</label>
          <select value={cityId} onChange={(e) => setCityId(e.target.value)}>
            {!cityId && <option value="">Alege orașul</option>}
            {cities.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div className="f">
          <label>Descriere</label>
          <textarea
            rows={3}
            maxLength={300}
            placeholder="Câteva cuvinte despre tine"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <div className="r">
          <button type="button" className="b" disabled={busy} onClick={save}>Salvează</button>
          {message.text && <span className={message.error ? 'err' : 'mu'}>{message.text}</span>}
        </div>
      </div>

      <div className="card">
        <b>Sporturi și nivel</b>
        <div className="mu" style={{ marginBottom: 6 }}>
          Nivelul declarat se vede până primești primele review-uri.
        </div>
        {sports.map((sport) => {
          const current = user.sports.find((s) => s.sportId === sport.id)?.level ?? ''
          return (
            <div className="pl" key={sport.id}>
              <span>{sport.name}</span>
              <select style={{ width: 'auto' }} value={current} onChange={(e) => changeLevel(sport.id, e.target.value)}>
                <option value="">Nu joc</option>
                {LEVELS.map((l) => <option key={l.value} value={l.value}>{l.label}</option>)}
              </select>
            </div>
          )
        })}
        {sportsMessage && <div className="err" style={{ marginTop: 8 }}>{sportsMessage}</div>}
      </div>

      <div className="card">
        <div className="pl">
          <span>Parolă</span>
          <span className="r">
            {passwordNote && <span className="mu">{passwordNote}</span>}
            {user.hasPassword === false ? (
              <button type="button" className="b o" onClick={sendSetPasswordLink}>Setează o parolă</button>
            ) : (
              <button type="button" className="b o" onClick={() => setModal('password')}>Schimbă parola</button>
            )}
          </span>
        </div>
        <div className="pl">
          <span>Contul</span>
          <button type="button" className="b o" onClick={() => setModal('delete')}>Șterge contul</button>
        </div>
      </div>

      {modal === 'password' && (
        <ChangePasswordModal
          onClose={() => setModal(null)}
          onDone={() => { setModal(null); setPasswordNote('Parola a fost schimbată. Celelalte dispozitive au fost deconectate.') }}
        />
      )}
      {modal === 'email' && (
        <ChangeEmailModal
          hasPassword={user.hasPassword !== false}
          onClose={() => setModal(null)}
          onDone={(me) => { updateUser(me); setModal(null); setEmailNote(''); restartResend() }}
        />
      )}
      {modal === 'delete' && <DeleteAccountModal onClose={() => setModal(null)} />}
    </>
  )
}
