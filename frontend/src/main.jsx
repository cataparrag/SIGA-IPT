import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { CssBaseline, ThemeProvider } from '@mui/material'
import theme from './theme'
import keycloak from './auth/keycloak'
import { RoleProvider } from './context/RoleContext'
import { NotificationProvider } from './context/NotificationContext'
import App from './App'
import './index.css'

const root = createRoot(document.getElementById('root'))

// The app starts only after the user logs in with Keycloak (login-required).
keycloak
  .init({ onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false })
  .then(() => {
    root.render(
      <StrictMode>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <BrowserRouter>
            <RoleProvider>
              <NotificationProvider>
                <App />
              </NotificationProvider>
            </RoleProvider>
          </BrowserRouter>
        </ThemeProvider>
      </StrictMode>,
    )
  })
  .catch(() => {
    root.render(
      <div style={{ fontFamily: 'sans-serif', padding: 32 }}>
        No se pudo conectar con el servidor de autenticación (Keycloak). Revisa que esté levantado.
      </div>,
    )
  })
