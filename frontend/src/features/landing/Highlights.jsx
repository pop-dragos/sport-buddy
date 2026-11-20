import { motion } from 'motion/react'
import SportIcon from '../../shared/SportIcon'
import { HIGHLIGHTS, SPORTS } from './demoData'

function Tile({ item, index, animate }) {
  const content = (
    <>
      <p className="tile-big" style={{ color: item.accent }}>{item.big}</p>
      <p className="tile-text">{item.text}</p>
      {item.id === 'sports' && (
        <div className="tile-icons" aria-hidden="true">
          {SPORTS.map((s) => <SportIcon key={s.code} type={s.code} />)}
        </div>
      )}
    </>
  )
  const className = `tile${item.wide ? ' wide' : ''}${item.full ? ' full' : ''}`

  if (!animate) return <div className={className}>{content}</div>
  return (
    <motion.div
      className={className}
      initial={{ opacity: 0, y: 24 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.3 }}
      transition={{ duration: 0.6, ease: 'easeOut', delay: (index % 3) * 0.08 }}
    >
      {content}
    </motion.div>
  )
}

// „Pe scurt": cardurile cu ce face aplicația, ca grila de pe paginile de produs Apple.
export default function Highlights({ animate }) {
  return (
    <section className="highlights" id="pe-scurt" aria-labelledby="pe-scurt-titlu">
      <h2 id="pe-scurt-titlu" className="section-title">Pe scurt.</h2>
      <div className="tiles">
        {HIGHLIGHTS.map((h, i) => <Tile key={h.id} item={h} index={i} animate={animate} />)}
      </div>
    </section>
  )
}
