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
})
