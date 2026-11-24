import { Link } from 'react-router-dom'
import Avatar from '../../shared/Avatar'
import { useI18n } from '../../shared/i18n/useI18n'
import './friends.css'

// Un rând din listele de pe pagina Prieteni: cine e, de unde, ce joacă; acțiunile vin din `children`.
export default function FriendRow({ friend, sportNames, children }) {
  const { t, tn } = useI18n()
  const details = [
    friend.cityName,
    friend.sportCodes.map((code) => sportNames[code] ?? code).join(', '),
    friend.matchesTogether != null &&
      tn(friend.matchesTogether, '{n} meci împreună', '{n} meciuri împreună')
  ].filter(Boolean)

  return (
    <div className="pl">
      <Link to={`/profile/${friend.userId}`} className="r friend-row">
        <Avatar name={friend.fullName} url={friend.avatarUrl} />
        <div style={{ minWidth: 0 }}>
          <b>{friend.username}</b>
          {details.length > 0 && <div className="mu">{details.join(' · ')}</div>}
        </div>
      </Link>
      <span className="r friend-actions">
        <span className="friend-rating">{friend.rating != null ? `★ ${friend.rating.toFixed(1)}` : <span className="mu">{t('nou')}</span>}</span>
        {children}
      </span>
    </div>
  )
}
