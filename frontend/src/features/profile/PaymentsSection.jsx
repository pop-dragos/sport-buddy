import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage } from '../../api/http'
import { getPaymentHistory } from '../../api/payments'
import { tr, trField } from '../../shared/i18n'
import { useI18n } from '../../shared/i18n/useI18n'
import { useMoney } from '../../shared/i18n/money'
import { formatDay, formatTime } from '../home/format'

// stările plății din backend (PaymentStatus) și cum le arătăm
function statusOf(status) {
  const labels = { PAID: tr('Plătită'), FAILED: tr('Refuzată'), PENDING: tr('În așteptare'), REFUNDED: tr('Rambursată') }
  const style = status === 'PAID' ? { background: '#dcfce7', color: '#166534', borderColor: '#bbf7d0' } : {}
  return { label: labels[status] ?? status, style }
}

// Plățile online (simulate) făcute după confirmarea lobby-ului. Plata la fața locului nu apare aici.
export default function PaymentsSection() {
  const { t } = useI18n()
  const { moneyBoth } = useMoney()
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
  if (!payments) return <div className="mu">{t('Se încarcă...')}</div>

  return (
    <div className="card">
      <b>{t('Istoric plăți')}</b>
      {payments.length === 0 && <div className="mu" style={{ marginTop: 6 }}>{t('Nu ai nicio plată încă.')}</div>}
      {payments.map((p) => {
        const status = statusOf(p.status)
        return (
          <div className="pl" key={p.paymentId}>
            <div style={{ minWidth: 0 }}>
              <Link to={`/lobbies/${p.lobbyId}`} style={{ color: 'inherit' }}>
                <b>{t(p.sportName)} · {trField(p.fieldName)}</b>
              </Link>
              <div className="mu">{t('Meciul din {day}, ora {time}', { day: formatDay(p.dateTime), time: formatTime(p.dateTime) })}</div>
            </div>
            <span className="r" style={{ flexShrink: 0 }}>
              <b>{moneyBoth(p.amount)}</b>
              <span className="tag" style={status.style}>{status.label}</span>
            </span>
          </div>
        )
      })}
    </div>
  )
}
