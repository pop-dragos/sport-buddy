import Avatar from '../../shared/Avatar'
import { useT } from '../../shared/i18n/useI18n'
import { ME } from './demoData'

// Prima pagină, cum o vede Ana înainte să aleagă un sport: aceleași carduri și texte ca în features/home și feed.
export default function DemoHome() {
  const t = useT()
  return (
    <div className="home-cols">
      <div>
        <h3 className="home-greeting">{t('Bună, {name}', { name: ME.fullName.split(' ')[0] })}</h3>
        <div className="card">
          <div className="mu">{t('Următorul meci')}</div>
          <div className="home-empty">
            {t('Niciun meci programat. Alege un sport din stânga sau creează un lobby cu „+".')}
          </div>
        </div>
      </div>

      <div>
        <h3 className="feed-title">{t('Comunitate')}</h3>
        <div className="card">
          <div className="top">
            <span className="r">
              <span className="r feed-author"><Avatar name="vlad" />vlad</span>
              <span className="feed-rating">★ 4.2</span>
            </span>
            <span className="tag feed-LOOKING_FOR_PLAYERS">{t('Fotbal')} · {t('Căutăm jucători')}</span>
          </div>
          <p className="feed-text">{t('Mai căutăm jucători pentru sâmbătă seara, pe Teren de fotbal – Centru. Nivel mediu.')}</p>
          <div className="r">
            <span className="b o">♡ 3</span>
            <span className="mu">{t('{n} comentarii', { n: 2 })}</span>
          </div>
        </div>
      </div>
    </div>
  )
}
