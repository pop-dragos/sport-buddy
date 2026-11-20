import { motion, useTransform } from 'motion/react'
import { formatRating, formatTime } from '../home/format'
import { dayKey, daysInMonth, leadingBlanks } from '../lobbies/dates'
import { CHOSEN_LOBBY_ID, CITY, DAY_LOBBIES, DAYS_WITH_LOBBIES, MONTH, SELECTED_DAY } from './demoData'

const WEEK_DAYS = ['L', 'M', 'M', 'J', 'V', 'S', 'D']

// Când apar rândurile (una după alta) și când se evidențiază lobby-ul ales, pe progresul scroll-ului
const ROWS_FROM = 0.13
const ROW_STEP = 0.025
const PICK_AT = [0.24, 0.27]

function DayRow({ lobby, index, progress }) {
  const start = ROWS_FROM + index * ROW_STEP
  const opacity = useTransform(progress, [start, start + 0.02], [0, 1])
  const y = useTransform(progress, [start, start + 0.02], [8, 0])
  const ring = useTransform(progress, PICK_AT, [0, 1])
  const free = lobby.max - lobby.current

  return (
    <motion.div className="pl demo-pick" style={{ opacity, y }}>
      {lobby.id === CHOSEN_LOBBY_ID && <motion.i className="demo-pick-ring" style={{ opacity: ring }} />}
      <div>
        {formatTime(lobby.dateTime)} · {lobby.field}
        <div className="mu">
          {lobby.current}/{lobby.max} · liber: {free} · nivel {formatRating(lobby.rating)}
        </div>
      </div>
      <span className="b o">Vezi</span>
    </motion.div>
  )
}

// Fereastra „alege ziua" din CalendarModal: se deschide la click pe Fotbal, cu ziua de azi selectată.
export default function DemoCalendar({ progress }) {
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

  const monthTitle = new Date(year, month, 1).toLocaleDateString('ro-RO', { month: 'long', year: 'numeric' })
  const selectedTitle = new Date(year, month, SELECTED_DAY)
    .toLocaleDateString('ro-RO', { weekday: 'long', day: 'numeric', month: 'long' })

  return (
    <motion.div className="demo-ov" style={{ opacity: backdrop }}>
      <motion.div className="mb" style={{ scale }}>
        <h3>Fotbal în {CITY}: alege ziua</h3>
        <div className="top">
          <span className="b o">‹</span>
          <b>{monthTitle}</b>
          <span className="b o">›</span>
        </div>

        <div className="cal">
          {WEEK_DAYS.map((w, i) => <span key={i}>{w}</span>)}
          {cells}
        </div>

        <b>{selectedTitle}</b>
        {DAY_LOBBIES.map((l, i) => <DayRow key={l.id} lobby={l} index={i} progress={progress} />)}

        <div className="lobby-actions">
          <span className="b o">Închide</span>
        </div>
      </motion.div>
    </motion.div>
  )
}
