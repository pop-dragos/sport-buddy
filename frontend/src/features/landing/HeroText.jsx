import { Link } from 'react-router-dom'
import { useT } from '../../shared/i18n/useI18n'

// Deschiderea paginii: titlul mare, o frază și cele două acțiuni.
export default function HeroText({ onShowStory }) {
  const t = useT()
  return (
    <div className="hero">
      <p className="hero-eyebrow">SportBuddy</p>
      <h1 className="hero-title">
        {t('Găsește-ți echipa.')}
        <br />
        <span className="hero-title-soft">{t('În orașul tău.')}</span>
      </h1>
      <p className="hero-sub">
        {t('Fotbal, baschet, tenis și încă nouă sporturi. Alegi ziua, intri în joc și plătești corect.')}
      </p>
      <div className="hero-actions">
        <Link to="/login" state={{ mode: 'up' }} className="pill">{t('Creează cont')}</Link>
        <button type="button" className="text-link" onClick={onShowStory}>
          {t('Vezi cum funcționează')} <span aria-hidden="true">›</span>
        </button>
      </div>
    </div>
  )
}
