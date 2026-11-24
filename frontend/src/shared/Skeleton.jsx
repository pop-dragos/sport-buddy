import { useT } from './i18n/useI18n'

// Card gri care pulsează discret în locul conținutului, cât se încarcă (în loc de „Se încarcă...").
export default function Skeleton({ lines = 3 }) {
  const t = useT()
  return (
    <div className="card" aria-busy="true" aria-label={t('Se încarcă')}>
      <div className="sk sk-title" />
      {Array.from({ length: lines }, (_, i) => <div key={i} className="sk" />)}
    </div>
  )
}
