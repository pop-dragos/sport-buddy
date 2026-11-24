import { useState } from 'react'
import { useT } from '../../shared/i18n/useI18n'
import AccountSection from './AccountSection'
import PaymentsSection from './PaymentsSection'
import Toggle from './Toggle'
import { useCatalog } from './useCatalog'
import { useSettings } from './useSettings'

const SECTION_KEYS = ['account', 'look', 'notifications', 'privacy', 'payments']
const SECTION_STORE = 'sb_settings_tab'

// La schimbarea limbii paginile se reiau de la zero; ținem minte tab-ul ca să nu sară înapoi la „Cont".
function readSection() {
  try {
    const stored = sessionStorage.getItem(SECTION_STORE)
    return SECTION_KEYS.includes(stored) ? stored : 'account'
  } catch {
    return 'account'
  }
}

function Choice({ title, value, options, onChange, hint }) {
  return (
    <>
      <div className="pl">
        <span>{title}</span>
        <select style={{ width: 'auto' }} aria-label={title} value={value} onChange={(e) => onChange(e.target.value)}>
          {options.map(([v, label]) => <option key={v} value={v}>{label}</option>)}
        </select>
      </div>
      {hint && <div className="mu settings-hint">{hint}</div>}
    </>
  )
}

export default function SettingsPage() {
  const t = useT()
  const [section, setSectionState] = useState(readSection)
  const { sports, cities } = useCatalog()
  const { settings, error, save } = useSettings()

  const needsSettings = section !== 'account' && section !== 'payments'

  function setSection(next) {
    setSectionState(next)
    try {
      sessionStorage.setItem(SECTION_STORE, next)
    } catch {
      // fără stocare, tab-ul se pierde doar la schimbarea limbii
    }
  }

  const sectionLabels = {
    account: t('Cont'),
    look: t('Aspect'),
    notifications: t('Notificări'),
    privacy: t('Confidențialitate'),
    payments: t('Plăți')
  }
  const themes = [['SYSTEM', t('Sistem')], ['LIGHT', t('Luminos')], ['DARK', t('Întunecat')]]
  // numele limbilor rămân în limba lor, ca să le găsești oricând
  const languages = [['RO', 'Română'], ['EN', 'English']] // i18n-ignore: numele limbilor rămân în limba lor
  const currencies = [['RON', t('RON (leu românesc)')], ['EUR', t('EUR (euro)')], ['USD', t('USD (dolar american)')]]
  const visibility = [['EVERYONE', t('Toată lumea')], ['FRIENDS', t('Doar prietenii')]]
  const whoCanAdd = [['EVERYONE', t('Toată lumea')], ['FRIENDS_OF_FRIENDS', t('Prieteni de prieteni')], ['NOBODY', t('Nimeni')]]

  return (
    <div style={{ maxWidth: 960, margin: '0 auto' }}>
      <div className="r segmented" style={{ marginBottom: 12 }}>
        {SECTION_KEYS.map((key) => (
          <button key={key} type="button" className={`chip ${section === key ? 'on' : ''}`} onClick={() => setSection(key)}>
            {sectionLabels[key]}
          </button>
        ))}
      </div>

      {error && <div className="err" style={{ marginBottom: 10 }}>{error}</div>}

      {section === 'account' && <AccountSection sports={sports} cities={cities} />}

      {needsSettings && !settings && !error && <div className="mu">{t('Se încarcă...')}</div>}

      {section === 'look' && settings && (
        <div className="card">
          <Choice title={t('Temă')} value={settings.theme} options={themes} onChange={(theme) => save({ theme })} />
          <Choice
            title={t('Limbă')}
            value={settings.language ?? 'RO'}
            options={languages}
            onChange={(language) => save({ language })}
          />
          <Choice
            title={t('Monedă')}
            value={settings.currency ?? 'RON'}
            options={currencies}
            onChange={(currency) => save({ currency })}
            hint={t('Prețurile se afișează aproximativ în moneda aleasă (curs BNR). Plata se face mereu în RON.')}
          />
          <Toggle
            title={t('Vremea pe prima pagină')}
            description={t('Prognoza pentru orașul selectat')}
            on={settings.showWeather}
            onChange={(showWeather) => save({ showWeather })}
          />
        </div>
      )}

      {section === 'notifications' && settings && (
        <div className="card">
          <Toggle
            title={t('Cereri de alăturare')}
            description={t('Când cineva vrea în lobby-ul tău')}
            on={settings.notifyJoinRequests}
            onChange={(notifyJoinRequests) => save({ notifyJoinRequests })}
          />
          <Toggle
            title={t('Mesaje')}
            description={t('De la prieteni și din lobby-uri')}
            on={settings.notifyMessages}
            onChange={(notifyMessages) => save({ notifyMessages })}
          />
          <Toggle
            title={t('Reminder înainte de meci')}
            description={t('Cu o oră înainte de început')}
            on={settings.notifyReminder}
            onChange={(notifyReminder) => save({ notifyReminder })}
          />
          <Toggle
            title={t('Rezumat pe email')}
            description={t('Meciurile din oraș, o dată pe săptămână')}
            on={settings.notifyWeeklyDigest}
            onChange={(notifyWeeklyDigest) => save({ notifyWeeklyDigest })}
          />
        </div>
      )}

      {section === 'privacy' && settings && (
        <div className="card">
          <Choice
            title={t('Cine îți vede profilul')}
            value={settings.profileVisibility}
            options={visibility}
            onChange={(profileVisibility) => save({ profileVisibility })}
          />
          <Choice
            title={t('Cine te poate adăuga')}
            value={settings.whoCanAddMe}
            options={whoCanAdd}
            onChange={(whoCanAddMe) => save({ whoCanAddMe })}
          />
          <Toggle
            title={t('Lista de prieteni vizibilă')}
            description={t('Alți utilizatori îți pot vedea prietenii')}
            on={settings.showFriendsList}
            onChange={(showFriendsList) => save({ showFriendsList })}
          />
        </div>
      )}

      {section === 'payments' && <PaymentsSection />}
    </div>
  )
}
