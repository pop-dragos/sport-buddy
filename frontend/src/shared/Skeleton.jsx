// Card gri care pulsează discret în locul conținutului, cât se încarcă (în loc de „Se încarcă...").
export default function Skeleton({ lines = 3 }) {
  return (
    <div className="card" aria-busy="true" aria-label="Se încarcă">
      <div className="sk sk-title" />
      {Array.from({ length: lines }, (_, i) => <div key={i} className="sk" />)}
    </div>
  )
}
