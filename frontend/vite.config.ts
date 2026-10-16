/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // The browser only talks to the Vite dev server, which forwards /api to Spring Boot.
    // Same origin for the browser, so the backend needs no CORS configuration.
    proxy: {
      '/api': 'http://localhost:9000',
    },
  },
  test: {
    environment: 'jsdom',
  },
})
