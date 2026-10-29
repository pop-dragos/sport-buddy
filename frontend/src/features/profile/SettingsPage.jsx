import { useState } from 'react'
import AccountSection from './AccountSection'
import PaymentsSection from './PaymentsSection'
import Toggle from './Toggle'
import { useCatalog } from './useCatalog'
import { useSettings } from './useSettings'

const SECTIONS = [
  ['account', 'Cont'],
  ['look', 'Aspect'],
  ['notifications', 'Notificări'],
  ['privacy', 'Confidențialitate'],
  ['payments', 'Plăți']
]

const THEMES = [['SYSTEM', 'Sistem'], ['LIGHT', 'Luminos'], ['DARK', 'Întunecat']]
const VISIBILITY = [['EVERYONE', 'Toată lumea'], ['FRIENDS', 'Doar prietenii']]
const WHO_CAN_ADD = [['EVERYONE', 'Toată lumea'], ['FRIENDS_OF_FRIENDS', 'Prieteni de prieteni'], ['NOBODY', 'Nimeni']]

function Choice({ title, value, options, onChange }) {
  return (
    <div className="pl">
      <span>{title}</span>
      <select style={{ width: 'auto' }} value={value} onChange={(e) => onChange(e.target.value)}>
        {options.map(([v, label]) => <option key={v} value={v}>{label}</option>)}
      </select>
    </div>
  )
}

export default function SettingsPage() {
  const [section, setSection] = useState('account')
  const { sports, cities } = useCatalog()
  const { settings, error, save } = useSettings()

  const needsSettings = section !== 'account' && section !== 'payments'

  return (
    <div style={{ maxWidth: 960, margin: '0 auto' }}>
      <div className="r" style={{ flexWrap: 'wrap', marginBottom: 12 }}>
        {SECTIONS.map(([key, label]) => (
          <button key={key} type="button" className={`chip ${section === key ? 'on' : ''}`} onClick={() => setSection(key)}>
            {label}
          </button>
        ))}
      </div>

      {error && <div className="err" style={{ marginBottom: 10 }}>{error}</div>}

      {section === 'account' && <AccountSection sports={sports} cities={cities} />}

      {needsSettings && !settings && !error && <div className="mu">Se încarcă...</div>}

      {section === 'look' && settings && (
        <div className="card">
          <Choice title="Temă" value={settings.theme} options={THEMES} onChange={(theme) => save({ theme })} />
          <Toggle
            title="Vremea pe prima pagină"
            description="Prognoza pentru orașul selectat"
            on={settings.showWeather}
            onChange={(showWeather) => save({ showWeather })}
          />
        </div>
      )}

      {section === 'notifications' && settings && (
        <div className="card">
          <Toggle
            title="Cereri de alăturare"
            description="Când cineva vrea în lobby-ul tău"
            on={settings.notifyJoinRequests}
            onChange={(notifyJoinRequests) => save({ notifyJoinRequests })}
          />
          <Toggle
            title="Mesaje"
            description="De la prieteni și din lobby-uri"
            on={settings.notifyMessages}
            onChange={(notifyMessages) => save({ notifyMessages })}
          />
          <Toggle
            title="Reminder înainte de meci"
            description="Cu o oră înainte de început"
            on={settings.notifyReminder}
            onChange={(notifyReminder) => save({ notifyReminder })}
          />
          <Toggle
            title="Rezumat pe email"
            description="Meciurile din oraș, o dată pe săptămână"
            on={settings.notifyWeeklyDigest}
            onChange={(notifyWeeklyDigest) => save({ notifyWeeklyDigest })}
          />
        </div>
      )}

      {section === 'privacy' && settings && (
        <div className="card">
          <Choice
            title="Cine îți vede profilul"
            value={settings.profileVisibility}
            options={VISIBILITY}
            onChange={(profileVisibility) => save({ profileVisibility })}
          />
          <Choice
            title="Cine te poate adăuga"
            value={settings.whoCanAddMe}
            options={WHO_CAN_ADD}
            onChange={(whoCanAddMe) => save({ whoCanAddMe })}
          />
          <Toggle
            title="Lista de prieteni vizibilă"
            description="Alți utilizatori îți pot vedea prietenii"
            on={settings.showFriendsList}
            onChange={(showFriendsList) => save({ showFriendsList })}
          />
        </div>
      )}

      {section === 'payments' && <PaymentsSection />}
    </div>
  )
}
