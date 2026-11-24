import { getSettings, updateSettings } from '../api/users'
import { useAuth } from '../features/auth/useAuth'
import { markLanguageChoice } from './i18n'
import { useI18n } from './i18n/useI18n'

// Comutator mic RO | EN pentru paginile de dinainte de conectare (login, prezentare, termeni).
// Dacă ești deja conectat, alegerea se salvează și în cont; altfel rămâne în browser și, la conectare,
// se salvează în contul în care intri (vezi loadSettings din AuthProvider).
export default function LanguageSwitch() {
  const { lang, setLang, t } = useI18n()
  const { status } = useAuth()

  function choose(next) {
    if (next === lang) return
    setLang(next)
    if (status === 'authed') {
      getSettings()
        .then((s) => updateSettings({ ...s, language: next.toUpperCase() }))
        .catch(() => {})
    } else {
      markLanguageChoice()
    }
  }

  return (
    <div className="langsw" role="group" aria-label={t('Limba')}>
      {['ro', 'en'].map((code) => (
        <button key={code} type="button" className={lang === code ? 'on' : ''} aria-pressed={lang === code}
          onClick={() => choose(code)}>
          {code.toUpperCase()}
        </button>
      ))}
    </div>
  )
}
