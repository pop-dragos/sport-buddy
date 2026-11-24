import { useNavigate } from 'react-router-dom'
import { useT } from '../../shared/i18n/useI18n'

// Apare doar cât profilul nu e complet.
export default function ProfileCompletionCard({ completion }) {
    const navigate = useNavigate()
    const t = useT()
    if (!completion || completion.percent >= 100) return null

    const labels = { phone: t('telefon'), avatar: t('poză'), city: t('oraș'), description: t('descriere') }
    const missing = completion.missing.map((key) => labels[key] ?? key).join(', ')

    return (
        <div className="card">
            <div className="top">
                <b>{t('Completează-ți profilul')}</b>
                <span className="mu">{completion.percent}%</span>
            </div>
            <div className="home-bar"><i style={{ width: `${completion.percent}%` }} /></div>
            <div className="mu">{t('Lipsește: {missing}', { missing })}</div>
            <button className="b o home-action" onClick={() => navigate('/settings')}>{t('Completează acum')}</button>
        </div>
    )
}
