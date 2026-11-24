import { useEffect, useState } from 'react'
import { errorMessage } from '../../api/http'
import { getPendingReviews, submitReviews } from '../../api/ratings'
import { useT } from '../../shared/i18n/useI18n'
import './ratings.css'

const STARS = [1, 2, 3, 4, 5]

// Cardul „Review colegi de meci" din pagina lobby-ului, după meci (ca în prototip).
// Notele se trimit o singură dată, deci butonul se activează abia când toți colegii au o notă.
export default function ReviewCard({ lobbyId, onSubmitted }) {
    const t = useT()
    const [pending, setPending] = useState(null)
    const [scores, setScores] = useState({})
    const [sent, setSent] = useState(false)
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    useEffect(() => {
        let cancelled = false
        getPendingReviews(lobbyId)
            .then((list) => { if (!cancelled) setPending(list) })
            .catch((e) => { if (!cancelled) setError(errorMessage(e)) })
        return () => { cancelled = true }
    }, [lobbyId])

    const allRated = pending?.length > 0 && pending.every((p) => scores[p.userId])

    async function submit() {
        setBusy(true)
        setError('')
        try {
            await submitReviews(lobbyId, pending.map((p) => ({ userId: p.userId, score: scores[p.userId] })))
            setSent(true)
            onSubmitted?.()
        } catch (e) {
            setError(errorMessage(e))
        }
        setBusy(false)
    }

    if (!pending && !error) return null

    return (
        <div className="card">
            <b>{t('Review colegi de meci')}</b>
            <div className="mu review-hint">{t('Notează de la 1 la 5 cum a jucat fiecare.')}</div>

            {sent && <div className="tag">{t('Review trimis, mulțumim.')}</div>}
            {!sent && pending?.length === 0 && <div className="mu">{t('Nu mai ai pe nimeni de notat la acest meci.')}</div>}

            {!sent && pending?.map((p) => (
                <div key={p.userId} className="pl review-row">
                    <span>{p.username}</span>
                    <span className="r">
                        {STARS.map((n) => (
                            <button
                                key={n}
                                className={`star${(scores[p.userId] || 0) >= n ? ' on' : ''}`}
                                title={t('{n} din 5', { n })}
                                onClick={() => setScores({ ...scores, [p.userId]: n })}
                            >
                                ★
                            </button>
                        ))}
                    </span>
                </div>
            ))}

            {!sent && pending?.length > 0 && (
                <button className="b review-send" disabled={!allRated || busy} onClick={submit}>
                    {t('Trimite review-urile')}
                </button>
            )}
            {error && <div className="err review-hint">{error}</div>}
        </div>
    )
}
