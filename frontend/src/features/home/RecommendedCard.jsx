import { useNavigate } from 'react-router-dom'
import { formatDay, formatRating, formatTime } from './format'

export default function RecommendedCard({ lobbies }) {
    const navigate = useNavigate()

    return (
        <div className="card">
            <div className="mu home-title">Recomandate pentru nivelul tău</div>
            {lobbies.length === 0 && <div className="mu">Nimic momentan.</div>}
            {lobbies.map((l) => (
                <div key={l.id} className="pl">
                    <div>
                        <b>{l.sportName}</b> · {formatDay(l.dateTime)} {formatTime(l.dateTime)}
                        <div className="mu">
                            {l.fieldName} · liber: {l.freeSlots} · nivel {formatRating(l.averageRating)}
                        </div>
                    </div>
                    <button className="b o" onClick={() => navigate(`/lobbies/${l.id}`)}>Vezi</button>
                </div>
            ))}
        </div>
    )
}
