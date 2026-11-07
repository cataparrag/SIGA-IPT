import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The dev server forwards /api/* to the Spring Boot backend, so the browser
// only talks to one origin (no CORS needed). In production Nginx does the same.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})