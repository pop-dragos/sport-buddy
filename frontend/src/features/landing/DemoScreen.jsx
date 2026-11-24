import { motion, useTransform } from 'motion/react'
import AppWindow from './AppWindow'
import DemoCalendar from './DemoCalendar'
import DemoHome from './DemoHome'
import DemoLobby from './DemoLobby'
import { useT } from '../../shared/i18n/useI18n'
import { reached } from './demoData'

// Ce se vede în fereastră la un anumit pas: Acasă, apoi calendarul peste ea, apoi pagina lobby-ului.
// progress: progresul scroll-ului (sau o valoare fixă în varianta statică); still: fără animații de intrare.
export default function DemoScreen({ phase, progress, fluid = false, still = false }) {
  const t = useT()
  const onLobby = reached(phase, 'lobby')
  // „Fotbal" apăsat în bara laterală chiar înainte să se deschidă calendarul
  const press = useTransform(progress, [0.05, 0.08, 0.12, 0.16], [0, 1, 1, 0])
  const calendar = !onLobby && reached(phase, 'calendar') ? <DemoCalendar progress={progress} /> : null

  return (
    <AppWindow
      pageTitle={onLobby ? t('Lobby') : t('Acasă')}
      homeActive={!onLobby}
      pressFootball={fluid ? null : press}
      overlay={calendar}
      fluid={fluid}
      still={still}
    >
      {/* Fără animație de ieșire: conținutul se schimbă imediat la prag, ca fereastra să țină pasul
          și cu un scroll rapid înainte și înapoi (animațiile de ieșire s-ar pune la coadă). */}
      {onLobby ? (
        <motion.div
          key="lobby"
          initial={still ? false : { opacity: 0, x: 48 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.4, ease: 'easeOut' }}
        >
          <DemoLobby phase={phase} progress={progress} fluid={fluid} still={still} />
        </motion.div>
      ) : (
        <motion.div
          key="home"
          initial={still ? false : { opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.25 }}
        >
          <DemoHome />
        </motion.div>
      )}
    </AppWindow>
  )
}
