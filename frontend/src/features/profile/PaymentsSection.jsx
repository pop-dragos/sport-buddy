import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage } from '../../api/http'
import { getPaymentHistory } from '../../api/payments'
import { formatDay, formatTime } from '../home/format'

// stările plății din backend (PaymentStatus) și cum le arătăm
const STATUS = {
  PAID: { label: 'Plătită', style: { background: '#dcfce7', color: '#166534', borderColor: '#bbf7d0' } },
  FAILED: { label: 'Refuzată', style: {} },
  PENDING: { label: 'În așteptare', style: {} },
  REFUNDED: { label: 'Rambursată', style: {} }
}

// Plățile online (simulate) făcute după confirmarea lobby-ului. Plata la fața locului nu apare aici.
export default function PaymentsSection() {
  const [payments, setPayments] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    getPaymentHistory()
      .then((p) => { if (!cancelled) setPayments(p) })
      .catch((e) => { if (!cancelled) setError(errorMessage(e)) })
    return () => { cancelled = true }
  }, [])

  if (error) return <div className="card err">{error}</div>
  if (!payments) return <div className="mu">Se încarcă...</div>

  return (
    <div className="card">
      <b>Istoric plăți</b>
      {payments.length === 0 && <div className="mu" style={{ marginTop: 6 }}>Nu ai nicio plată încă.</div>}
      {payments.map((p) => {
        const status = STATUS[p.status] ?? { label: p.status, style: {} }
        return (
          <div className="pl" key={p.paymentId}>
            <div style={{ minWidth: 0 }}>
              <Link to={`/lobbies/${p.lobbyId}`} style={{ color: 'inherit' }}>
                <b>{p.sportName} · {p.fieldName}</b>
              </Link>
              <div className="mu">Meciul din {formatDay(p.dateTime)}, ora {formatTime(p.dateTime)}</div>
            </div>
            <span className="r" style={{ flexShrink: 0 }}>
              <b>{p.amount} RON</b>
              <span className="tag" style={status.style}>{status.label}</span>
            </span>
          </div>
        )
      })}
    </div>
  )
}
