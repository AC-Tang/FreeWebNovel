import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueJsx(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: true,
    proxy: {
      // 配置 API 代理
      '/api': {
        target: 'http://localhost:8080', // 后端 API 地址
        changeOrigin: true, // 是否改变域名
        // 不再重写路径，保留/api前缀，因为后端现在需要/api前缀
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
  },
})
