import Avatar from '../../shared/Avatar'
import { ME } from './demoData'

// Prima pagină, cum o vede Ana înainte să aleagă un sport: aceleași carduri și texte ca în features/home și feed.
export default function DemoHome() {
  return (
    <div className="home-cols">
      <div>
        <h3 className="home-greeting">Bună, {ME.fullName.split(' ')[0]}</h3>
        <div className="card">
          <div className="mu">Următorul meci</div>
          <div className="home-empty">
            Niciun meci programat. Alege un sport din stânga sau creează un lobby cu „+".
          </div>
        </div>
      </div>

      <div>
        <h3 className="feed-title">Comunitate</h3>
        <div className="card">
          <div className="top">
            <span className="r">
              <span className="r feed-author"><Avatar name="vlad" />vlad</span>
              <span className="feed-rating">★ 4.2</span>
            </span>
            <span className="tag feed-LOOKING_FOR_PLAYERS">Fotbal · Căutăm jucători</span>
          </div>
          <p className="feed-text">Mai căutăm jucători pentru sâmbătă seara, pe Teren de fotbal – Centru. Nivel mediu.</p>
          <div className="r">
            <span className="b o">♡ 3</span>
            <span className="mu">2 comentarii</span>
          </div>
        </div>
      </div>
    </div>
  )
}
