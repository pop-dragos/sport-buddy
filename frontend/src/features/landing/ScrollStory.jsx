import { motion, useMotionValue, useMotionValueEvent, useScroll, useTransform } from 'motion/react'
import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import DemoScreen from './DemoScreen'
import HeroText from './HeroText'
import Laptop from './Laptop'
import RichText from './RichText'
import { INTRO, phaseAt, STAGES } from './demoData'
import { showStory } from './storyNav'

// Laptopul e desenat la o mărime fixă (fereastra de 1200×780, rama ecranului și baza mai lată)
// și micșorat o singură dată după ecran (și la redimensionare), niciodată în timpul scroll-ului.
const LAPTOP = { width: 1375, height: 826 }
// tot ce nu e laptop, pe verticală: bara de sus a paginii, marginile, spațiul dintre titlu și laptop, punctele
const NAV_HEIGHT = 52
const OTHER_HEIGHT = 92

// la fel ca .story-titles din landing.css
function titlesHeight() {
  return Math.min(170, Math.max(110, window.innerHeight * 0.18))
}

function fitScale() {
  const width = Math.min(window.innerWidth * 0.86, 1180)
  const height = window.innerHeight - NAV_HEIGHT - titlesHeight() - OTHER_HEIGHT
  return Math.max(0.35, Math.min(width / LAPTOP.width, height / LAPTOP.height, 1))
}

function useFitScale() {
  const [scale, setScale] = useState(fitScale)
  useEffect(() => {
    const onResize = () => setScale(fitScale())
    window.addEventListener('resize', onResize)
    return () => window.removeEventListener('resize', onResize)
  }, [])
  return scale
}

// progresul poveștii (0–1) mutat pe progresul întregii secțiuni, care începe cu deschiderea
const toRaw = (x) => INTRO + x * (1 - INTRO)

// Titlul și textul unei etape: apar și dispar la granițele ei, cu o suprapunere scurtă.
// Pragurile rămân între 0 și 1: motion leagă animația direct de scroll-ul browserului.
function StageTitle({ stage, index, progress }) {
  const [from, to] = stage.range
  const first = index === 0
  const last = index === STAGES.length - 1
  const start = first ? [INTRO - 0.03, INTRO] : [toRaw(from) - 0.02, toRaw(from) + 0.02]
  const end = last ? [1] : [toRaw(to) - 0.02, toRaw(to) + 0.02]
  const input = [...start, ...end]
  const shown = last ? [0, 1, 1] : [0, 1, 1, 0]
  const shift = last ? [16, 0, 0] : [16, 0, 0, -12]
  const opacity = useTransform(progress, input, shown)
  const y = useTransform(progress, input, shift)

  return (
    <motion.div className="story-stage" style={{ opacity, y }}>
      <h2 className="story-title">{stage.title}</h2>
      <RichText parts={stage.text} className="story-text" />
    </motion.div>
  )
}

function StageDot({ stage, index, progress }) {
  const [from, to] = stage.range
  const first = index === 0
  const last = index === STAGES.length - 1
  const input = [...(first ? [INTRO - 0.02, INTRO] : [toRaw(from) - 0.01, toRaw(from) + 0.01]),
    ...(last ? [1] : [toRaw(to) - 0.01, toRaw(to) + 0.01])]
  const output = last ? [0.25, 1, 1] : [0.25, 1, 1, 0.25]
  const opacity = useTransform(progress, input, output)
  return <motion.i style={{ opacity }} />
}

export default function ScrollStory() {
  const sectionRef = useRef(null)
  const heroRef = useRef(null)
  const titlesRef = useRef(null)
  const { scrollYProgress } = useScroll({ target: sectionRef, offset: ['start start', 'end end'] })
  // progresul poveștii propriu-zise, după deschidere
  const story = useTransform(scrollYProgress, [INTRO, 1], [0, 1])

  // pasul curent se schimbă doar la trecerea unui prag (React ignoră setările cu aceeași valoare)
  const [phase, setPhase] = useState(() => phaseAt(story.get()))
  useMotionValueEvent(story, 'change', (p) => setPhase(phaseAt(p)))
  const scale = useFitScale()

  // Deschiderea: titlul mare dispare, iar laptopul urcă și crește până la mărimea întreagă.
  // Cât coboară laptopul la început depinde de înălțimea titlului, deci o măsurăm după randare.
  const heroShift = useMotionValue(0)
  useLayoutEffect(() => {
    const measure = () => heroShift.set(Math.max(0, heroRef.current.offsetHeight + 32 - titlesRef.current.offsetHeight))
    measure()
    window.addEventListener('resize', measure)
    return () => window.removeEventListener('resize', measure)
  }, [heroShift])

  // titlul mare dispare înainte să apară titlul primei etape (din INTRO - 0.03), ca să nu se suprapună
  const heroOpacity = useTransform(scrollYProgress, [0, INTRO * 0.2, INTRO * 0.6], [1, 1, 0])
  const heroY = useTransform(scrollYProgress, [0, INTRO], [0, -48])
  const heroEvents = useTransform(heroOpacity, (o) => (o > 0.4 ? 'auto' : 'none'))
  const deviceScale = useTransform(scrollYProgress, [0, INTRO], [0.86, 1])
  const deviceY = useTransform([scrollYProgress, heroShift], ([p, shift]) => shift * Math.max(0, 1 - p / INTRO))
  const dotsOpacity = useTransform(scrollYProgress, [INTRO - 0.03, INTRO], [0, 1])

  return (
    <section ref={sectionRef} className="story" id="cum-functioneaza" aria-label="Cum funcționează SportBuddy">
      <div className="story-sticky">
        {/* exteriorul urmează scroll-ul; interiorul are doar intrarea de la încărcarea paginii */}
        <motion.div ref={heroRef} className="story-hero" style={{ opacity: heroOpacity, y: heroY, pointerEvents: heroEvents }}>
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, ease: 'easeOut' }}>
            <HeroText onShowStory={() => showStory()} />
          </motion.div>
        </motion.div>

        <div ref={titlesRef} className="story-titles">
          {STAGES.map((s, i) => <StageTitle key={s.title} stage={s} index={i} progress={scrollYProgress} />)}
        </div>

        <motion.div className="story-device" style={{ y: deviceY, scale: deviceScale }}>
          <div className="story-frame" style={{ width: LAPTOP.width * scale, height: LAPTOP.height * scale }} aria-hidden="true" inert>
            <div className="story-scaler" style={{ width: LAPTOP.width, height: LAPTOP.height, transform: `scale(${scale})` }}>
              <Laptop>
                <DemoScreen phase={phase} progress={story} />
              </Laptop>
            </div>
          </div>
        </motion.div>

        <motion.div className="story-dots" style={{ opacity: dotsOpacity }} aria-hidden="true">
          {STAGES.map((s, i) => <StageDot key={s.title} stage={s} index={i} progress={scrollYProgress} />)}
        </motion.div>
      </div>
    </section>
  )
}
