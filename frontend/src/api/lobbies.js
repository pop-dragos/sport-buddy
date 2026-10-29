import { http } from './http'

// month are forma 2026-10
export function getCalendar(cityId, sportId, month) {
    return http(`/lobbies/calendar?cityId=${cityId}&sportId=${sportId}&month=${month}`)
}

// date are forma 2026-10-24
export function getLobbiesForDay(cityId, sportId, date) {
    return http(`/lobbies?cityId=${cityId}&sportId=${sportId}&date=${date}`)
}

// filter: UPCOMING, CREATED, REQUESTS sau FINISHED
export function getMyLobbies(filter) {
    return http(`/lobbies/mine?filter=${filter}`)
}

export function getTodo() {
    return http('/lobbies/mine/todo')
}

export function getLobby(id) {
    return http(`/lobbies/${id}`)
}

export function createLobby(data) {
    return http('/lobbies', { method: 'POST', body: JSON.stringify(data) })
}

export function requestToJoin(id) {
    return http(`/lobbies/${id}/join-requests`, { method: 'POST' })
}

export function acceptRequest(id, requestId) {
    return http(`/lobbies/${id}/join-requests/${requestId}/accept`, { method: 'POST' })
}

export function rejectRequest(id, requestId) {
    return http(`/lobbies/${id}/join-requests/${requestId}/reject`, { method: 'POST' })
}

// ieșire din lobby sau retragerea cererii, dacă nu a fost încă acceptată
export function leaveLobby(id) {
    return http(`/lobbies/${id}/leave`, { method: 'POST' })
}

export function lockLobby(id) {
    return http(`/lobbies/${id}/lock`, { method: 'POST' })
}

export function cancelLobby(id) {
    return http(`/lobbies/${id}/cancel`, { method: 'POST' })
}

// attendance: [{ userId, present }], fără organizator
export function saveAttendance(id, attendance) {
    return http(`/lobbies/${id}/attendance`, { method: 'POST', body: JSON.stringify({ attendance }) })
}

// Simulare, doar pe profilul dev al backend-ului
export function simulatePassDeadline(id) {
    return http(`/dev/lobbies/${id}/pass-deadline`, { method: 'POST' })
}

export function simulateFinish(id) {
    return http(`/dev/lobbies/${id}/finish`, { method: 'POST' })
}
