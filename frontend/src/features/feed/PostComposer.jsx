import { useState } from 'react'
import { createPost } from '../../api/feed'
import { errorMessage } from '../../api/http'
import { TAGS } from './tags'

const MAX_LENGTH = 1000

// Cu filtrul pe un sport, postarea nouă aparține acelui sport; pe „Toate” se alege din listă.
export default function PostComposer({ sports, filterSportId, onCreated }) {
  const [content, setContent] = useState('')
  const [pickedSportId, setPickedSportId] = useState('')
  const [tag, setTag] = useState(TAGS[0].value)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const sportId = filterSportId ?? (Number(pickedSportId) || sports[0]?.id)
  const filterSport = sports.find((s) => s.id === filterSportId)

  async function submit() {
    if (!content.trim()) return setError('Scrie ceva în postare.')
    if (!sportId) return setError('Alege sportul.')
    setBusy(true)
    setError('')
    try {
      onCreated(await createPost(content.trim(), sportId, tag))
      setContent('')
    } catch (e) {
      setError(errorMessage(e))
    }
    setBusy(false)
  }

  return (
    <div className="card">
      <textarea
        rows={2}
        maxLength={MAX_LENGTH}
        placeholder="Scrie o postare"
        value={content}
        onChange={(e) => setContent(e.target.value)}
      />
      <div className="top" style={{ marginTop: 8 }}>
        <span className="r">
          {filterSportId ? (
            <span className="tag">{filterSport?.name}</span>
          ) : (
            <select style={{ width: 'auto' }} value={sportId ?? ''} onChange={(e) => setPickedSportId(e.target.value)}>
              {sports.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          )}
          <select style={{ width: 'auto' }} value={tag} onChange={(e) => setTag(e.target.value)}>
            {TAGS.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
          </select>
        </span>
        <button type="button" className="b" disabled={busy} onClick={submit}>Postează</button>
      </div>
      {error && <div className="err" style={{ marginTop: 8 }}>{error}</div>}
    </div>
  )
}
