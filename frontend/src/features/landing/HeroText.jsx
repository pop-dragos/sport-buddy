import { Link } from 'react-router-dom'

// Deschiderea paginii: titlul mare, o frază și cele două acțiuni.
export default function HeroText({ onShowStory }) {
  return (
    <div className="hero">
      <p className="hero-eyebrow">SportBuddy</p>
      <h1 className="hero-title">
        Găsește-ți echipa.
        <br />
        <span className="hero-title-soft">În orașul tău.</span>
      </h1>
      <p className="hero-sub">
        Fotbal, baschet, tenis și încă nouă sporturi. Alegi ziua, intri în joc și plătești corect.
      </p>
      <div className="hero-actions">
        <Link to="/login" state={{ mode: 'up' }} className="pill">Creează cont</Link>
        <button type="button" className="text-link" onClick={onShowStory}>
          Vezi cum funcționează <span aria-hidden="true">›</span>
        </button>
      </div>
    </div>
  )
}
