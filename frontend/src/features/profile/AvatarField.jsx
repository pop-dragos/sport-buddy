import { useRef, useState } from 'react'
import { errorMessage } from '../../api/http'
import { deleteAvatar, uploadAvatar } from '../../api/users'
import Avatar from '../../shared/Avatar'
import { useT } from '../../shared/i18n/useI18n'
import { useAuth } from '../auth/useAuth'
import { AVATAR_TYPES, avatarProblem } from './avatarRules'

// Poza se trimite imediat după alegere; serverul verifică din nou tipul și mărimea.
export default function AvatarField() {
  const { user, updateUser } = useAuth()
  const t = useT()
  const input = useRef(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function run(action) {
    setBusy(true)
    setError('')
    try {
      updateUser(await action())
    } catch (e) {
      setError(errorMessage(e))
    }
    setBusy(false)
  }

  function pick(e) {
    const file = e.target.files[0]
    e.target.value = ''
    if (!file) return
    const problem = avatarProblem(file)
    if (problem) return setError(problem)
    run(() => uploadAvatar(file))
  }

  return (
    <div className="f">
      <label>{t('Poză de profil')}</label>
      <div className="r">
        <Avatar name={user.fullName} url={user.avatarUrl} size={44} />
        <button type="button" className="b o" disabled={busy} onClick={() => input.current.click()}>
          {user.avatarUrl ? t('Schimbă poza') : t('Alege o poză')}
        </button>
        {user.avatarUrl && (
          <button type="button" className="b o" disabled={busy} onClick={() => run(deleteAvatar)}>
            {t('Șterge poza')}
          </button>
        )}
        <input ref={input} type="file" accept={AVATAR_TYPES.join(',')} className="hide" onChange={pick} />
      </div>
      {error && <div className="err">{error}</div>}
    </div>
  )
}
