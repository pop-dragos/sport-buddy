import { http } from './http'

export function getSports() {
    return http('/catalog/sports')
}

export function getCities(query = '') {
    return http(`/catalog/cities?query=${encodeURIComponent(query)}`)
}

export function getFields(cityId, sportId) {
    const sport = sportId ? `&sportId=${sportId}` : ''
    return http(`/catalog/fields?cityId=${cityId}${sport}`)
}
