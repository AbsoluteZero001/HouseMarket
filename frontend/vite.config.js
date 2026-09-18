import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
    define: {
        global: 'globalThis'
    },
    optimizeDeps: {
        esbuildOptions: {
            define: {
                global: 'globalThis'
            }
        }
    },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        ws: true
      },
      '/user': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
})
