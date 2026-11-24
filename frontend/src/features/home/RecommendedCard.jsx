import { useNavigate } from 'react-router-dom'
import SportIcon from '../../shared/SportIcon'
import { trField } from '../../shared/i18n'
import { useT } from '../../shared/i18n/useI18n'
import { formatDay, formatRating, formatTime } from './format'

export default function RecommendedCard({ lobbies }) {
    const navigate = useNavigate()
    const t = useT()

    return (
        <div className="card">
            <div className="mu home-title">{t('Recomandate pentru nivelul tău')}</div>
            {lobbies.length === 0 && <div className="mu">{t('Nimic momentan.')}</div>}
            {lobbies.map((l) => (
                <div key={l.id} className="pl">
                    <div>
                        <div className="home-sport">
                            <SportIcon type={l.sportCode} />
                            <b>{t(l.sportName)}</b> · {formatDay(l.dateTime)} {formatTime(l.dateTime)}
                        </div>
                        <div className="mu">
                            {trField(l.fieldName)} · {t('liber: {n}', { n: l.freeSlots })} · {t('nivel {n}', { n: formatRating(l.averageRating) })}
                        </div>
                    </div>
                    <button className="b o" onClick={() => navigate(`/lobbies/${l.id}`)}>{t('Vezi')}</button>
                </div>
            ))}
        </div>
    )
}
