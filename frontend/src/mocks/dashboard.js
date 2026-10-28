// Date false până apare GET /api/dashboard/home (modulul dashboard).
// Forma este cea din docs/api.md; datele sunt calculate față de azi, ca să nu se învechească.

function inDays(days, hour, minute = 0) {
    const d = new Date()
    d.setDate(d.getDate() + days)
    d.setHours(hour, minute, 0, 0)
    // forma din api.md, fără fus orar: 2026-10-24T18:00:00
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(hour)}:${pad(minute)}:00`
}

function lobby(id, sportId, sportCode, sportName, fieldName, cityId, dateTime, maxPlayers, currentPlayers, averageRating) {
    return {
        id, sportId, sportCode, sportName, fieldId: id * 10, fieldName, cityId, cityName: '',
        dateTime, status: 'OPEN', maxPlayers, currentPlayers, freeSlots: maxPlayers - currentPlayers,
        averageRating, myState: 'NONE'
    }
}

export function mockDashboard(cityId) {
    return {
        nextMatch: { ...lobby(101, 1, 'football', 'Fotbal', 'Teren de fotbal – Centru', cityId, inDays(2, 19), 10, 7, 3.8), myState: 'ACCEPTED' },
        todo: { pendingRequests: 2, attendanceToSave: 0, reviewsToGive: 1, paymentsToMake: 0, total: 3 },
        recommended: [
            lobby(102, 2, 'basketball', 'Baschet', 'Sala de baschet – Nord', cityId, inDays(1, 18, 30), 10, 6, 3.6),
            lobby(103, 3, 'tennis', 'Tenis', 'Teren de tenis – Parc', cityId, inDays(3, 17), 4, 2, 4.1),
            lobby(104, 5, 'volleyball', 'Volei', 'Sala de volei – Centru', cityId, inDays(4, 20), 12, 9, null)
        ],
        topPlayers: [
            { userId: 3, username: 'ioana', avatarUrl: null, average: 4.8, reviewCount: 9 },
            { userId: 5, username: 'radu', avatarUrl: null, average: 4.6, reviewCount: 14 },
            { userId: 2, username: 'andrei', avatarUrl: null, average: 4.5, reviewCount: 7 }
        ]
    }
}
