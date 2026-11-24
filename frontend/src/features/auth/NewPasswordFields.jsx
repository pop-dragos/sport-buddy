import { useT } from '../../shared/i18n/useI18n'
import PasswordInput from './PasswordInput'
import PasswordStrength from './PasswordStrength'

// Parola nouă de două ori (cont nou, resetare, schimbare din setări), cu bara de putere.
// „Nu coincid” apare abia după ce a început scrierea confirmării.
export default function NewPasswordFields({ password, confirm, onPassword, onConfirm, labels = {}, autoFocus }) {
  const t = useT()
  const mismatch = confirm.length > 0 && confirm !== password
  const matches = confirm.length > 0 && confirm === password

  return (
    <>
      <div>
        <PasswordInput
          value={password}
          onChange={onPassword}
          placeholder={labels.password ?? t('Parolă *')}
          autoComplete="new-password"
          autoFocus={autoFocus}
        />
        <PasswordStrength password={password} />
      </div>
      <div>
        <PasswordInput
          value={confirm}
          onChange={onConfirm}
          placeholder={labels.confirm ?? t('Confirmă parola *')}
          autoComplete="new-password"
          invalid={mismatch}
        />
        {mismatch && <div className="pwm-match bad">{t('Parolele nu coincid.')}</div>}
        {matches && <div className="pwm-match good">{t('Parolele coincid.')}</div>}
      </div>
    </>
  )
}
