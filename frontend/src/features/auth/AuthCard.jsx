import LanguageSwitch from '../../shared/LanguageSwitch'

// Cadrul comun al paginilor de autentificare: fundalul, cardul din mijloc, sigla și titlul.
// onSubmit: cardul devine formular (Enter trimite).
export default function AuthCard({ title, subtitle, onSubmit, children }) {
  const Tag = onSubmit ? 'form' : 'div'
  return (
    <div className="login-page">
      <Tag className="lg" onSubmit={onSubmit} noValidate={onSubmit ? true : undefined}>
        <div className="lg-top"><LanguageSwitch /></div>
        <div style={{ textAlign: 'center' }}>
          <span className="mk">S</span>
          <h2 style={{ margin: '12px 0 0', fontSize: 22 }}>{title}</h2>
          {subtitle && <div className="mu" style={{ marginTop: 6 }}>{subtitle}</div>}
        </div>
        {children}
      </Tag>
    </div>
  )
}
