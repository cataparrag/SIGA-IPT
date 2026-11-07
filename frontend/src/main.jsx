import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { CssBaseline, ThemeProvider } from '@mui/material'
import theme from './theme'
import { RoleProvider } from './context/RoleContext'
import App from './App'
import './index.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <RoleProvider>
          <App />
        </RoleProvider>
      </BrowserRouter>
    </ThemeProvider>
  </StrictMode>,
)
