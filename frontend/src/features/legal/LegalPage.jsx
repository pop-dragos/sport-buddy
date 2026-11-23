import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { PRIVACY, TERMS, UPDATED } from './legalContent'
import './legal.css'

const PAGES = { terms: TERMS, privacy: PRIVACY }

// Pagini publice (merg și fără cont): /termeni și /confidentialitate.
export default function LegalPage({ page }) {
  const navigate = useNavigate()
  const content = PAGES[page]

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
        <button type="button" className="al legal-back" onClick={back}>‹ Înapoi</button>
        <div className="legal-head">
          <h1>{content.title}</h1>
          <div className="mu">Ultima actualizare: {UPDATED}</div>
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
          <Link to={page === 'terms' ? '/confidentialitate' : '/termeni'}>
            {page === 'terms' ? PRIVACY.title : TERMS.title}
          </Link>
          <Link to="/login">Conectare</Link>
        </div>
      </article>
    </div>
  )
}
