import { useNavigate } from 'react-router-dom'
import SportIcon from '../../shared/SportIcon'
import { trField } from '../../shared/i18n'
import { useT } from '../../shared/i18n/useI18n'
import { daysUntil, formatDay, formatTime } from './format'

export default function NextMatchCard({ match }) {
    const navigate = useNavigate()
    const t = useT()

    return (
        <div className="card">
            <div className="mu">{t('Următorul meci')}</div>
            {match ? (
                <>
                    <b className="home-big home-sport">
                        <SportIcon type={match.sportCode} /> {t(match.sportName)} · {trField(match.fieldName)}
                    </b>
                    <div className="mu">
                        {formatDay(match.dateTime)} · {formatTime(match.dateTime)} · {daysUntil(match.dateTime)}
                    </div>
                    <button className="b o home-action" onClick={() => navigate(`/lobbies/${match.id}`)}>{t('Deschide')}</button>
                </>
            ) : (
                <div className="home-empty">
                    {t('Niciun meci programat. Alege un sport din stânga sau creează un lobby cu „+".')}
                </div>
            )}
        </div>
    )
}
