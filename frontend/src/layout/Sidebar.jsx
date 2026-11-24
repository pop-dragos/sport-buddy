import { NavLink } from 'react-router-dom'
import SportIcon from '../shared/SportIcon'
import { useT } from '../shared/i18n/useI18n'
import NavIcon from './NavIcon'
import useNavigation from './useNavigation'
import UserMenu from './UserMenu'

export default function Sidebar() {
  const { sports, sportsError, todoTotal, openCalendar } = useNavigation()
  const t = useT()

  return (
    <aside className="sidebar">
      <div className="logo">
        <b>S</b><span>portBuddy</span>{/* i18n-ignore: numele aplicației */}
      </div>

      <div id="sbl">
        <NavLink to="/" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <NavIcon name="home" />
          <span>{t('Acasă')}</span>
        </NavLink>

        <NavLink to="/agenda" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <NavIcon name="agenda" />
          <span>{t('Agenda mea')}</span>
          {todoTotal > 0 && <span className="agenda-count" title={t('De făcut')}>{todoTotal}</span>}
        </NavLink>

        <NavLink to="/friends" className={({ isActive }) => `si ${isActive ? 'active' : ''}`}>
          <NavIcon name="friends" />
          <span>{t('Prieteni')}</span>
        </NavLink>

        <div className="mu sports-label" style={{ padding: '10px 10px 4px' }}>{t('SPORTURI')}</div>

        {sportsError && <div className="mu" style={{ padding: '4px 10px' }}>{t('Sporturile nu sunt disponibile.')}</div>}

        {sports.map((s) => (
          <button key={s.id} className="si" onClick={() => openCalendar(s)}>
            <SportIcon type={s.code} />
            <span>{t(s.name)}</span>
          </button>
        ))}
      </div>

      <UserMenu />
    </aside>
  )
}
