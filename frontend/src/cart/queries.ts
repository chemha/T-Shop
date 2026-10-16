import { api, call } from '../api/client'

export const cartQuery = {
  queryKey: ['cart'],
  queryFn: () => call(api.GET('/me/cart')),
}
