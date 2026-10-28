import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../../api/http'
import { getProfile } from '../../api/users'
import Avatar from '../../shared/Avatar'
import { useAuth } from '../auth/useAuth'
import { levelLabel } from './levels'
import { useCatalog } from './useCatalog'

function memberSince(date) {
  return new Date(date).toLocaleDateString('ro-RO', { month: 'long', year: 'numeric' })
}

function ProfileView({ id }) {
  const { user: me } = useAuth()
  const { sports, cities } = useCatalog()
  const isMe = String(me.id) === id

  const [other, setOther] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (isMe) return
    let cancelled = false
    getProfile(id)
      .then((p) => { if (!cancelled) setOther(p) })
      .catch((e) => { if (!cancelled) setError(errorMessage(e)) })
    return () => { cancelled = true }
  }, [id, isMe])

  if (error) return <div className="card mu">{error}</div>

  const profile = isMe ? { ...me, memberSince: me.createdAt } : other
  if (!profile) return null

  const cityName = cities.find((c) => c.id === profile.cityId)?.name
  const sportName = (sportId) => sports.find((s) => s.id === sportId)?.name ?? `Sport ${sportId}`

  return (
    <div style={{ maxWidth: 960, margin: '0 auto' }}>
      <div className="card">
        <div className="r" style={{ gap: 16, flexWrap: 'nowrap' }}>
          <Avatar name={profile.fullName} url={profile.avatarUrl} size={64} />
          <div style={{ flex: 1, minWidth: 0 }}>
            <b style={{ fontSize: 18 }}>{profile.fullName}</b>
            <div className="mu">
              {cityName ? `${cityName} · ` : ''}membru din {memberSince(profile.memberSince)}
            </div>
            <div className="mu" style={{ marginTop: 4 }}>Fără review-uri</div>
          </div>
        </div>

        <p style={{ margin: '12px 0 0' }}>
          {profile.description || (isMe ? <span className="mu">Nu ai adăugat o descriere.</span> : null)}
        </p>

        {isMe && (
          <div className="r" style={{ marginTop: 12 }}>
            <Link to="/settings" className="b o" style={{ textDecoration: 'none' }}>Editează profilul</Link>
          </div>
        )}
      </div>

      <div className="g" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
        {[['–', 'Meciuri'], ['–', 'Rating'], ['–', isMe ? 'Prieteni' : 'Meciuri împreună']].map(([value, label]) => (
          <div className="card" key={label} style={{ textAlign: 'center' }}>
            <b style={{ fontSize: 20 }}>{value}</b>
            <div className="mu">{label}</div>
          </div>
        ))}
      </div>

      <div className="card">
        <b>Sporturi</b>
        {profile.sports.length === 0 && (
          <div className="mu">Niciun sport adăugat. Le poți alege din setări.</div>
        )}
        {profile.sports.map((s) => (
          <div className="pl" key={s.sportId}>
            <span>{sportName(s.sportId)}</span>
            <span className="tag">{levelLabel(s.level)}</span>
          </div>
        ))}
      </div>

      <div className="card">
        <b>Etichete de la colegi</b>
        <div className="mu" style={{ marginTop: 8 }}>Apar după primele review-uri.</div>
      </div>

      <b style={{ display: 'block', margin: '4px 0 8px' }}>{isMe ? 'Meciurile mele' : 'Meciuri împreună'}</b>
      <div className="card mu">Nimic de arătat.</div>
    </div>
  )
}

export default function ProfilePage() {
  const { id } = useParams()
  // key: la schimbarea profilului din URL, componenta pornește de la zero
  return <ProfileView key={id} id={id} />
}
