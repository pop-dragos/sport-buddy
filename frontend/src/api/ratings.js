import { http } from './http'

// colegii pe care încă nu i-am notat la un meci încheiat
export function getPendingReviews(lobbyId) {
    return http(`/lobbies/${lobbyId}/reviews/pending`)
}

// { userId, average, reviewCount, reliabilityPercent, tags }; average și reliabilityPercent goale la utilizatorii noi
export function getUserRating(userId) {
    return http(`/ratings/users/${userId}`)
}

// reviews: [ { userId, score } ], toate deodată
export function submitReviews(lobbyId, reviews) {
    return http(`/lobbies/${lobbyId}/reviews`, { method: 'POST', body: JSON.stringify({ reviews }) })
}
