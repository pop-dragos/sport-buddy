import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getPaymentHistory } from '../../api/payments'
import { formatDay, formatTime } from '../home/format'
import './payments.css'

// Istoricul plăților online, pentru Setări > Plăți.
export default function PaymentHistory() {
  const [payments, setPayments] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    let cancelled = false
    getPaymentHistory()
      .then((res) => { if (!cancelled) setPayments(res) })
      .catch(() => { if (!cancelled) setError(true) })
    return () => { cancelled = true }
  }, [])

  return (
    <div className="card">
      <b>Istoric plăți</b>
      {error && <div className="mu pay-hint">Istoricul nu este disponibil acum.</div>}
      {payments?.length === 0 && (
        <div className="mu pay-hint">Plățile tale apar aici după ce plătești online un lobby confirmat.</div>
      )}
      {payments?.map((p) => (
        <div key={p.paymentId} className="pl">
          <span>
            <Link className="pay-link" to={`/lobbies/${p.lobbyId}`}>{p.sportName}, {p.fieldName}</Link>
            <div className="mu pay-small">{formatDay(p.dateTime)}, {formatTime(p.dateTime)}</div>
          </span>
          <span className="pay-amount">
            {p.amount} RON
            {p.status !== 'PAID' && <div className="mu pay-small">refuzată</div>}
          </span>
        </div>
      ))}
    </div>
  )
}
