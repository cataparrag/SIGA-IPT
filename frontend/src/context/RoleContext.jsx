import { createContext, useContext, useEffect, useState } from 'react'
import { Box, CircularProgress, Typography } from '@mui/material'
import keycloak from '../auth/keycloak'
import api, { errorMessage } from '../api/client'

const ROLE_PRIORITY = ['ADMIN', 'TEACHER', 'STUDENT']
const RoleContext = createContext(null)

/**
 * Provides the logged-in user. The role comes from the Keycloak token; the
 * student/teacher id comes from /api/me (the backend links them by email).
 */
export function RoleProvider({ children }) {
  const [user, setUser] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.get('/me')
      .then(({ data }) => setUser(data))
      .catch((err) => setError(errorMessage(err)))
  }, [])

  if (error) {
    return (
      <Box sx={{ display: 'grid', placeItems: 'center', minHeight: '100vh', p: 3, textAlign: 'center' }}>
        <Box>
          <Typography variant="h6">No se pudo cargar tu cuenta</Typography>
          <Typography color="text.secondary">{error}</Typography>
        </Box>
      </Box>
    )
  }
  if (!user) {
    return <Box sx={{ display: 'grid', placeItems: 'center', minHeight: '100vh' }}><CircularProgress /></Box>
  }

  const role = ROLE_PRIORITY.find((r) => user.roles.includes(r))
  const logout = () => keycloak.logout({ redirectUri: window.location.origin })
  return <RoleContext.Provider value={{ role, user, logout }}>{children}</RoleContext.Provider>
}

export function useRole() {
  return useContext(RoleContext)
}
