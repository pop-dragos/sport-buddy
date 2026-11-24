import SportIcon from '../shared/SportIcon'
import useSwipeDown from '../shared/useSwipeDown'
import { useT } from '../shared/i18n/useI18n'

export default function SportsSheet({ sports, sportsError, onClose, onSelect }) {
  const { handleProps, sheetStyle } = useSwipeDown(onClose)
  const t = useT()

  return (
    <div className="sports-sheet-backdrop" onClick={onClose}>
      <section
        className="sports-sheet"
        role="dialog"
        aria-modal="true"
        aria-labelledby="sports-sheet-title"
        style={sheetStyle}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="mb-grab" aria-hidden="true" {...handleProps}><span /></div>
        <div className="sports-sheet-heading">
          <h2 id="sports-sheet-title">{t('Sporturi')}</h2>
          <button type="button" className="sports-sheet-close" aria-label={t('Închide lista sporturilor')} onClick={onClose}>
            <span aria-hidden="true">×</span>
          </button>
        </div>
        {sportsError && <div className="mu">{t('Sporturile nu sunt disponibile.')}</div>}
        <div className="sports-sheet-list">
          {sports.map((sport) => (
            <button
              key={sport.id}
              type="button"
              className="sports-sheet-item"
              aria-label={t(sport.name)}
              onClick={() => onSelect(sport)}
            >
              <SportIcon type={sport.code} />
              <span>{t(sport.name)}</span>
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}
