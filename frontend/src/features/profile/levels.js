import { tr } from '../../shared/i18n'

// Valorile vin din server (SportLevel); etichetele se traduc la afișare.
export const LEVELS = [
  { value: 'BEGINNER' },
  { value: 'INTERMEDIATE' },
  { value: 'ADVANCED' }
]

export function levelLabel(value) {
  const labels = { BEGINNER: tr('Începător'), INTERMEDIATE: tr('Mediu'), ADVANCED: tr('Avansat') }
  return labels[value] ?? value
}
