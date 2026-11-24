import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getCalendar, getLobbiesForDay } from '../../api/lobbies'
import Modal from '../../shared/Modal'
import { currentLocale, trField } from '../../shared/i18n'
import { useT } from '../../shared/i18n/useI18n'
import CreateLobbyModal from './CreateLobbyModal'
import { useCity } from '../catalog/useCity'
import { formatRating, formatTime } from '../home/format'
import { dayKey, daysInMonth, leadingBlanks, monthKey } from './dates'
import './lobbies.css'

// inițialele zilelor, luni primul, în limba curentă (2024-01-01 a fost luni)
function weekDayInitials() {
    return [0, 1, 2, 3, 4, 5, 6].map((i) =>
        new Date(2024, 0, 1 + i).toLocaleDateString(currentLocale(), { weekday: 'narrow' }))
}

export default function CalendarModal({ sport, onClose }) {
    const { city } = useCity()
    const navigate = useNavigate()
    const t = useT()
    // „azi" se citește o singură dată, când se deschide calendarul
    const [today] = useState(() => new Date())
    const [view, setView] = useState({ year: today.getFullYear(), month: today.getMonth() })
    const [selected, setSelected] = useState(() => dayKey(today))
    const [days, setDays] = useState([])
    const [lobbies, setLobbies] = useState(null)
    const [creating, setCreating] = useState(false)

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

    // „Creează unul" înlocuiește calendarul cu formularul, cu sportul și ziua deja alese
    if (creating) {
        return <CreateLobbyModal initialSport={sport} initialDate={selected} onClose={onClose} />
    }

    const monthTitle = new Date(view.year, view.month, 1)
        .toLocaleDateString(currentLocale(), { month: 'long', year: 'numeric' })
    const selectedTitle = new Date(selected + 'T00:00')
        .toLocaleDateString(currentLocale(), { weekday: 'long', day: 'numeric', month: 'long' })

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
        <Modal title={t('{sport} în {city}: alege ziua', { sport: t(sport.name), city: city?.name ?? '...' })} onClose={onClose}>
            <div className="top calendar-month-nav">
                <button className="b o" onClick={() => changeMonth(-1)}>‹</button>
                <b>{monthTitle}</b>
                <button className="b o" onClick={() => changeMonth(1)}>›</button>
            </div>

            <div className="cal">
                {weekDayInitials().map((w, i) => <span key={i}>{w}</span>)}
                {cells}
            </div>

            <b>{selectedTitle}</b>
            {lobbies === null && <div className="mu lobby-empty">{t('Se încarcă...')}</div>}
            {lobbies?.length === 0 && (
                <>
                    <div className="mu lobby-empty">{t('Niciun lobby în ziua asta.')}</div>
                    <button className="b" onClick={() => setCreating(true)}>{t('Creează unul')}</button>
                </>
            )}
            {lobbies?.map((l) => (
                <div key={l.id} className="pl">
                    <div>
                        {formatTime(l.dateTime)} · {trField(l.fieldName)}
                        <div className="mu">
                            {l.currentPlayers}/{l.maxPlayers} · {t('liber: {n}', { n: l.freeSlots })} · {t('nivel {n}', { n: formatRating(l.averageRating) })}
                        </div>
                    </div>
                    <button className="b o" onClick={() => open(l.id)}>{t('Vezi')}</button>
                </div>
            ))}

            <div className="lobby-actions">
                <button className="b o" onClick={onClose}>{t('Închide')}</button>
            </div>
        </Modal>
    )
}
