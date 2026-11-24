import { useI18n } from '../../shared/i18n/useI18n'

// Stelele de sub nume, ca în prototip: steaua e plină dacă media o atinge (cu o toleranță de un sfert).
function Stars({ average }) {
  return (
    <span aria-hidden="true">
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} style={{ color: '#f59e0b', opacity: average >= n - 0.25 ? 1 : 0.25 }}>★</span>
      ))}
    </span>
  )
}

// Ratingul nu se poate ascunde (brief), deci apare pe orice profil, inclusiv pe cele private.
export default function RatingLine({ rating }) {
  const { t, tn } = useI18n()
  if (!rating) return null
  if (rating.average == null) {
    return <div className="mu" style={{ marginTop: 4 }}>{t('nou · fără review-uri')}</div>
  }

  const parts = [
    rating.average.toFixed(1),
    tn(rating.reviewCount, '{n} review', '{n} review-uri'),
    rating.reliabilityPercent != null && t('fiabilitate {n}%', { n: rating.reliabilityPercent })
  ].filter(Boolean)

  return (
    <div style={{ marginTop: 4 }} aria-label={t('Rating {n} din 5', { n: rating.average.toFixed(1) })}>
      <Stars average={rating.average} /> <span className="mu">{parts.join(' · ')}</span>
    </div>
  )
}
