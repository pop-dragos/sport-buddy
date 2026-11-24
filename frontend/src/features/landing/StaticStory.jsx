import { motion, useMotionValue } from 'motion/react'
import DemoScreen from './DemoScreen'
import Laptop from './Laptop'
import RichText from './RichText'
import { useT } from '../../shared/i18n/useI18n'
import { STAGES } from './demoData'

// O etapă: titlul, textul și laptopul în starea finală a etapei (aceleași componente, fără scroll legat).
function StaticStage({ stage, animate }) {
  const t = useT()
  const progress = useMotionValue(stage.staticProgress)
  const content = (
    <>
      <h2 className="story-title">{t(stage.title)}</h2>
      <RichText parts={stage.text} className="story-text" />
      <div className="story-frame fluid" aria-hidden="true" inert>
        <Laptop fluid>
          <DemoScreen phase={stage.staticPhase} progress={progress} fluid still />
        </Laptop>
      </div>
    </>
  )

  if (!animate) return <div className="story-step">{content}</div>
  return (
    <motion.div
      className="story-step"
      initial={{ opacity: 0, y: 24 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.15 }}
      transition={{ duration: 0.6, ease: 'easeOut' }}
    >
      {content}
    </motion.div>
  )
}

// Pe ecrane mici și cu „reduce motion": cele trei etape una sub alta, cu toată informația vizibilă.
export default function StaticStory({ animate }) {
  const t = useT()
  return (
    <section className="story-static" id="cum-functioneaza" aria-label={t('Cum funcționează SportBuddy')}>
      {STAGES.map((s) => <StaticStage key={s.title} stage={s} animate={animate} />)}
    </section>
  )
}
