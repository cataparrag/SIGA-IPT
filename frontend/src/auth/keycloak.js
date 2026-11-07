import Keycloak from 'keycloak-js'

/**
 * Keycloak runs under the /auth path. In development it is at http://localhost:8180/auth
 * (set in .env.development); in production Nginx serves it at <same server>/auth.
 */
const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || `${window.location.origin}/auth`,
  realm: 'siga-ipt',
  clientId: 'siga-frontend',
})

export default keycloak
