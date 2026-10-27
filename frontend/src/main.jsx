import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './shared/styles.css'
import App from './App.jsx'
import AuthProvider from './features/auth/AuthProvider'
import CityProvider from './features/catalog/CityProvider'
import { applyStoredTheme } from './shared/theme'

applyStoredTheme()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <CityProvider>
          <App />
        </CityProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>
)
