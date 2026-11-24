import { useState } from 'react'
import { forgotPassword } from '../../api/auth'
import { errorMessage } from '../../api/http'
import { cancelEmailChange, resendEmailChange, updateMe, updateSports } from '../../api/users'
import { useI18n } from '../../shared/i18n/useI18n'
import { useAuth } from '../auth/useAuth'
import { useCooldown } from '../auth/useCooldown'
import AvatarField from './AvatarField'
import ChangeEmailModal from './ChangeEmailModal'
import ChangePasswordModal from './ChangePasswordModal'
import DeleteAccountModal from './DeleteAccountModal'
import { LEVELS, levelLabel } from './levels'

const MIN_AGE = 16
const MAX_AGE = 99

export default function AccountSection({ sports, cities }) {
  const { user, updateUser } = useAuth()
  const { t } = useI18n()

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
      setPasswordNote(t('Ți-am trimis pe {email} un link pentru setarea parolei.', { email: user.email }))
    } catch (e) {
      setPasswordNote(errorMessage(e))
    }
  }

  async function resendEmail() {
    setEmailNote('')
    try {
      await resendEmailChange()
      setEmailNote(t('Am retrimis linkul de confirmare.'))
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
      setMessage({ text: t('Numele și vârsta ({min}–{max}) sunt obligatorii.', { min: MIN_AGE, max: MAX_AGE }), error: true })
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
      setMessage({ text: t('Modificările au fost salvate.'), error: false })
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
          <label>{t('Nume afișat')}</label>
          <input value={fullName} onChange={(e) => setFullName(e.target.value)} />
        </div>
        <div className="f">
          <label>{t('Email')}</label>
          <div className="r" style={{ flexWrap: 'nowrap' }}>
            <input type="email" value={user.email} readOnly aria-label={t('Email')} />
            <button type="button" className="b o" onClick={() => setModal('email')}>{t('Schimbă')}</button>
          </div>
          {user.pendingEmail && (
            <div className="note" role="status">
              {t('Așteaptă confirmarea la')} <b className="ce-mail">{user.pendingEmail}</b>
              {t('. Deschide linkul trimis acolo; până atunci, contul rămâne pe adresa de mai sus.')}
              <div className="r" style={{ marginTop: 8 }}>
                <button type="button" className="b o" disabled={resendLeft > 0} onClick={resendEmail}>
                  {resendLeft > 0 ? t('Retrimite ({n} s)', { n: resendLeft }) : t('Retrimite linkul')}
                </button>
                <button type="button" className="b o" onClick={cancelEmail}>{t('Anulează schimbarea')}</button>
              </div>
            </div>
          )}
          {emailNote && <span className="mu">{emailNote}</span>}
        </div>
        <div className="f">
          <label>{t('Telefon')}</label>
          <input type="tel" placeholder={t('07xx xxx xxx')} value={phone} onChange={(e) => setPhone(e.target.value)} />
        </div>
        <div className="f">
          <label>{t('Vârstă *')}</label>
          <input type="number" min={MIN_AGE} max={MAX_AGE} value={age} onChange={(e) => setAge(e.target.value)} />
        </div>
        <AvatarField />
        <div className="f">
          <label>{t('Oraș implicit')}</label>
          <select value={cityId} onChange={(e) => setCityId(e.target.value)}>
            {!cityId && <option value="">{t('Alege orașul')}</option>}
            {cities.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div className="f">
          <label>{t('Descriere')}</label>
          <textarea
            rows={3}
            maxLength={300}
            placeholder={t('Câteva cuvinte despre tine')}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <div className="r">
          <button type="button" className="b" disabled={busy} onClick={save}>{t('Salvează')}</button>
          {message.text && <span className={message.error ? 'err' : 'mu'}>{message.text}</span>}
        </div>
      </div>

      <div className="card">
        <b>{t('Sporturi și nivel')}</b>
        <div className="mu" style={{ marginBottom: 6 }}>
          {t('Nivelul declarat se vede până primești primele review-uri.')}
        </div>
        {sports.map((sport) => {
          const current = user.sports.find((s) => s.sportId === sport.id)?.level ?? ''
          return (
            <div className="pl" key={sport.id}>
              <span>{t(sport.name)}</span>
              <select style={{ width: 'auto' }} value={current} onChange={(e) => changeLevel(sport.id, e.target.value)}>
                <option value="">{t('Nu joc')}</option>
                {LEVELS.map((l) => <option key={l.value} value={l.value}>{levelLabel(l.value)}</option>)}
              </select>
            </div>
          )
        })}
        {sportsMessage && <div className="err" style={{ marginTop: 8 }}>{sportsMessage}</div>}
      </div>

      <div className="card">
        <div className="pl">
          <span>{t('Parolă')}</span>
          <span className="r">
            {passwordNote && <span className="mu">{passwordNote}</span>}
            {user.hasPassword === false ? (
              <button type="button" className="b o" onClick={sendSetPasswordLink}>{t('Setează o parolă')}</button>
            ) : (
              <button type="button" className="b o" onClick={() => setModal('password')}>{t('Schimbă parola')}</button>
            )}
          </span>
        </div>
        <div className="pl">
          <span>{t('Contul')}</span>
          <button type="button" className="b o" onClick={() => setModal('delete')}>{t('Șterge contul')}</button>
        </div>
      </div>

      {modal === 'password' && (
        <ChangePasswordModal
          onClose={() => setModal(null)}
          onDone={() => { setModal(null); setPasswordNote(t('Parola a fost schimbată. Celelalte dispozitive au fost deconectate.')) }}
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
