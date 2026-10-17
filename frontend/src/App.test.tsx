import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter } from 'react-router'
import { App } from './App'
import { AuthProvider } from './auth/AuthProvider'

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

const product = { id: 3, categoryId: 1, categoryName: 'Laptops', name: 'Dell XPS 13', price: 1099.99, stock: 10 }

describe('App', () => {
  afterEach(() => {
    cleanup()
    vi.unstubAllGlobals()
    localStorage.clear()
    sessionStorage.clear()
  })

  it('links to the source code on every page', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response(JSON.stringify([]), { headers: { 'Content-Type': 'application/json' } })),
    )
    renderAt('/cart')

    const link = await screen.findByRole('link', { name: 'Source code on GitHub' })
    expect(link.getAttribute('href')).toBe('https://github.com/chemha/T-Shop')
    expect(link.getAttribute('target')).toBe('_blank')
    expect(link.getAttribute('rel')).toContain('noopener')
  })

  it('shows a visitor their cart without asking them to log in', async () => {
    localStorage.setItem('tshop.guestCart', JSON.stringify([{ productId: 3, quantity: 2 }]))
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response(JSON.stringify(product), { headers: { 'Content-Type': 'application/json' } })),
    )

    renderAt('/cart')

    expect(await screen.findByText('Dell XPS 13')).toBeDefined()
    expect(screen.getByRole('heading', { name: 'Cart' })).toBeDefined()
    expect(screen.getByRole('link', { name: 'Log in or create an account' })).toBeDefined()
  })

  it('updates the cart when the quantity changes, without leaving the field', async () => {
    localStorage.setItem('tshop.guestCart', JSON.stringify([{ productId: 3, quantity: 1 }]))
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response(JSON.stringify(product), { headers: { 'Content-Type': 'application/json' } })),
    )
    renderAt('/cart')

    const quantity = await screen.findByLabelText('Quantity of Dell XPS 13')
    await userEvent.clear(quantity)
    await userEvent.type(quantity, '3')

    // Subtotal and total (3 × 1099.99) appear once the short delay has passed
    await waitFor(() => expect(screen.getAllByText('€3,299.97')).toHaveLength(2))
    expect(JSON.parse(localStorage.getItem('tshop.guestCart')!)).toEqual([{ productId: 3, quantity: 3 }])
  })

  it('applies the quantity at once when pressing Enter', async () => {
    localStorage.setItem('tshop.guestCart', JSON.stringify([{ productId: 3, quantity: 1 }]))
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response(JSON.stringify(product), { headers: { 'Content-Type': 'application/json' } })),
    )
    renderAt('/cart')

    const quantity = await screen.findByLabelText('Quantity of Dell XPS 13')
    await userEvent.clear(quantity)
    await userEvent.type(quantity, '4{Enter}')

    // No waiting for the delay
    expect(JSON.parse(localStorage.getItem('tshop.guestCart')!)).toEqual([{ productId: 3, quantity: 4 }])
  })

  it('sends a rejected quantity only once', async () => {
    // Logged-in customer whose cart is on the server
    const payload = btoa(JSON.stringify({ sub: '7', roles: ['CUSTOMER'], exp: Math.floor(Date.now() / 1000) + 3600 }))
    sessionStorage.setItem('tshop.token', `header.${payload.replace(/=+$/, '')}.signature`)
    const cart = { items: [{ productId: 3, productName: 'Dell XPS 13', unitPrice: 1099.99, quantity: 1, subtotal: 1099.99, inStock: true }], total: 1099.99 }
    const puts: Request[] = []
    vi.stubGlobal(
      'fetch',
      vi.fn(async (request: Request) => {
        if (request.method === 'PUT') {
          puts.push(request)
          return new Response(JSON.stringify({ status: 409, detail: "Only 10 unit(s) of 'Dell XPS 13' left, 50 requested" }), {
            status: 409,
            headers: { 'Content-Type': 'application/problem+json' },
          })
        }
        const body = request.url.endsWith('/me/addresses') ? [] : cart
        return new Response(JSON.stringify(body), { headers: { 'Content-Type': 'application/json' } })
      }),
    )
    renderAt('/cart')

    const quantity = await screen.findByLabelText('Quantity of Dell XPS 13')
    await userEvent.clear(quantity)
    await userEvent.type(quantity, '50')

    expect(await screen.findByText(/Only 10 unit\(s\)/)).toBeDefined()
    // Showing the error re-renders the page; before the fix that sent the same request every 400 ms
    await new Promise((resolve) => setTimeout(resolve, 1300))
    expect(puts).toHaveLength(1)
  })
})
