import { useMutation, useQueryClient } from '@tanstack/react-query'
import { api, call } from '../api/client'

export const cartQuery = {
  queryKey: ['cart'],
  queryFn: () => call(api.GET('/me/cart')),
}

/** Adds a product or changes its quantity. The response is the whole updated cart. */
export function useSetCartItem() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ productId, quantity }: { productId: number; quantity: number }) =>
      call(api.PUT('/me/cart/items/{productId}', { params: { path: { productId } }, body: { quantity } })),
    // Use the returned cart directly instead of fetching it again
    onSuccess: (cart) => queryClient.setQueryData(cartQuery.queryKey, cart),
  })
}
