import { defineConfig, loadEnv, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

// 读取 package.json 以注入应用版本号
const pkg = JSON.parse(
  readFileSync(fileURLToPath(new URL('./package.json', import.meta.url)), 'utf-8')
) as { version: string }

// 在构建产物根目录生成 version.json，包含版本信息与构建时间戳，便于追踪与回滚
function buildMetaPlugin(meta: Record<string, string>): Plugin {
  return {
    name: 'build-meta',
    generateBundle() {
      this.emitFile({
        type: 'asset',
        fileName: 'version.json',
        source: JSON.stringify(meta, null, 2)
      })
    }
  }
}

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 按 mode 加载对应的 .env.[mode] 环境变量（默认仅读取 VITE_ 前缀变量）
  const env = loadEnv(mode, process.cwd())
  const buildTimestamp = new Date().toISOString()

  const meta = {
    version: pkg.version,
    buildTimestamp,
    mode,
    appEnv: env.VITE_APP_ENV || mode
  }

  return {
    plugins: [vue(), buildMetaPlugin(meta)],
    base: env.VITE_BASE_URL || '/',
    define: {
      // 注入到构建产物中的全局常量
      __APP_VERSION__: JSON.stringify(pkg.version),
      __BUILD_TIMESTAMP__: JSON.stringify(buildTimestamp),
      __APP_MODE__: JSON.stringify(mode)
    },
    server: {
      port: Number(env.VITE_DEV_PORT) || 5173,
      // 开发环境按 VITE_API_BASE_URL 代理后端接口
      proxy: env.VITE_API_BASE_URL
        ? {
            '/api': {
              target: env.VITE_API_BASE_URL,
              changeOrigin: true
            }
          }
        : undefined
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      // 生产环境关闭 sourcemap，其余环境开启以便排查问题
      sourcemap: mode !== 'production'
    }
  }
})
