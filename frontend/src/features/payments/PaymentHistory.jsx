import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getPaymentHistory } from '../../api/payments'
import { trField } from '../../shared/i18n'
import { useT } from '../../shared/i18n/useI18n'
import { useMoney } from '../../shared/i18n/money'
import { formatDay, formatTime } from '../home/format'
import './payments.css'

// Istoricul plăților online, pentru Setări > Plăți.
export default function PaymentHistory() {
  const t = useT()
  const { moneyBoth } = useMoney()
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
      <b>{t('Istoric plăți')}</b>
      {error && <div className="mu pay-hint">{t('Istoricul nu este disponibil acum.')}</div>}
      {payments?.length === 0 && (
        <div className="mu pay-hint">{t('Plățile tale apar aici după ce plătești online un lobby confirmat.')}</div>
      )}
      {payments?.map((p) => (
        <div key={p.paymentId} className="pl">
          <span>
            <Link className="pay-link" to={`/lobbies/${p.lobbyId}`}>{t(p.sportName)}, {trField(p.fieldName)}</Link>
            <div className="mu pay-small">{formatDay(p.dateTime)}, {formatTime(p.dateTime)}</div>
          </span>
          <span className="pay-amount">
            {moneyBoth(p.amount)}
            {p.status !== 'PAID' && <div className="mu pay-small">{t('refuzată')}</div>}
          </span>
        </div>
      ))}
    </div>
  )
}
