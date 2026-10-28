import { useNavigate } from 'react-router-dom'

const LABELS = { phone: 'telefon', avatar: 'poză', city: 'oraș', description: 'descriere' }

// Apare doar cât profilul nu e complet.
export default function ProfileCompletionCard({ completion }) {
    const navigate = useNavigate()
    if (!completion || completion.percent >= 100) return null

    const missing = completion.missing.map((key) => LABELS[key] ?? key).join(', ')

    return (
        <div className="card">
            <div className="top">
                <b>Completează-ți profilul</b>
                <span className="mu">{completion.percent}%</span>
            </div>
            <div className="home-bar"><i style={{ width: `${completion.percent}%` }} /></div>
            <div className="mu">Lipsește: {missing}</div>
            <button className="b o home-action" onClick={() => navigate('/settings')}>Completează acum</button>
        </div>
    )
}
