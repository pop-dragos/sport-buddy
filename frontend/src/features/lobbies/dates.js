// Datele se trimit la backend ca text, în ora locală: „2026-10-24" și „2026-10".
// Nu folosim toISOString(), pentru că acela trece în UTC și poate muta ziua cu una în urmă.

const pad = (n) => String(n).padStart(2, '0')

export function dayKey(date) {
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

export function monthKey(year, month) {
    return `${year}-${pad(month + 1)}`
}

// Câte celule goale sunt înainte de ziua 1, cu săptămâna începând luni
export function leadingBlanks(year, month) {
    return (new Date(year, month, 1).getDay() + 6) % 7
}

export function daysInMonth(year, month) {
    return new Date(year, month + 1, 0).getDate()
}
