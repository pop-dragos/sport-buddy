import { motion, useScroll, useTransform } from 'motion/react'
import { useRef } from 'react'
import { tx } from '../../shared/i18n'
import { useT } from '../../shared/i18n/useI18n'

const TEXT = tx('Fără grupuri de mesaje, fără calcule pe hârtie, fără surprize la teren. Vezi cine joacă, cât costă și când se confirmă meciul.')

// Un cuvânt se „aprinde" (din gri în culoarea textului) când scroll-ul trece de el.
function Word({ word, index, total, progress }) {
  const from = 0.1 + (index / total) * 0.75
  const opacity = useTransform(progress, [from, from + 0.75 / total], [0.18, 1])
  return <motion.span style={{ opacity }}>{word} </motion.span>
}

function RevealOnScroll() {
  const t = useT()
  const ref = useRef(null)
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start start', 'end end'] })
  const words = t(TEXT).split(' ')

  return (
    <section ref={ref} className="reveal">
      <div className="reveal-sticky">
        <p className="reveal-text">
          {words.map((w, i) => (
            <Word key={i} word={w} index={i} total={words.length} progress={scrollYProgress} />
          ))}
        </p>
      </div>
    </section>
  )
}

// Fraza mare dintre poveste și „Pe scurt". Pe ecrane mici și cu reduce motion apare direct, întreagă.
export default function RevealText({ animate }) {
  const t = useT()
  if (!animate) {
    return (
      <section className="reveal still">
        <p className="reveal-text">{t(TEXT)}</p>
      </section>
    )
  }
  return <RevealOnScroll />
}
