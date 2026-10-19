import { Routes, Route } from 'react-router-dom'
import Layout from './layout/Layout'
import HomePage from './features/home/HomePage'
import AgendaPage from './features/lobbies/AgendaPage'
import LobbyPage from './features/lobbies/LobbyPage'
import FriendsPage from './features/friends/FriendsPage'
import ProfilePage from './features/profile/ProfilePage'
import SettingsPage from './features/profile/SettingsPage'
import LoginPage from './features/auth/LoginPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/agenda" element={<AgendaPage />} />
        <Route path="/lobbies/:id" element={<LobbyPage />} />
        <Route path="/friends" element={<FriendsPage />} />
        <Route path="/profile/:username" element={<ProfilePage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Route>
    </Routes>
  )
}