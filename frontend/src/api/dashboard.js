import { http } from './http'

// widget-urile de pe prima pagină: următorul meci, de făcut, recomandate, top jucători, completarea profilului
export function getDashboard(cityId) {
    return http(`/dashboard/home?cityId=${cityId}`)
}
