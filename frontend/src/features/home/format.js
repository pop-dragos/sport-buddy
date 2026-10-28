// dateTime vine din backend ca „2026-10-24T18:00:00", în ora României

export function formatDay(dateTime) {
    return new Date(dateTime).toLocaleDateString('ro-RO', { weekday: 'short', day: 'numeric', month: 'short' })
}

export function formatTime(dateTime) {
    return dateTime.slice(11, 16)
}

// „azi", „mâine" sau „în 3 zile", după zilele din calendar, nu după ore
export function daysUntil(dateTime) {
    const start = new Date(dateTime)
    const today = new Date()
    start.setHours(0, 0, 0, 0)
    today.setHours(0, 0, 0, 0)
    const days = Math.round((start - today) / 86400000)
    if (days <= 0) return 'azi'
    if (days === 1) return 'mâine'
    return `în ${days} zile`
}

export function formatRating(value) {
    return value == null ? 'nou' : value.toFixed(1)
}
