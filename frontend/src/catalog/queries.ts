import { api, call } from '../api/client'

export const categoriesQuery = {
  queryKey: ['categories'],
  queryFn: () => call(api.GET('/categories')),
}
