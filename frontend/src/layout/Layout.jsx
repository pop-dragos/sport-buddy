import { Outlet, useLocation } from 'react-router-dom'
import Sidebar from './Sidebar'
import Header from './Header'

// Paginile sunt centrate, ca în prototip. Prima pagină are propriul layout (două coloane, 1400px),
// iar pagina unui lobby e mai lată, ca să încapă detaliile și chat-ul unul lângă altul.
function pageClass(pathname) {
  if (pathname === '/') return undefined
  if (pathname.startsWith('/lobbies/')) return 'page page-wide'
  return 'page'
}

export default function Layout() {
  const { pathname } = useLocation()

  return (
    <div className="app">
      <Sidebar />
      <main className="content">
        <Header />
        <div id="ct">
          <div className={pageClass(pathname)}>
            <Outlet />
          </div>
        </div>
      </main>
    </div>
  )
}