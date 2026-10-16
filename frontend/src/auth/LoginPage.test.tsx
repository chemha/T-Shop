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

describe('LoginPage', () => {
  afterEach(() => {
    cleanup()
    vi.unstubAllGlobals()
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
