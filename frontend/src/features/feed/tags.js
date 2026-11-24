import { tr } from '../../shared/i18n'

// Ordinea din selectul de postare nouă, ca în prototip
export const TAGS = [
  { value: 'DISCUSSION' },
  { value: 'LOOKING_FOR_PLAYERS' },
  { value: 'TOURNAMENT' }
]

export function tagLabel(value) {
  const labels = { DISCUSSION: tr('Discuție'), LOOKING_FOR_PLAYERS: tr('Căutăm jucători'), TOURNAMENT: tr('Turneu') }
  return labels[value] ?? value
}
