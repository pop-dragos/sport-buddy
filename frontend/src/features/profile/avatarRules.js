import { tr } from '../../shared/i18n'

// Aceleași reguli ca pe server (UserService.updateAvatar), verificate înainte de trimitere.
export const AVATAR_TYPES = ['image/png', 'image/jpeg', 'image/webp', 'image/gif']
export const AVATAR_MAX_BYTES = 2 * 1024 * 1024

// textul erorii, sau '' dacă fișierul e în regulă
export function avatarProblem(file) {
  if (!AVATAR_TYPES.includes(file.type)) return tr('Sunt acceptate doar imagini PNG, JPG, WebP sau GIF.')
  if (file.size > AVATAR_MAX_BYTES) return tr('Imaginea poate avea maximum 2 MB.')
  return ''
}
