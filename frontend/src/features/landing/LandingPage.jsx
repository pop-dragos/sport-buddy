import { Link } from 'react-router-dom'
import LanguageSwitch from '../../shared/LanguageSwitch'
import { useT } from '../../shared/i18n/useI18n'
import HeroText from './HeroText'
import Highlights from './Highlights'
import RevealText from './RevealText'
import ScrollStory from './ScrollStory'
import StaticStory from './StaticStory'
import { showStory } from './storyNav'
import { useStoryMode } from './useStoryMode'
// clasele refolosite din aplicație (calendarul, pagina lobby-ului, review-ul, prima pagină, feed-ul)
import '../catalog/catalog.css'
import '../feed/feed.css'
import '../home/home.css'
import '../lobbies/lobbies.css'
import '../ratings/ratings.css'
import './landing.css'

// Pagina publică de prezentare (/bun-venit), în stilul paginilor de produs Apple:
// deschidere mare, povestea în laptop legată de scroll, o frază care se aprinde, „Pe scurt" și finalul.
// Merge fără cont și fără backend (date din demoData.js).
export default function LandingPage() {
  const t = useT()
  const { mode, reduced } = useStoryMode()
  const animate = !reduced
  const goToStory = () => showStory(animate)

  return (
    <div className="landing">
      <nav className="landing-nav" aria-label="SportBuddy">
        <Link to="/bun-venit" className="landing-logo"><b>S</b>portBuddy</Link>
        <div className="landing-nav-links">
          <button type="button" onClick={goToStory}>{t('Cum funcționează')}</button>
          <a href="#pe-scurt">{t('Pe scurt')}</a>
        </div>
        <div className="landing-nav-actions">
          <LanguageSwitch />
          <Link to="/login" className="landing-nav-login">{t('Conectează-te')}</Link>
          <Link to="/login" state={{ mode: 'up' }} className="pill small">{t('Creează cont')}</Link>
        </div>
      </nav>

      <div role="main">
        {mode === 'scroll' ? (
          <ScrollStory />
        ) : (
          <>
            <section className="hero-static">
              <HeroText onShowStory={goToStory} />
            </section>
            <StaticStory animate={animate} />
          </>
        )}

        <RevealText animate={mode === 'scroll'} />

        <Highlights animate={animate} />

        <section className="landing-cta" aria-labelledby="cta-titlu">
          <h2 id="cta-titlu">{t('Următorul meci te așteaptă.')}</h2>
          <p>{t('Contul e gratuit. Alegi orașul și sportul, iar restul îl vezi direct în calendar.')}</p>
          <div className="hero-actions">
            <Link to="/login" state={{ mode: 'up' }} className="pill">{t('Creează cont')}</Link>
            <Link to="/login" className="text-link">{t('Am deja cont')} <span aria-hidden="true">›</span></Link>
          </div>
        </section>
      </div>

      <footer className="landing-footer">
        <span>{t('SportBuddy · proiect la Ingineria Software, UTCN')}</span>
        <span className="landing-footer-links">
          <Link to="/login">{t('Conectează-te')}</Link>
          <Link to="/login" state={{ mode: 'up' }}>{t('Creează cont')}</Link>
          <Link to="/termeni">{t('Termeni')}</Link>
          <Link to="/confidentialitate">{t('Confidențialitate')}</Link>
        </span>
      </footer>
    </div>
  )
}
