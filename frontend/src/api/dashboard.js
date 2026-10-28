import { mockDashboard } from '../mocks/dashboard'

// TEMPORAR: date din mocks/ până apare backend-ul dashboard.
// Atunci funcția devine: return http(`/dashboard/home?cityId=${cityId}`)
export function getDashboard(cityId) {
    return Promise.resolve(mockDashboard(cityId))
}
