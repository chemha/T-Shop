import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuth } from '../auth/useAuth'
import { isAdmin } from '../auth/session'

/** Sends anonymous users to the login page, and back here after logging in. */
export function RequireAuth({ admin = false, children }: { admin?: boolean; children: ReactNode }) {
  const { session } = useAuth()
  const location = useLocation()

  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (admin && !isAdmin(session)) {
    return <Navigate to="/" replace />
  }
  return children
}
