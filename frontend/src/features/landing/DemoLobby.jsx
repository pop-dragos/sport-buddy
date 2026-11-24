import { animate, motion, useMotionValue, useTransform } from 'motion/react'
import { useLayoutEffect, useRef } from 'react'
import Avatar from '../../shared/Avatar'
import { trField } from '../../shared/i18n'
import { useI18n } from '../../shared/i18n/useI18n'
import { formatDay, formatRating, formatTime } from '../home/format'
import { LOBBY, pricePerPlayer, reached, REVIEWS } from './demoData'

// Când se umplu stelele din review, una câte una (o stea pe rând, pentru toți colegii)
const STARS_FROM = 0.76
const STARS_TO = 0.855
const TOTAL_STARS = REVIEWS.reduce((n, r) => n + r.score, 0)

// Text care se schimbă la un pas al poveștii: valoarea nouă apare cu un fade scurt.
function Swap({ value, still }) {
  return (
    <motion.span key={value} initial={still ? false : { opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.25 }}>
      {value}
    </motion.span>
  )
}

// Stelele de lângă ratingul participanților (pline până la media rotunjită), în galbenul aplicației
function Stars({ value }) {
  return (
    <span className="demo-stars" aria-hidden="true">
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} style={{ opacity: value >= n - 0.25 ? 1 : 0.25 }}>★</span>
      ))}
    </span>
  )
}

function Fact({ label, value, still }) {
  return (
    <div>
      <div className="mu lobby-small">{label}</div>
      <div><Swap value={value} still={still} /></div>
    </div>
  )
}

function Row({ label, value, still, check }) {
  return (
    <div className="pl">
      <span className="mu">{label}</span>
      <span className="r demo-row-value">
        {check && (
          <svg className="ic demo-check" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
               strokeLinecap="round" strokeLinejoin="round">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        )}
        <Swap value={value} still={still} />
      </span>
    </div>
  )
}

function Player({ p, me }) {
  const t = useI18n().t
  return (
    <div className="pl">
      <span className="lobby-player">
        <Avatar name={p.username} size={28} />
        <span>
          <span className="demo-name">{p.username}</span>
          {me && <span className="mu"> {t('(tu)')}</span>}
          {p.isHost && <div className="mu lobby-small">{t('organizator')}</div>}
        </span>
      </span>
      <span className="lobby-rating">
        <span><Stars value={p.rating} /> {formatRating(p.rating)}</span>
        <span className="mu lobby-small">{t('{n}% prezență', { n: p.reliability })}</span>
      </span>
    </div>
  )
}

// O stea din cardul de review: gri estompat, iar peste ea cea galbenă apare la pragul ei de scroll
function ReviewStar({ index, filled, progress }) {
  const at = STARS_FROM + (index / TOTAL_STARS) * (STARS_TO - STARS_FROM)
  const opacity = useTransform(progress, [at, at + 0.004], [0, 1])
  return (
    <span className="demo-star">
      <span className="star">★</span>
      {filled && <motion.span className="star on" style={{ opacity }}>★</motion.span>}
    </span>
  )
}

