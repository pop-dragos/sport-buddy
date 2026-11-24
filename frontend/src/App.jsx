import { lazy, Suspense } from 'react'
import { useI18n } from './shared/i18n/useI18n'
import { Routes, Route } from 'react-router-dom'
import Layout from './layout/Layout'
import HomePage from './features/home/HomePage'
import AgendaPage from './features/lobbies/AgendaPage'
import LobbyPage from './features/lobbies/LobbyPage'
import FriendsPage from './features/friends/FriendsPage'
import ConversationPage from './features/inbox/ConversationPage'
import MessagesLayout from './features/inbox/MessagesLayout'
import ProfilePage from './features/profile/ProfilePage'
import SettingsPage from './features/profile/SettingsPage'
import LoginPage from './features/auth/LoginPage'
import ForgotPasswordPage from './features/auth/ForgotPasswordPage'
import ResetPasswordPage from './features/auth/ResetPasswordPage'
import VerifyEmailPage from './features/auth/VerifyEmailPage'
import ConfirmEmailChangePage from './features/auth/ConfirmEmailChangePage'
import LegalPage from './features/legal/LegalPage'
import RequireAuth from './features/auth/RequireAuth'

// Pagina de prezentare se încarcă separat, ca biblioteca de animații să nu ajungă în restul aplicației.
const LandingPage = lazy(() => import('./features/landing/LandingPage'))

export default function App() {
  // la schimbarea limbii paginile se reiau de la zero, ca să nu rămână pe ecran texte din limba veche
  const { lang } = useI18n()
  return (
    <Routes key={lang}>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      {/* deschise din linkurile trimise pe email */}
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/confirm-email" element={<ConfirmEmailChangePage />} />
      <Route path="/termeni" element={<LegalPage page="terms" />} />
      <Route path="/confidentialitate" element={<LegalPage page="privacy" />} />
      <Route path="/bun-venit" element={<Suspense fallback={null}><LandingPage /></Suspense>} />
      <Route element={<RequireAuth />}>
        <Route element={<Layout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/agenda" element={<AgendaPage />} />
          <Route path="/lobbies/:id" element={<LobbyPage />} />
          <Route path="/friends" element={<FriendsPage />} />
          <Route path="/messages" element={<MessagesLayout />}>
            <Route path="direct/:id" element={<ConversationPage kind="direct" />} />
            <Route path="lobby/:id" element={<ConversationPage kind="lobby" />} />
          </Route>
          <Route path="/profile/:id" element={<ProfilePage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>
      </Route>
    </Routes>
  )
}
