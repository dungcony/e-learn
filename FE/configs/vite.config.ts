import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig(({ mode }) => {
  // nạp biến môi trường từ thư mục FE
  const env = loadEnv(mode, path.resolve(__dirname, '..'), '');
  const backendTarget = env.VITE_API_BASE_URL || 'http://localhost:8000';

  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, '../src'),
      },
    },
    css: {
      // thư mục chứa postcss.config.js
      postcss: __dirname,
    },
    server: {
      port: 3000,
      proxy: {
        '/auth': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/users': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/tables': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/dishes': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/orders': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/kitchen': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/invoices': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/reports': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/admin': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/files': {
          target: backendTarget,
          changeOrigin: true,
        },
      },
    },
  };
});
