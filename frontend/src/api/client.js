import axios from 'axios'
import keycloak from '../auth/keycloak'

// All requests go to /api; Vite (dev) or Nginx (production) forwards them to the backend.
const api = axios.create({ baseURL: '/api' })

// Adds "Authorization: Bearer <token>" to every request, refreshing the token if it expires in < 30 s.
api.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    try {
      await keycloak.updateToken(30)
    } catch {
      keycloak.login()
    }
    config.headers.Authorization = `Bearer ${keycloak.token}`
  }
  return config
})

/** Extracts the message prepared by the backend's GlobalExceptionHandler. */
export function errorMessage(error) {
  if (error?.response?.status === 401) return 'Tu sesión expiró. Vuelve a iniciar sesión.'
  if (error?.response?.status === 403 && !error.response.data?.message) return 'No tienes permiso para realizar esta acción.'
  return error?.response?.data?.message ?? 'No se pudo conectar con el servidor'
}

export default api
