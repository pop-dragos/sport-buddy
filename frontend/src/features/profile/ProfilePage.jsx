import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { errorMessage } from '../../api/http'
import { getFriends } from '../../api/friends'
import { getUserLobbies, getUserStats } from '../../api/lobbies'
import { getUserRating } from '../../api/ratings'
import { getProfile } from '../../api/users'
import Avatar from '../../shared/Avatar'
import { useAuth } from '../auth/useAuth'
import FriendButton from '../friends/FriendButton'
import { formatDay, formatTime } from '../home/format'
import { levelLabel } from './levels'
import RatingLine from './RatingLine'
import { useCatalog } from './useCatalog'

const RECENT_MATCHES = 3

function memberSince(date) {
  return new Date(date).toLocaleDateString('ro-RO', { month: 'long', year: 'numeric' })
}

function ProfileView({ id }) {
  const { user: me } = useAuth()
  const { sports, cities } = useCatalog()
  const isMe = String(me.id) === id

  const [other, setOther] = useState(null)
  const [rating, setRating] = useState(null)
  const [friendCount, setFriendCount] = useState(null)
  // { matchesPlayed, matchesTogether } și lista de jos: pe profilul meu meciurile mele, pe al altcuiva cele împreună
  const [stats, setStats] = useState(null)
  const [matches, setMatches] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    const ignore = () => {}
    getUserRating(id).then((r) => { if (!cancelled) setRating(r) }).catch(ignore)
    getUserStats(id).then((s) => { if (!cancelled) setStats(s) }).catch(ignore)
    getUserLobbies(id, RECENT_MATCHES).then((l) => { if (!cancelled) setMatches(l) }).catch(ignore)
    if (isMe) {
      getFriends().then((f) => { if (!cancelled) setFriendCount(f.length) }).catch(ignore)
    } else {
      getProfile(id)
        .then((p) => { if (!cancelled) setOther(p) })
        .catch((e) => { if (!cancelled) setError(errorMessage(e)) })
    }
    return () => { cancelled = true }
  }, [id, isMe])

  if (error) return <div className="card mu">{error}</div>

  const profile = isMe ? { ...me, memberSince: me.createdAt } : other
  if (!profile) return null

  // profil vizibil doar prietenilor: serverul trimite doar numele și poza; ratingul rămâne vizibil
  if (profile.restricted) {
    return (
      <div style={{ maxWidth: 960, margin: '0 auto' }}>
        <div className="card">
          <div className="r" style={{ gap: 16, flexWrap: 'nowrap' }}>
            <Avatar name={profile.fullName} url={profile.avatarUrl} size={64} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <b style={{ fontSize: 18 }}>{profile.fullName}</b>
              <RatingLine rating={rating} />
              <div className="mu" style={{ marginTop: 4 }}>Profilul este vizibil doar prietenilor.</div>
            </div>
          </div>
          <FriendButton profile={profile} />
        </div>
      </div>
    )
  }

  const cityName = cities.find((c) => c.id === profile.cityId)?.name
  const sportName = (sportId) => sports.find((s) => s.id === sportId)?.name ?? `Sport ${sportId}`
  const tags = rating?.tags ?? []

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
            <RatingLine rating={rating} />
          </div>
        </div>

        <p style={{ margin: '12px 0 0' }}>
          {profile.description || (isMe ? <span className="mu">Nu ai adăugat o descriere.</span> : null)}
        </p>

        {isMe ? (
          <div className="r" style={{ marginTop: 12 }}>
            <Link to="/settings" className="b o" style={{ textDecoration: 'none' }}>Editează profilul</Link>
          </div>
        ) : (
          <FriendButton profile={profile} />
        )}
      </div>

      <div className="g" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
        {[
          [stats?.matchesPlayed ?? '–', 'Meciuri'],
          [rating?.average != null ? rating.average.toFixed(1) : '–', 'Rating'],
          isMe ? [friendCount ?? '–', 'Prieteni'] : [stats?.matchesTogether ?? '–', 'Meciuri împreună']
        ].map(([value, label]) => (
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
        {tags.length === 0 ? (
          <div className="mu" style={{ marginTop: 8 }}>Apar după primele review-uri.</div>
        ) : (
          <div className="r" style={{ marginTop: 8, flexWrap: 'wrap' }}>
            {tags.map((t) => <span className="tag" key={t}>{t}</span>)}
          </div>
        )}
      </div>

      <b style={{ display: 'block', margin: '4px 0 8px' }}>{isMe ? 'Meciurile mele' : 'Meciuri împreună'}</b>
      {(matches ?? []).map((l) => (
        <div className="card" key={l.id}>
          <div className="top">
            <div>
              <b>{l.sportName} · {l.fieldName}</b>
              <div className="mu">{formatDay(l.dateTime)} · {formatTime(l.dateTime)} · {l.cityName}</div>
            </div>
            <Link to={`/lobbies/${l.id}`} className="b o" style={{ textDecoration: 'none' }}>Deschide</Link>
          </div>
        </div>
      ))}
      {(matches ?? []).length === 0 && <div className="card mu">Nimic de arătat.</div>}
    </div>
  )
}

export default function ProfilePage() {
  const { id } = useParams()
  // key: la schimbarea profilului din URL, componenta pornește de la zero
  return <ProfileView key={id} id={id} />
}
