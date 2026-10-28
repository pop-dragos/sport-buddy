import { useNavigate } from 'react-router-dom'
import { daysUntil, formatDay, formatTime } from './format'

export default function NextMatchCard({ match }) {
    const navigate = useNavigate()

    return (
        <div className="card">
            <div className="mu">Următorul meci</div>
            {match ? (
                <>
                    <b className="home-big">{match.sportName} · {match.fieldName}</b>
                    <div className="mu">
                        {formatDay(match.dateTime)} · {formatTime(match.dateTime)} · {daysUntil(match.dateTime)}
                    </div>
                    <button className="b o home-action" onClick={() => navigate(`/lobbies/${match.id}`)}>Deschide</button>
                </>
            ) : (
                <div className="home-empty">
                    Niciun meci programat. Alege un sport din stânga sau creează un lobby cu „+".
                </div>
            )}
        </div>
    )
}
