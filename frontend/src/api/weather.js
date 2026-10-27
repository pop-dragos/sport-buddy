import { http } from './http'

export function getWeather(cityId, days = 5) {
    return http(`/weather?cityId=${cityId}&days=${days}`)
}