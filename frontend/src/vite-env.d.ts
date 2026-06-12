/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 构建期由 vite.config.ts 的 define 注入的全局常量
declare const __APP_VERSION__: string
declare const __BUILD_TIMESTAMP__: string
declare const __APP_MODE__: string

// VITE_ 环境变量类型声明（对应 .env.development / .env.test / .env.production）
interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_ENV: string
  readonly VITE_API_BASE_URL: string
  readonly VITE_BASE_URL: string
  readonly VITE_DEV_PORT?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
