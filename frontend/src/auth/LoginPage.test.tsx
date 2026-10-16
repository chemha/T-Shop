import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter } from 'react-router'
import { AuthProvider } from './AuthProvider'
import { LoginPage } from './LoginPage'

function renderLoginPage() {
  render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

/** Replaces the network with a single canned response. */
function respondWith(status: number, body: object) {
  const fetch = vi.fn(async (_request: Request) =>
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/problem+json' } }),
  )
  vi.stubGlobal('fetch', fetch)
  return fetch
}

/** A token the client can read (only the server checks signatures). */
function fakeToken(): string {
  const payload = btoa(JSON.stringify({ sub: '7', roles: ['CUSTOMER'], exp: Math.floor(Date.now() / 1000) + 3600 }))
  return `header.${payload.replace(/=+$/, '')}.signature`
}

describe('LoginPage', () => {
  afterEach(() => {
    cleanup()
    vi.unstubAllGlobals()
    localStorage.clear()
    sessionStorage.clear()
  })

  it('moves the guest cart to the server after logging in', async () => {
    localStorage.setItem('tshop.guestCart', JSON.stringify([{ productId: 3, quantity: 2 }]))
    const requests: Request[] = []
    vi.stubGlobal(
      'fetch',
      vi.fn(async (request: Request) => {
        requests.push(request)
        const body = request.url.endsWith('/auth/login')
          ? { accessToken: fakeToken(), tokenType: 'Bearer', expiresIn: 3600 }
          : { items: [], total: 0 }
        return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } })
      }),
    )
    renderLoginPage()

    await userEvent.type(screen.getByLabelText('Email'), 'ana@example.com')
    await userEvent.type(screen.getByLabelText('Password'), 'correct-password')
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    await vi.waitFor(() => expect(localStorage.getItem('tshop.guestCart')).toBeNull())
    const merge = requests.find((request) => request.url.endsWith('/me/cart/merge'))!
    // Sent with the token just received, so the server knows whose cart it is
    expect(merge.headers.get('Authorization')).toBe(`Bearer ${sessionStorage.getItem('tshop.token')}`)
    expect(await merge.json()).toEqual({ items: [{ productId: 3, quantity: 2 }] })
  })

  it('shows the problem detail returned by the API', async () => {
    const fetch = respondWith(401, { status: 401, detail: 'Invalid email or password' })
    renderLoginPage()

    await userEvent.type(screen.getByLabelText('Email'), 'ana@example.com')
    await userEvent.type(screen.getByLabelText('Password'), 'wrong')
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(await screen.findByRole('alert')).toHaveProperty('textContent', 'Invalid email or password')
    const [request] = fetch.mock.calls[0]!
    expect(request.url).toMatch(/\/api\/v1\/auth\/login$/)
    expect(await request.json()).toEqual({ email: 'ana@example.com', password: 'wrong' })
  })

  it('marks the fields rejected by validation when registering', async () => {
    respondWith(400, {
      status: 400,
      detail: 'The request has invalid fields.',
      errors: [{ field: 'password', message: 'size must be between 8 and 72' }],
    })
    renderLoginPage()

    await userEvent.click(screen.getByRole('button', { name: 'Create an account' }))
    await userEvent.type(screen.getByLabelText('First name'), 'Ana')
    await userEvent.type(screen.getByLabelText('Last name'), 'García')
    await userEvent.type(screen.getByLabelText('Birth date'), '1990-05-17')
    await userEvent.type(screen.getByLabelText('Email'), 'ana@example.com')
    await userEvent.type(screen.getByLabelText('Password'), 'short')
    await userEvent.click(screen.getByRole('button', { name: 'Create account' }))

    expect(await screen.findByText('size must be between 8 and 72')).toBeDefined()
    expect(screen.getByLabelText('Password').getAttribute('aria-invalid')).toBe('true')
  })
})
