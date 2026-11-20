import { motion } from 'motion/react'
import Avatar from '../../shared/Avatar'
import SportIcon from '../../shared/SportIcon'
import { CITY, ME, SPORTS } from './demoData'

// Aceleași iconițe ca în bara laterală a aplicației (layout/Sidebar.jsx)
const NAV_ICONS = {
  home: (
    <>
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <polyline points="9 22 9 12 15 12 15 22" />
    </>
  ),
  agenda: (
    <>
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
      <line x1="16" y1="2" x2="16" y2="6" />
      <line x1="8" y1="2" x2="8" y2="6" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </>
  ),
  friends: (
    <>
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </>
  )
}

function NavIcon({ name }) {
  return (
    <svg className="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round">
      {NAV_ICONS[name]}
    </svg>
  )
}

// Rama ferestrei și interfața fixă a aplicației (bara laterală și header-ul), cu aceleași clase ca layout/.
// Se schimbă doar zona principală (children) și, peste ea, o fereastră modală (overlay), ca în aplicație.
// fluid: pe mobil și cu reduced-motion fereastra are înălțimea conținutului, iar modalul apare în pagină.
export default function AppWindow({ pageTitle, homeActive, pressFootball, overlay, fluid, still, children }) {
  return (
    <div className={`demo-window${fluid ? ' fluid' : ''}`}>
      <div className="demo-titlebar">
        <span className="demo-dots"><i /><i /><i /></span>
        <span className="demo-url">sportbuddy.local</span>
      </div>

      <div className="app demo-app">
        <aside className="sidebar">
          <div className="logo">
            <b>S</b><span>portBuddy</span>
          </div>
          <div className="demo-sbl">
            <span className={`si ${homeActive ? 'active' : ''}`}><NavIcon name="home" /><span>Acasă</span></span>
            <span className="si"><NavIcon name="agenda" /><span>Agenda mea</span></span>
            <span className="si"><NavIcon name="friends" /><span>Prieteni</span></span>
            <div className="mu" style={{ padding: '10px 10px 4px' }}>SPORTURI</div>
            {SPORTS.map((s) => (
              <span key={s.code} className="si demo-sport">
                {s.code === 'football' && pressFootball && (
                  <motion.i className="demo-press" style={{ opacity: pressFootball }} />
                )}
                <SportIcon type={s.code} />
                <span>{s.name}</span>
              </span>
            ))}
          </div>
          <div className="um">
            <span className="si">
              <Avatar name={ME.fullName} />
              <span className="nm">{ME.username}</span>
            </span>
          </div>
        </aside>

        <div className="demo-content">
          <header>
            <motion.h2
              key={pageTitle}
              initial={still ? false : { opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.2 }}
            >
              {pageTitle}
            </motion.h2>
            <div className="r">
              <div className="city-search">
                <input value={CITY} readOnly tabIndex={-1} />
                <span className="city-toggle">▾</span>
              </div>
              <span className="b plus">+</span>
            </div>
          </header>
          <div className="demo-ct">{fluid && overlay ? overlay : children}</div>
        </div>

        {!fluid && overlay}
      </div>
    </div>
  )
}
