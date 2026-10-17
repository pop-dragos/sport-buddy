import React from 'react';
import { Outlet, Link } from 'react-router-dom';

export default function Layout() {
  return (
    <div className="layout-container">
      {/* Header */}
      <header className="header">
        <h1>SportBuddy</h1>
      </header>

      <div className="layout-body" style={{ display: 'flex' }}>
        {/* Sidebar cu link-uri */}
        <aside className="sidebar">
          <nav>
            <ul>
              <li><Link to="/">Acasă</Link></li>
              <li><Link to="/chat">Chat</Link></li>
              <li><Link to="/profile">Profil</Link></li>
              <li><Link to="/lobbies">Lobbies</Link></li>
            </ul>
          </nav>
        </aside>

        {/* Zona unde se va afișa pagina corespunzătoare rutei */}
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}