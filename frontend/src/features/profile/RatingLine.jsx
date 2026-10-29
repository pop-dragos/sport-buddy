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
  if (!rating) return null
  if (rating.average == null) {
    return <div className="mu" style={{ marginTop: 4 }}>nou · fără review-uri</div>
  }

  const parts = [
    rating.average.toFixed(1),
    `${rating.reviewCount} ${rating.reviewCount === 1 ? 'review' : 'review-uri'}`,
    rating.reliabilityPercent != null && `fiabilitate ${rating.reliabilityPercent}%`
  ].filter(Boolean)

  return (
    <div style={{ marginTop: 4 }} aria-label={`Rating ${rating.average.toFixed(1)} din 5`}>
      <Stars average={rating.average} /> <span className="mu">{parts.join(' · ')}</span>
    </div>
  )
}
