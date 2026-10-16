import { useQueryClient } from '@tanstack/react-query'
import { useEffect, useState, type ReactNode } from 'react'
import { api, call } from '../api/client'
import { clearGuestCart, readGuestCart } from '../cart/guestCart'
import { cartQuery } from '../cart/queries'
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
    await mergeGuestCart()
  }

  /** Moves what the visitor put in the cart before logging in into their cart on the server. */
  async function mergeGuestCart() {
    const items = readGuestCart()
    if (items.length === 0) {
      return
    }
    try {
      const cart = await call(api.POST('/me/cart/merge', { body: { items } }))
      clearGuestCart()
      queryClient.setQueryData(cartQuery.queryKey, cart)
    } catch {
      // Logging in still worked. The guest cart stays in localStorage and is merged on the next login
    }
  }

  return <AuthContext value={{ session, login, logout: clearSession }}>{children}</AuthContext>
}
