import { http } from './http'

// colegii pe care încă nu i-am notat la un meci încheiat
export function getPendingReviews(lobbyId) {
    return http(`/lobbies/${lobbyId}/reviews/pending`)
}

// reviews: [ { userId, score } ], toate deodată
export function submitReviews(lobbyId, reviews) {
    return http(`/lobbies/${lobbyId}/reviews`, { method: 'POST', body: JSON.stringify({ reviews }) })
}
