export default function Toggle({ title, description, on, onChange }) {
  return (
    <div className="pl">
      <div>
        {title}
        {description && <div className="mu">{description}</div>}
      </div>
      <button
        type="button"
        role="switch"
        aria-checked={on}
        aria-label={title}
        className={`sw ${on ? 'on' : ''}`}
        onClick={() => onChange(!on)}
      />
    </div>
  )
}
