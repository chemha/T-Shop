import { createContext, use } from 'react'
import type { Session } from './session'

export type Auth = {
  session: Session | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

// Kept apart from AuthProvider: Vite's hot reload needs files that export only components
export const AuthContext = createContext<Auth | null>(null)

export function useAuth(): Auth {
  const auth = use(AuthContext)
  if (!auth) {
    throw new Error('useAuth must be used inside <AuthProvider>')
  }
  return auth
}
