import { useQueryClient } from '@tanstack/react-query'
import { useEffect, useState, type ReactNode } from 'react'
import { api, call } from '../api/client'
import { clearSession, loadSession, onSessionChange, saveSession } from './session'
import { AuthContext } from './useAuth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState(loadSession)
  const queryClient = useQueryClient()

  useEffect(
    () =>
      onSessionChange(() => {
        setSession(loadSession())
        // Never show one user's cached cart or orders to the next one
        queryClient.clear()
      }),
    [queryClient],
  )

  // Log out by itself when the token expires while the page is open
  useEffect(() => {
    if (!session) {
      return
    }
    const timer = setTimeout(clearSession, session.expiresAt - Date.now())
    return () => clearTimeout(timer)
  }, [session])

  async function login(email: string, password: string) {
    const response = await call(api.POST('/auth/login', { body: { email, password } }))
    saveSession(response.accessToken)
  }

  return <AuthContext value={{ session, login, logout: clearSession }}>{children}</AuthContext>
}
