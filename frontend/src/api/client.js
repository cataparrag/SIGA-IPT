import axios from 'axios'

// All requests go to /api; Vite (dev) or Nginx (production) forwards them to the backend.
const api = axios.create({ baseURL: '/api' })

// Tomorrow: an interceptor here will add "Authorization: Bearer <token>" from Keycloak.

/** Extracts the message prepared by the backend's GlobalExceptionHandler. */
export function errorMessage(error) {
  return error?.response?.data?.message ?? 'No se pudo conectar con el servidor'
}

export default api
