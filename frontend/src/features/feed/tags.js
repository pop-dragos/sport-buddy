// Ordinea din selectul de postare nouă, ca în prototip
export const TAGS = [
  { value: 'DISCUSSION', label: 'Discuție' },
  { value: 'LOOKING_FOR_PLAYERS', label: 'Căutăm jucători' },
  { value: 'TOURNAMENT', label: 'Turneu' }
]

export function tagLabel(value) {
  return TAGS.find((t) => t.value === value)?.label ?? value
}
