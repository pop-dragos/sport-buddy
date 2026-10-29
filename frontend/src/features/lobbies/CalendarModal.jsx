import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getCalendar, getLobbiesForDay } from '../../api/lobbies'
import Modal from '../../shared/Modal'
import { useCity } from '../catalog/useCity'
import { formatRating, formatTime } from '../home/format'
import { dayKey, daysInMonth, leadingBlanks, monthKey } from './dates'
import './lobbies.css'

const WEEK_DAYS = ['L', 'M', 'M', 'J', 'V', 'S', 'D']

export default function CalendarModal({ sport, onClose }) {
    const { city } = useCity()
    const navigate = useNavigate()
    // „azi" se citește o singură dată, când se deschide calendarul
    const [today] = useState(() => new Date())
    const [view, setView] = useState({ year: today.getFullYear(), month: today.getMonth() })
    const [selected, setSelected] = useState(() => dayKey(today))
    const [days, setDays] = useState([])
    const [lobbies, setLobbies] = useState(null)

    // zilele cu punct, de fiecare dată când se schimbă luna
    useEffect(() => {
        if (!city) return
        let cancelled = false
        getCalendar(city.id, sport.id, monthKey(view.year, view.month))
            .then((res) => { if (!cancelled) setDays(res.days) })
            .catch(() => { if (!cancelled) setDays([]) })
        return () => { cancelled = true }
    }, [city, sport.id, view])

    // lobby-urile zilei alese
    useEffect(() => {
        if (!city) return
        let cancelled = false
        getLobbiesForDay(city.id, sport.id, selected)
            .then((res) => { if (!cancelled) setLobbies(res) })
            .catch(() => { if (!cancelled) setLobbies([]) })
        return () => { cancelled = true }
    }, [city, sport.id, selected])

    function changeMonth(delta) {
        const d = new Date(view.year, view.month + delta, 1)
        setView({ year: d.getFullYear(), month: d.getMonth() })
    }

    function open(id) {
        onClose()
        navigate(`/lobbies/${id}`)
    }

    const monthTitle = new Date(view.year, view.month, 1)
        .toLocaleDateString('ro-RO', { month: 'long', year: 'numeric' })
    const selectedTitle = new Date(selected + 'T00:00')
        .toLocaleDateString('ro-RO', { weekday: 'long', day: 'numeric', month: 'long' })

    const cells = []
    for (let i = 0; i < leadingBlanks(view.year, view.month); i++) {
        cells.push(<i key={`blank-${i}`} />)
    }
    for (let d = 1; d <= daysInMonth(view.year, view.month); d++) {
        const key = dayKey(new Date(view.year, view.month, d))
        const classes = ['d']
        if (key === selected) classes.push('sel')
        if (key === dayKey(today)) classes.push('td')
        if (days.includes(key)) classes.push('has')
        cells.push(
            <button key={key} className={classes.join(' ')} onClick={() => setSelected(key)}>{d}</button>
        )
    }

    return (
        <Modal title={`${sport.name} în ${city?.name ?? '...'}: alege ziua`} onClose={onClose}>
            <div className="top">
                <button className="b o" onClick={() => changeMonth(-1)}>‹</button>
                <b>{monthTitle}</b>
                <button className="b o" onClick={() => changeMonth(1)}>›</button>
            </div>

            <div className="cal">
                {WEEK_DAYS.map((w, i) => <span key={i}>{w}</span>)}
                {cells}
            </div>

            <b>{selectedTitle}</b>
            {lobbies === null && <div className="mu lobby-empty">Se încarcă...</div>}
            {lobbies?.length === 0 && <div className="mu lobby-empty">Niciun lobby în ziua asta.</div>}
            {lobbies?.map((l) => (
                <div key={l.id} className="pl">
                    <div>
                        {formatTime(l.dateTime)} · {l.fieldName}
                        <div className="mu">
                            {l.currentPlayers}/{l.maxPlayers} · liber: {l.freeSlots} · nivel {formatRating(l.averageRating)}
                        </div>
                    </div>
                    <button className="b o" onClick={() => open(l.id)}>Vezi</button>
                </div>
            ))}

            <div className="lobby-actions">
                <button className="b o" onClick={onClose}>Închide</button>
            </div>
        </Modal>
    )
}
