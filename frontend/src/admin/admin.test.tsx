import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter } from 'react-router'
import { App } from '../App'
import { AuthProvider } from '../auth/AuthProvider'

function logInAs(customerId: number, role: 'ADMIN' | 'CUSTOMER') {
  const payload = btoa(JSON.stringify({ sub: String(customerId), roles: [role], exp: Math.floor(Date.now() / 1000) + 3600 }))
  sessionStorage.setItem('tshop.token', `header.${payload.replace(/=+$/, '')}.signature`)
}

/** Answers each request with the body returned by respond(request), recording every request. */
function fakeApi(respond: (request: Request) => { status?: number; body: unknown }) {
  const requests: Request[] = []
  vi.stubGlobal(
    'fetch',
    vi.fn(async (request: Request) => {
      requests.push(request)
      const { status = 200, body } = respond(request)
      return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
    }),
  )
  return requests
}

function renderAt(path: string) {
  render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={[path]}>
        <AuthProvider>
          <App />
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const page = (content: unknown[]) => ({ content, page: { number: 0, size: 20, totalElements: content.length, totalPages: 1 } })
const customer = (id: number, firstName: string, role: 'ADMIN' | 'CUSTOMER') => ({
  id,
  firstName,
  lastName: 'Test',
  birthDate: '1990-01-01',
  email: `${firstName.toLowerCase()}@example.com`,
  role,
  createdAt: '2026-10-16T10:00:00Z',
})

describe('admin pages', () => {
  afterEach(() => {
    cleanup()
    vi.unstubAllGlobals()
    sessionStorage.clear()
    localStorage.clear()
  })

  it('are not shown to customers', async () => {
    logInAs(2, 'CUSTOMER')
    fakeApi(() => ({ body: page([]) }))

    renderAt('/admin/customers')

    expect(await screen.findByRole('heading', { name: 'Catalogue' })).toBeDefined()
    expect(screen.queryByRole('heading', { name: 'Customers' })).toBeNull()
  })

  it('let an admin promote a customer, but not change their own account', async () => {
    logInAs(1, 'ADMIN')
    const requests = fakeApi((request) =>
      request.method === 'PUT'
        ? { body: customer(2, 'Bob', 'ADMIN') }
        : { body: page([customer(1, 'Ana', 'ADMIN'), customer(2, 'Bob', 'CUSTOMER')]) },
    )
    renderAt('/admin/customers')

    const anaRow = (await screen.findByText('Ana Test')).closest('tr')!
    expect(within(anaRow).queryByRole('button')).toBeNull()

    const bobRow = screen.getByText('Bob Test').closest('tr')!
    await userEvent.click(within(bobRow).getByRole('button', { name: 'Make admin' }))

    await vi.waitFor(() => expect(requests.some((request) => request.method === 'PUT')).toBe(true))
    const put = requests.find((request) => request.method === 'PUT')!
    expect(put.url).toMatch(/\/admin\/customers\/2\/role$/)
    expect(await put.json()).toEqual({ role: 'ADMIN' })
  })

  it('show why a category could not be deleted', async () => {
    logInAs(1, 'ADMIN')
    vi.stubGlobal('confirm', () => true)
    fakeApi((request) =>
      request.method === 'DELETE'
        ? { status: 409, body: { status: 409, detail: "The category 'Laptops' still has products" } }
        : { body: [{ id: 1, name: 'Laptops' }] },
    )
    renderAt('/admin/categories')

    const row = (await screen.findByText('Laptops')).closest('tr')!
    await userEvent.click(within(row).getByRole('button', { name: 'Delete' }))

    expect(await screen.findByText("The category 'Laptops' still has products")).toBeDefined()
  })
})
