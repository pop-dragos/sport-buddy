import { Link } from 'react-router-dom'
import Avatar from '../../shared/Avatar'
import { formatRating } from './format'

export default function TopPlayersCard({ players }) {
    return (
        <div className="card">
            <div className="mu home-title">Top jucători</div>
            {players.length === 0 && <div className="mu">Apar după primele review-uri.</div>}
            {players.map((p, i) => (
                <div key={p.userId} className="pl">
                    <Link to={`/profile/${p.userId}`} className="home-player">
                        <span>{i + 1}.</span>
                        <Avatar name={p.username} url={p.avatarUrl} size={24} />
                        <span>{p.username}</span>
                    </Link>
                    <b>{formatRating(p.average)}</b>
                </div>
            ))}
        </div>
    )
}
