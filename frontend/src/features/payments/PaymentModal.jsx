import { useState } from 'react'
import { errorMessage } from '../../api/http'
import { payOnline } from '../../api/payments'
import Modal from '../../shared/Modal'
import { formatDay, formatTime } from '../home/format'
import './payments.css'

// Fereastra de plată din prototip. Plata e simulată: 4242… trece, 4000 0000 0000 0002 e refuzat.
export default function PaymentModal({ lobby, onClose, onPaid }) {
  const [card, setCard] = useState({ cardNumber: '4242 4242 4242 4242', expiry: '12/29', cvc: '123' })
  const [error, setError] = useState(null)
  const [paying, setPaying] = useState(false)

  function set(key, value) {
    setCard((c) => ({ ...c, [key]: value }))
  }

  async function submit(e) {
    e.preventDefault()
    setError(null)
    setPaying(true)
    try {
      await payOnline({ lobbyId: lobby.id, ...card })
      onPaid()
    } catch (err) {
      setError(errorMessage(err))
      setPaying(false)
    }
  }

  return (
    <Modal title="Plată online" onClose={onClose}>
      <div className="mu">
        {lobby.sport.name}, {formatDay(lobby.dateTime)}, {formatTime(lobby.dateTime)}
      </div>
      <div className="pl pay-total">
        <span>Preț final</span>
        <b>{lobby.finalPrice} RON</b>
      </div>

      <form onSubmit={submit}>
        <div className="f">
          <label>Număr card</label>
          <input value={card.cardNumber} onChange={(e) => set('cardNumber', e.target.value)} inputMode="numeric" required />
        </div>
        <div className="r pay-row">
          <div className="f">
            <label>Expiră</label>
            <input value={card.expiry} onChange={(e) => set('expiry', e.target.value)} placeholder="LL/AA" required />
          </div>
          <div className="f">
            <label>CVC</label>
            <input value={card.cvc} onChange={(e) => set('cvc', e.target.value)} inputMode="numeric" required />
          </div>
        </div>
        <div className="mu pay-hint">Card de test: 4242… trece, 4000 0000 0000 0002 e refuzat.</div>

        {error && <div className="err">{error}</div>}

        <div className="r pay-actions">
          <button className="b" type="submit" disabled={paying}>
            {paying ? 'Se procesează...' : `Plătește ${lobby.finalPrice} RON`}
          </button>
          <button className="b o" type="button" onClick={onClose}>Anulează</button>
        </div>
      </form>
    </Modal>
  )
}
