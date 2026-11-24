import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import LanguageSwitch from '../../shared/LanguageSwitch'
import { useI18n } from '../../shared/i18n/useI18n'
import { PRIVACY, TERMS, UPDATED } from './legalContent'
import * as en from './legalContentEn'
import './legal.css'

// Fiecare document e scris separat în fiecare limbă: ro în legalContent.js, en în legalContentEn.js.
const CONTENT = {
  ro: { terms: TERMS, privacy: PRIVACY, updated: UPDATED },
  en: { terms: en.TERMS, privacy: en.PRIVACY, updated: en.UPDATED }
}

// Pagini publice (merg și fără cont): /termeni și /confidentialitate.
export default function LegalPage({ page }) {
  const navigate = useNavigate()
  const { lang, t } = useI18n()
  const texts = CONTENT[lang]
  const content = texts[page]
  const other = page === 'terms' ? texts.privacy : texts.terms

  useEffect(() => {
    document.title = `${content.title} · SportBuddy`
    window.scrollTo(0, 0)
    return () => { document.title = 'SportBuddy' }
  }, [content])

  // înapoi de unde ai venit; deschisă direct dintr-un link, duce la pagina de prezentare
  function back() {
    if (window.history.state?.idx > 0) navigate(-1)
    else navigate('/bun-venit')
  }

  return (
    <div className="login-page">
      <article className="lg legal">
        <div className="legal-bar">
          <button type="button" className="al legal-back" onClick={back}>‹ {t('Înapoi')}</button>
          <LanguageSwitch />
        </div>
        <div className="legal-head">
          <h1>{content.title}</h1>
          <div className="mu">{t('Ultima actualizare: {date}', { date: texts.updated })}</div>
        </div>
        <p>{content.intro}</p>

        {content.sections.map((s) => (
          <section key={s.title}>
            <h2>{s.title}</h2>
            {s.body.map((part, i) => (Array.isArray(part)
              ? <ul key={i}>{part.map((item) => <li key={item}>{item}</li>)}</ul>
              : <p key={i}>{part}</p>))}
          </section>
        ))}

        <div className="legal-links">
          <Link to={page === 'terms' ? '/confidentialitate' : '/termeni'}>{other.title}</Link>
          <Link to="/login">{t('Conectare')}</Link>
        </div>
      </article>
    </div>
  )
}
