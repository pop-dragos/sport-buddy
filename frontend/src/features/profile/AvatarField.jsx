import { useRef, useState } from 'react'
import { errorMessage } from '../../api/http'
import { deleteAvatar, uploadAvatar } from '../../api/users'
import Avatar from '../../shared/Avatar'
import { useAuth } from '../auth/useAuth'

const MAX_BYTES = 2 * 1024 * 1024
const ACCEPTED = ['image/png', 'image/jpeg', 'image/webp', 'image/gif']

// Poza se trimite imediat după alegere; serverul verifică din nou tipul și mărimea.
export default function AvatarField() {
  const { user, updateUser } = useAuth()
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
    if (!ACCEPTED.includes(file.type)) return setError('Sunt acceptate doar imagini PNG, JPG, WebP sau GIF.')
    if (file.size > MAX_BYTES) return setError('Imaginea poate avea maximum 2 MB.')
    run(() => uploadAvatar(file))
  }

  return (
    <div className="f">
      <label>Poză de profil</label>
      <div className="r">
        <Avatar name={user.fullName} url={user.avatarUrl} size={44} />
        <button type="button" className="b o" disabled={busy} onClick={() => input.current.click()}>
          {user.avatarUrl ? 'Schimbă poza' : 'Alege o poză'}
        </button>
        {user.avatarUrl && (
          <button type="button" className="b o" disabled={busy} onClick={() => run(deleteAvatar)}>
            Șterge poza
          </button>
        )}
        <input ref={input} type="file" accept={ACCEPTED.join(',')} className="hide" onChange={pick} />
      </div>
      {error && <div className="err">{error}</div>}
    </div>
  )
}