function ReviewCard({ progress, phase, still, cardRef }) {
  const t = useI18n().t
  let index = 0
  return (
    <motion.div
      ref={cardRef}
      className="card"
      initial={still ? false : { opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
    >
      <b>{t('Review colegi de meci')}</b>
      <div className="mu review-hint">{t('Notează de la 1 la 5 cum a jucat fiecare.')}</div>
      {REVIEWS.map((r) => (
        <div key={r.username} className="pl">
          <span>{r.username}</span>
          <span className="r">
            {[1, 2, 3, 4, 5].map((n) => {
              const filled = n <= r.score
              const star = <ReviewStar key={n} index={index} filled={filled} progress={progress} />
              if (filled) index++
              return star
            })}
          </span>
        </div>
      ))}
      <span className={`b review-send${reached(phase, 'rated') ? '' : ' demo-disabled'}`}>{t('Trimite review-urile')}</span>
    </motion.div>
  )
}

// Pagina lobby-ului (features/lobbies/LobbyPage.jsx) văzută de Ana, pas cu pas:
// cere să intre, e acceptată, lobby-ul se confirmă, meciul se încheie și își notează colegii.
export default function DemoLobby({ phase, progress, fluid, still }) {
  const { t, tn } = useI18n()
  const joined = reached(phase, 'joined')
  const locked = reached(phase, 'locked')
  const finished = reached(phase, 'finished')
  const open = !locked

  const current = LOBBY.players.length + LOBBY.broughtPlayers + (joined ? 1 : 0)
  const status = finished ? t('Încheiat') : locked ? t('Confirmat') : t('Deschis')
  const price = pricePerPlayer(current)
  const finalPrice = pricePerPlayer(LOBBY.players.length + LOBBY.broughtPlayers + 1)
  const deadline = `${formatDay(LOBBY.confirmDeadline)}, ${formatTime(LOBBY.confirmDeadline)}`
  const players = joined ? [...LOBBY.players, { ...LOBBY.me, me: true }] : LOBBY.players
  const average = players.reduce((s, p) => s + p.rating, 0) / players.length

  // În fereastra mare, zona principală nu are scroll propriu: o „derulăm" noi, ca să se vadă ce s-a schimbat
  // (participanții după acceptare, apoi cardul de review). Doar transform, măsurat după randare.
  const contentRef = useRef(null)
  const participantsRef = useRef(null)
  const reviewRef = useRef(null)
  const scrollY = useMotionValue(0)

  useLayoutEffect(() => {
    if (fluid) return
    const content = contentRef.current
    const viewport = content.closest('.demo-ct')
    const style = getComputedStyle(viewport)
    const visible = viewport.clientHeight - parseFloat(style.paddingTop) - parseFloat(style.paddingBottom)
    const showBottomOf = (el) => (el ? Math.max(0, el.offsetTop + el.offsetHeight + 4 - visible) : 0)

    let target = 0
    if (reached(phase, 'review')) target = showBottomOf(reviewRef.current)
    else if (reached(phase, 'members') && !locked) target = showBottomOf(participantsRef.current)
    const max = Math.max(0, content.scrollHeight - visible)
    const controls = animate(scrollY, -Math.min(target, max), { type: 'spring', stiffness: 140, damping: 26 })
    return () => controls.stop()
  }, [phase, fluid, locked, scrollY])

  // butonul din dreapta, ca LobbyAction: după relația Anei cu lobby-ul
  let actionKey
  let action
  if (finished) {
    actionKey = 'participated'
    action = <span className="mu">{t('Ai participat')}</span>
  } else if (joined) {
    actionKey = 'accepted'
    action = <span className="b o">{t('Ieși din lobby')}</span>
  } else if (phase === 'pending') {
    actionKey = 'pending'
    action = (
      <span className="r">
        <span className="mu">{t('Cerere trimisă')}</span>
        <span className="b o">{t('Retrage')}</span>
      </span>
    )
  } else {
    actionKey = 'none'
    action = <span className="b">{t('Cere să intri')}</span>
  }

  return (
    <motion.div ref={contentRef} className="lobby-page demo-lobby" style={{ y: scrollY }}>
      <span className="lobby-back">‹ {t('Înapoi')}</span>

      <div className="card">
        <div className="top">
          <h3 className="lobby-title">{t(LOBBY.sport)}</h3>
          <span className="tag"><Swap value={status} still={still} /></span>
        </div>
        <div className="mu">{trField(LOBBY.field)}, {LOBBY.city}</div>

        <div className="lobby-facts">
          <Fact label={t('Data')} value={formatDay(LOBBY.dateTime)} still={still} />
          <Fact label={t('Ora')} value={formatTime(LOBBY.dateTime)} still={still} />
          <Fact label={t('Organizator')} value={LOBBY.host} still={still} />
          <Fact label={t('Locuri')} value={t('{a} din {b}', { a: current, b: LOBBY.maxPlayers })} still={still} />
        </div>

        <div className="bar">
          <motion.i
            className="demo-bar-fill"
            initial={false}
            animate={{ scaleX: current / LOBBY.maxPlayers }}
            transition={{ duration: still ? 0 : 0.5, ease: 'easeOut' }}
          />
        </div>

        <div className="top">
          <span className="mu">
            <Swap value={tn(LOBBY.maxPlayers - current, '{n} loc liber', '{n} locuri libere')} still={still} />, {t('nivel mediu {n}', { n: formatRating(average) })}
          </span>
          <motion.span key={actionKey} initial={still ? false : { opacity: 0 }} animate={{ opacity: 1 }}>
            {action}
          </motion.span>
        </div>
      </div>

      <div className="card">
        <b>{t('Cost')}</b>
        <div className="lobby-list">
          <Row label={t('Teren')} value={`${LOBBY.fieldCost} RON`} still={still} />
          {open && (
            <>
              <Row label={tn(current, 'Acum, la {n} jucător', 'Acum, la {n} jucători')} value={t('{price} / persoană', { price: `${price} RON` })} still={still} />
              <Row label={t('Cu lobby-ul plin')} value={t('{price} / persoană', { price: `${pricePerPlayer(LOBBY.maxPlayers)} RON` })} still={still} />
            </>
          )}
          {locked && <Row label={t('Preț final')} value={t('{price} / persoană', { price: `${finalPrice} RON` })} still={still} check />}
          <Row label={t('Confirmare')} value={open ? t('până {when}', { when: deadline }) : status} still={still} />
          <Row label={t('Minim jucători')} value={LOBBY.minPlayers} still={still} />
        </div>
        {open && (
          <div className="mu lobby-note lobby-small">{t('Prețul devine final la confirmare. Până atunci poți ieși fără penalizare.')}</div>
        )}
        {locked && !finished && (
          <>
            <div className="mu lobby-note lobby-small">{t('De acum, ieșirea din lobby contează ca anulare târzie.')}</div>
            <div className="r lobby-note">
              <span className="b">{t('Plătește online')}</span>
              <span className="mu lobby-small">{t('sau la fața locului')}</span>
            </div>
          </>
        )}
      </div>

      <div className="card" ref={participantsRef}>
        <b>{t('Participanți')}</b>
        <div className="lobby-list">
          {players.map((p) => (
            p.me ? (
              <motion.div key={p.username} initial={still ? false : { opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }}>
                <Player p={p} me />
              </motion.div>
            ) : (
              <Player key={p.username} p={p} />
            )
          ))}
          <div className="pl mu">{t('+{n} jucători aduși de organizator', { n: LOBBY.broughtPlayers })}</div>
        </div>
      </div>

      {finished && <ReviewCard progress={progress} phase={phase} still={still} cardRef={reviewRef} />}
    </motion.div>
  )
}
