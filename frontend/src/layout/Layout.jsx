import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Header from './Header'

export default function Layout() {
  return (
    <div className="app">
      <Sidebar />
      <main className="content">
        <Header />
        <div id="ct">
          <Outlet />
        </div>
      </main>
    </div>
  )
}