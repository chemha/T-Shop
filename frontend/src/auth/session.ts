/**
 * The logged-in user's token, kept in sessionStorage: it survives a page reload but is gone when
 * the tab closes. Any script running on the page can read it, so the app must not have XSS holes
 * (React escapes everything it renders, and the app never injects raw HTML).
 */

const STORAGE_KEY = 'tshop.token'

export type Session = {
  token: string
  customerId: number
  roles: string[]
  /** Milliseconds since epoch, like Date.now() */
  expiresAt: number
}

/**
 * Reads the token's payload. This is only used to adapt the UI (show the admin menu, log out when
 * the token expires): the signature is checked by the server on every request, so editing the
 * token here would gain nothing.
 */
export function parseToken(token: string): Session {
  const payload = token.split('.')[1]
  if (!payload) {
    throw new Error('Malformed token')
  }
  const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
  const claims = JSON.parse(atob(base64)) as { sub: string; roles?: string[]; exp: number }
  return {
    token,
    customerId: Number(claims.sub),
    roles: claims.roles ?? [],
    expiresAt: claims.exp * 1000,
  }
}

export function isAdmin(session: Session | null): boolean {
  return session?.roles.includes('ADMIN') ?? false
}

const listeners = new Set<() => void>()

/** Notifies when the user logs in or out, including when the API rejects an expired token. */
export function onSessionChange(listener: () => void): () => void {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function loadSession(now: number = Date.now()): Session | null {
  const token = sessionStorage.getItem(STORAGE_KEY)
  if (!token) {
    return null
  }
  try {
    const session = parseToken(token)
    return session.expiresAt > now ? session : null
  } catch {
    return null
  }
}

export function saveSession(token: string): void {
  sessionStorage.setItem(STORAGE_KEY, token)
  listeners.forEach((listener) => listener())
}

export function clearSession(): void {
  sessionStorage.removeItem(STORAGE_KEY)
  listeners.forEach((listener) => listener())
}
