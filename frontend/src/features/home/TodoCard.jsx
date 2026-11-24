import { useNavigate } from 'react-router-dom'
import { useT } from '../../shared/i18n/useI18n'

// Primele două rânduri apar mereu, ca în prototip; celelalte doar când au ceva.
export default function TodoCard({ todo }) {
    const navigate = useNavigate()
    const t = useT()
    const rows = [
        [t('Cereri de aprobat'), todo.pendingRequests, true],
        [t('Review-uri de dat'), todo.reviewsToGive, true],
        [t('Prezență de bifat'), todo.attendanceToSave, false],
        [t('Plăți de făcut'), todo.paymentsToMake, false]
    ].filter(([, count, always]) => always || count > 0)

    return (
        <div className="card">
            <div className="mu">{t('De făcut')}</div>
            {rows.map(([label, count]) => (
                <div key={label} className="pl todo-row">
                    <span>{label}</span>
                    <button className="chip todo-count" onClick={() => navigate('/agenda')}>{count}</button>
                </div>
            ))}
        </div>
    )
}
