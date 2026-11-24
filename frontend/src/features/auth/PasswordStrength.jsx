import { useT } from '../../shared/i18n/useI18n'
import { passwordChecks, passwordStrength, strengthLabel } from './passwordRules'

// Bara de sub parola nouă: se umple pe măsură ce parola devine mai puternică,
// iar sub ea se bifează regulile obligatorii.
export default function PasswordStrength({ password }) {
  const t = useT()
  const score = passwordStrength(password)

  return (
    <div className="pwm" data-score={score}>
      <div className="pwm-bar" role="meter" aria-label={t('Puterea parolei')} aria-valuemin={0} aria-valuemax={4}
        aria-valuenow={score} aria-valuetext={strengthLabel(score) || t('nicio parolă')}>
        {[1, 2, 3, 4].map((i) => <i key={i} className={i <= score ? 'on' : ''} />)}
      </div>
      <div className="pwm-row">
        <ul className="pwm-rules">
          {passwordChecks(password).map((c) => (
            <li key={c.label} className={c.ok ? 'ok' : ''}>
              <svg viewBox="0 0 16 16" aria-hidden="true">
                {c.ok
                  ? <path d="M3.5 8.5l3 3 6-7" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                  : <circle cx="8" cy="8" r="2.5" fill="currentColor" />}
              </svg>
              {c.label}
            </li>
          ))}
        </ul>
        {score > 0 && <span className="pwm-label">{strengthLabel(score)}</span>}
      </div>
    </div>
  )
}
