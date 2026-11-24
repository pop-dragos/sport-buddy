import { Outlet, useLocation } from 'react-router-dom'
import InboxPage from './InboxPage'
import './inbox.css'

// Pagina Mesaje. La început se vede doar lista conversațiilor, pe toată pagina. După ce deschizi una, pagina se
// împarte: conversația în stânga (lângă meniu), lista în dreapta, ca să poți trece de la una la alta.
// Pe ecran îngust se vede un singur panou odată: lista sau conversația.
export default function MessagesLayout() {
  const { pathname } = useLocation()
  const hasChat = pathname !== '/messages'

  return (
    <div className={`msgs${hasChat ? ' has-chat' : ''}`}>
      {hasChat && (
        <div className="msgs-main">
          <Outlet />
        </div>
      )}
      {/* div, nu aside: elementul aside are stiluri globale de bară laterală */}
      <div className="msgs-list">
        <InboxPage />
      </div>
    </div>
  )
}
