import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': process.env.VITE_PROXY_TARGET || 'http://localhost:8080' },
    // în Docker pe Windows, schimbările de fișiere nu sunt detectate fără polling
    watch: process.env.VITE_POLLING ? { usePolling: true } : undefined
  }
})