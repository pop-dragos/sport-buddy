export const LEVELS = [
  { value: 'BEGINNER', label: 'Începător' },
  { value: 'INTERMEDIATE', label: 'Mediu' },
  { value: 'ADVANCED', label: 'Avansat' }
]

export function levelLabel(value) {
  return LEVELS.find((l) => l.value === value)?.label ?? value
}
