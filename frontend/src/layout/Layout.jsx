import { NavLink, Outlet } from 'react-router-dom'

export default function Layout() {
  return (
    <div className="app">
      <aside className="sidebar">
        <b>SportBuddy</b>
        <NavLink to="/">Acasă</NavLink>
        <NavLink to="/agenda">Agenda mea</NavLink>
        <NavLink to="/friends">Prieteni</NavLink>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}