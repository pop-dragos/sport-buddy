import { http } from './http'

// data: { lobbyId, cardNumber, expiry, cvc }; cardul nu se salvează pe server
export function payOnline(data) {
    return http('/payments', { method: 'POST', body: JSON.stringify(data) })
}

export function getPaymentHistory() {
    return http('/payments/history')
}
