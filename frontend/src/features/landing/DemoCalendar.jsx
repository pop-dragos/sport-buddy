import { motion, useTransform } from 'motion/react'
import { currentLocale, trField } from '../../shared/i18n'
import { useT } from '../../shared/i18n/useI18n'
import { formatRating, formatTime } from '../home/format'
import { dayKey, daysInMonth, leadingBlanks } from '../lobbies/dates'
import { CHOSEN_LOBBY_ID, CITY, DAY_LOBBIES, DAYS_WITH_LOBBIES, MONTH, SELECTED_DAY } from './demoData'

// inițialele zilelor, luni primul, în limba curentă (2024-01-01 a fost luni)
function weekDayInitials() {
  return [0, 1, 2, 3, 4, 5, 6].map((i) =>
    new Date(2024, 0, 1 + i).toLocaleDateString(currentLocale(), { weekday: 'narrow' }))
}

// Când apar rândurile (una după alta) și când se evidențiază lobby-ul ales, pe progresul scroll-ului
const ROWS_FROM = 0.13
const ROW_STEP = 0.025
const PICK_AT = [0.24, 0.27]

function DayRow({ lobby, index, progress }) {
  const t = useT()
  const start = ROWS_FROM + index * ROW_STEP
  const opacity = useTransform(progress, [start, start + 0.02], [0, 1])
  const y = useTransform(progress, [start, start + 0.02], [8, 0])
  const ring = useTransform(progress, PICK_AT, [0, 1])
  const free = lobby.max - lobby.current

  return (
    <motion.div className="pl demo-pick" style={{ opacity, y }}>
      {lobby.id === CHOSEN_LOBBY_ID && <motion.i className="demo-pick-ring" style={{ opacity: ring }} />}
      <div>
        {formatTime(lobby.dateTime)} · {trField(lobby.field)}
        <div className="mu">
          {lobby.current}/{lobby.max} · {t('liber: {n}', { n: free })} · {t('nivel {n}', { n: formatRating(lobby.rating) })}
        </div>
      </div>
      <span className="b o">{t('Vezi')}</span>
    </motion.div>
  )
}

// Fereastra „alege ziua" din CalendarModal: se deschide la click pe Fotbal, cu ziua de azi selectată.
export default function DemoCalendar({ progress }) {
  const t = useT()
  // se deschide după click pe Fotbal și se închide la „Vezi", înainte să apară pagina lobby-ului
  const backdrop = useTransform(progress, [0.08, 0.11, 0.29, 0.32], [0, 1, 1, 0])
  const scale = useTransform(progress, [0.08, 0.12], [0.97, 1])

  const { year, month } = MONTH
  const cells = []
  for (let i = 0; i < leadingBlanks(year, month); i++) cells.push(<i key={`blank-${i}`} />)
  for (let d = 1; d <= daysInMonth(year, month); d++) {
    const classes = ['d']
    if (d === SELECTED_DAY) classes.push('sel')
    if (DAYS_WITH_LOBBIES.includes(d)) classes.push('has')
    // butoane, ca în CalendarModal (regula .cal span e doar pentru literele zilelor săptămânii);
    // fereastra e inertă, deci nu pot fi apăsate
    cells.push(
      <button key={dayKey(new Date(year, month, d))} type="button" tabIndex={-1} className={classes.join(' ')}>{d}</button>
    )
  }

  const monthTitle = new Date(year, month, 1).toLocaleDateString(currentLocale(), { month: 'long', year: 'numeric' })
  const selectedTitle = new Date(year, month, SELECTED_DAY)
    .toLocaleDateString(currentLocale(), { weekday: 'long', day: 'numeric', month: 'long' })

  return (
    <motion.div className="demo-ov" style={{ opacity: backdrop }}>
      <motion.div className="mb" style={{ scale }}>
        <h3>{t('{sport} în {city}: alege ziua', { sport: t('Fotbal'), city: CITY })}</h3>
        <div className="top">
          <span className="b o">‹</span>
          <b>{monthTitle}</b>
          <span className="b o">›</span>
        </div>

        <div className="cal">
          {weekDayInitials().map((w, i) => <span key={i}>{w}</span>)}
          {cells}
        </div>

        <b>{selectedTitle}</b>
        {DAY_LOBBIES.map((l, i) => <DayRow key={l.id} lobby={l} index={i} progress={progress} />)}

        <div className="lobby-actions">
          <span className="b o">{t('Închide')}</span>
        </div>
      </motion.div>
    </motion.div>
  )
}
