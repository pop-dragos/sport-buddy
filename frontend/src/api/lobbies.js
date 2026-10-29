import { http } from './http'

// month are forma 2026-10
export function getCalendar(cityId, sportId, month) {
    return http(`/lobbies/calendar?cityId=${cityId}&sportId=${sportId}&month=${month}`)
}

// date are forma 2026-10-24
export function getLobbiesForDay(cityId, sportId, date) {
    return http(`/lobbies?cityId=${cityId}&sportId=${sportId}&date=${date}`)
}

export function getLobby(id) {
    return http(`/lobbies/${id}`)
}

export function createLobby(data) {
    return http('/lobbies', { method: 'POST', body: JSON.stringify(data) })
}
