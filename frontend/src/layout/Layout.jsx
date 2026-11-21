import { Outlet, useLocation } from 'react-router-dom'
import { useLayoutEffect, useRef } from 'react'
import Sidebar from './Sidebar'
import Header from './Header'
import BottomNav from './BottomNav'
import { NavigationProvider } from './NavigationProvider'

// Paginile sunt centrate, ca în prototip. Prima pagină are propriul layout (două coloane, 1400px),
// iar pagina unui lobby e mai lată, ca să încapă detaliile și chat-ul unul lângă altul.
function pageClass(pathname) {
  if (pathname === '/') return undefined
  if (pathname.startsWith('/lobbies/')) return 'page page-wide'
  return 'page'
}

export default function Layout() {
  const { pathname } = useLocation()
  const contentRef = useRef(null)

  useLayoutEffect(() => {
    contentRef.current?.scrollTo({ top: 0 })
  }, [pathname])

  return (
    <NavigationProvider>
      <div className="app">
        <Sidebar />
        <main className="content">
          <Header />
          <div id="ct" ref={contentRef}>
            <div className={pageClass(pathname)}>
              <Outlet />
            </div>
          </div>
        </main>
        <BottomNav />
      </div>
    </NavigationProvider>
  )
}
