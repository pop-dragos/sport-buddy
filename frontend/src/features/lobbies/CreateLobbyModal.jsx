import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getFields, getSports } from '../../api/catalog'
import { errorMessage } from '../../api/http'
import { createLobby } from '../../api/lobbies'
import Modal from '../../shared/Modal'
import { useCity } from '../catalog/useCity'
import { dayKey } from './dates'

const HOURS = [...Array(24).keys()].map((h) => String(h).padStart(2, '0'))
const MINUTES = ['00', '15', '30', '45']

// Ziua implicită: cea din calendar dacă nu e în trecut, altfel mâine (meciul trebuie să fie la 3+ ore).
function startDay(initialDate) {
  const today = dayKey(new Date())
  if (initialDate && initialDate > today) return initialDate
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  return dayKey(tomorrow)
}

export default function CreateLobbyModal({ initialSport, initialDate, onClose }) {
  const { cities, city } = useCity()
  const navigate = useNavigate()

  const [sports, setSports] = useState([])
  const [fields, setFields] = useState([])
  const [form, setForm] = useState(() => ({
    sportId: initialSport?.id ?? null,
    cityId: city?.id ?? null,
    date: startDay(initialDate),
    hour: '19',
    minute: '00',
    fieldId: null,
    maxPlayers: initialSport?.defaultCapacity ?? 10,
    minPlayers: 2,
    alreadyPlaying: 1,
    fieldCost: 0
  }))
  const [error, setError] = useState(null)
  const [saving, setSaving] = useState(false)
  const [minDate] = useState(() => dayKey(new Date()))

  // până se încarcă listele, se ia primul sport, orașul din header și primul teren
  const sportId = form.sportId ?? sports[0]?.id
  const cityId = form.cityId ?? city?.id
  const fieldId = fields.some((f) => f.id === form.fieldId) ? form.fieldId : fields[0]?.id

  useEffect(() => {
    getSports().then(setSports).catch(() => setSports([]))
  }, [])

  // terenurile depind de oraș și de sport
  useEffect(() => {
    if (!cityId || !sportId) return
    let cancelled = false
    getFields(cityId, sportId)
      .then((res) => { if (!cancelled) setFields(res) })
      .catch(() => { if (!cancelled) setFields([]) })
    return () => { cancelled = true }
  }, [cityId, sportId])

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  // la schimbarea sportului, numărul de locuri devine cel obișnuit pentru sportul ales
  function changeSport(id) {
    const sport = sports.find((s) => s.id === id)
    setForm((f) => ({ ...f, sportId: id, fieldId: null, maxPlayers: sport?.defaultCapacity ?? f.maxPlayers }))
  }

  async function submit(e) {
    e.preventDefault()
    setError(null)
    setSaving(true)
    try {
      const lobby = await createLobby({
        sportId,
        fieldId,
        dateTime: `${form.date}T${form.hour}:${form.minute}:00`,
        maxPlayers: Number(form.maxPlayers),
        minPlayers: Number(form.minPlayers),
        alreadyPlaying: Number(form.alreadyPlaying),
        fieldCost: Number(form.fieldCost)
      })
      onClose()
      navigate(`/lobbies/${lobby.id}`)
    } catch (err) {
      setError(errorMessage(err))
      setSaving(false)
    }
  }

  return (
    <Modal title="Creează lobby" onClose={onClose}>
      <form onSubmit={submit}>
        <div className="f">
          <label>Sport</label>
          <select value={sportId ?? ''} onChange={(e) => changeSport(Number(e.target.value))}>
            {sports.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </div>

        <div className="f">
          <label>Oraș</label>
          <select value={cityId ?? ''} onChange={(e) => setForm((f) => ({ ...f, cityId: Number(e.target.value), fieldId: null }))}>
            {cities.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>

        <div className="f">
          <label>Data</label>
          <input type="date" min={minDate} value={form.date} onChange={(e) => set('date', e.target.value)} required />
        </div>

        <div className="f">
          <label>Ora (0–23)</label>
          <div className="r">
            <select value={form.hour} onChange={(e) => set('hour', e.target.value)}>
              {HOURS.map((h) => <option key={h}>{h}</option>)}
            </select>
            <b>:</b>
            <select value={form.minute} onChange={(e) => set('minute', e.target.value)}>
              {MINUTES.map((m) => <option key={m}>{m}</option>)}
            </select>
          </div>
        </div>

        <div className="f">
          <label>Teren</label>
          <select value={fieldId ?? ''} onChange={(e) => set('fieldId', Number(e.target.value))}>
            {fields.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
          </select>
          {cities.length === 0 && <div className="mu">Se încarcă orașele...</div>}
          {cities.length > 0 && fields.length === 0 && <div className="mu">Nu există terenuri pentru acest sport în orașul ales.</div>}
        </div>

        <div className="f">
          <label>Număr jucători</label>
          <input type="number" min="2" max="50" value={form.maxPlayers} onChange={(e) => set('maxPlayers', e.target.value)} required />
        </div>

        <div className="f">
          <label>Costul terenului (RON, 0 = gratuit)</label>
          <input type="number" min="0" value={form.fieldCost} onChange={(e) => set('fieldCost', e.target.value)} required />
        </div>

        <div className="f">
          <label>Număr minim de jucători</label>
          <input type="number" min="2" value={form.minPlayers} onChange={(e) => set('minPlayers', e.target.value)} required />
        </div>

        <div className="f">
          <label>Câți jucători sunt deja (cu tine)</label>
          <input type="number" min="1" value={form.alreadyPlaying} onChange={(e) => set('alreadyPlaying', e.target.value)} required />
          <div className="mu">Ceilalți din grup nu au nevoie de cont. Căutăm doar completarea locurilor.</div>
        </div>

        {error && <div className="err">{error}</div>}

        <div className="r lobby-actions">
          <button className="b" type="submit" disabled={saving || !fieldId}>
            {saving ? 'Se creează...' : 'Creează lobby'}
          </button>
          <button className="b o" type="button" onClick={onClose}>Anulează</button>
        </div>
      </form>
    </Modal>
  )
}
