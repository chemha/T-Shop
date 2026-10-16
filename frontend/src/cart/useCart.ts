import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query'
import { useSyncExternalStore } from 'react'
import { api, ApiError, call, type Schemas } from '../api/client'
import { useAuth } from '../auth/useAuth'
import { onGuestCartChange, readGuestCart, removeGuestItem, setGuestItem } from './guestCart'
import { cartQuery } from './queries'

export type CartLine = Schemas['CartItem']

export type CartState = {
  lines: CartLine[]
  total: number
  isPending: boolean
  error: unknown
  isGuest: boolean
  setQuantity: (productId: number, quantity: number) => void
  remove: (productId: number) => void
  updating: boolean
  updateError: unknown
}

/**
 * The cart, whoever is browsing: the customer's cart on the server when logged in, or the guest
 * cart in localStorage otherwise. Pages use it the same way in both cases.
 */
export function useCart(): CartState {
  const { session } = useAuth()
  const server = useServerCart(session !== null)
  const guest = useGuestCart(session === null)
  return session ? server : guest
}

function useServerCart(enabled: boolean): CartState {
  const queryClient = useQueryClient()
  const cart = useQuery({ ...cartQuery, enabled })
  const onSuccess = (updated: Schemas['Cart']) => queryClient.setQueryData(cartQuery.queryKey, updated)

  const set = useMutation({
    mutationFn: ({ productId, quantity }: { productId: number; quantity: number }) =>
      call(api.PUT('/me/cart/items/{productId}', { params: { path: { productId } }, body: { quantity } })),
    onSuccess,
  })
  const remove = useMutation({
    mutationFn: (productId: number) =>
      call(api.DELETE('/me/cart/items/{productId}', { params: { path: { productId } } })),
    onSuccess,
  })

  return {
    lines: cart.data?.items ?? [],
    total: cart.data?.total ?? 0,
    isPending: enabled && cart.isPending,
    error: cart.error,
    isGuest: false,
    setQuantity: (productId, quantity) => set.mutate({ productId, quantity }),
    remove: (productId) => remove.mutate(productId),
    updating: set.isPending || remove.isPending,
    updateError: set.error ?? remove.error,
  }
}

function useGuestCart(enabled: boolean): CartState {
  const items = useSyncExternalStore(onGuestCartChange, readGuestCart)
  // One public product request per line, cached and shared with the rest of the app
  const products = useQueries({
    queries: (enabled ? items : []).map((item) => ({
      queryKey: ['products', 'detail', item.productId],
      queryFn: () => call(api.GET('/products/{productId}', { params: { path: { productId: item.productId } } })),
    })),
  })

  const lines: CartLine[] = []
  items.forEach((item, index) => {
    const product = products[index]?.data
    if (product) {
      lines.push({
        productId: product.id,
        productName: product.name,
        unitPrice: product.price,
        quantity: item.quantity,
        subtotal: product.price * item.quantity,
        inStock: product.stock >= item.quantity,
      })
    }
  })
  // A product deleted from the catalogue (404) just disappears from the guest cart
  const failed = products.find((query) => query.error && !(query.error instanceof ApiError && query.error.status === 404))

  return {
    lines,
    // Rounded to cents: these are float numbers, unlike the BigDecimal totals of the server
    total: Math.round(lines.reduce((sum, line) => sum + line.subtotal, 0) * 100) / 100,
    isPending: products.some((query) => query.isPending),
    error: failed?.error ?? null,
    isGuest: true,
    setQuantity: setGuestItem,
    remove: removeGuestItem,
    updating: false,
    updateError: null,
  }
}
