import { afterEach, describe, expect, it } from 'vitest'
import { isAdmin, loadSession, parseToken, saveSession } from './session'

/** A token with the given claims. The signature is irrelevant here: only the server checks it. */
function token(claims: object): string {
  const base64url = (value: object) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
  return `${base64url({ alg: 'RS256' })}.${base64url(claims)}.signature`
}

describe('session', () => {
  afterEach(() => sessionStorage.clear())

  it('reads the customer, roles and expiry from the token', () => {
    const session = parseToken(token({ sub: '42', roles: ['ADMIN'], exp: 2_000_000_000 }))

    expect(session.customerId).toBe(42)
    expect(isAdmin(session)).toBe(true)
    expect(session.expiresAt).toBe(2_000_000_000_000)
  })

  it('forgets an expired token', () => {
    saveSession(token({ sub: '1', roles: ['CUSTOMER'], exp: 100 }))

    expect(loadSession(101_000)).toBeNull()
    expect(loadSession(99_000)?.customerId).toBe(1)
  })

  it('ignores a malformed token', () => {
    sessionStorage.setItem('tshop.token', 'not-a-jwt')

    expect(loadSession()).toBeNull()
  })
})
