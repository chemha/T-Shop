import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { api, call, type Schemas } from '../api/client'
import { ErrorMessage } from '../components/ErrorMessage'
import { OrderCard } from './OrderCard'

export function OrdersPage() {
  const [page, setPage] = useState(0)
  const orders = useQuery({
    queryKey: ['orders', page],
    queryFn: () => call(api.GET('/me/orders', { params: { query: { page, size: 10 } } })),
  })

  return (
    <section>
      <h1>My orders</h1>
      <ErrorMessage error={orders.error} />
      {orders.isPending && <p>Loading…</p>}
      {orders.data?.content.length === 0 && <p>You haven't placed any orders yet.</p>}
      <div className="stack">
        {orders.data?.content.map((order) => (
          <OrderCard
            key={order.id}
            order={order}
            actions={order.status === 'PENDING' && <CancelButton order={order} />}
          />
        ))}
      </div>
      {orders.data && orders.data.page.totalPages > 1 && (
        <div className="pagination">
          <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Newer
          </button>
          <button type="button" disabled={page + 1 >= orders.data.page.totalPages} onClick={() => setPage(page + 1)}>
            Older
          </button>
        </div>
      )}
    </section>
  )
}

function CancelButton({ order }: { order: Schemas['Order'] }) {
  const queryClient = useQueryClient()
  const cancel = useMutation({
    mutationFn: () => call(api.POST('/me/orders/{orderId}/cancel', { params: { path: { orderId: order.id } } })),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] })
      queryClient.invalidateQueries({ queryKey: ['products'] }) // the stock came back
    },
  })

  return (
    <>
      <ErrorMessage error={cancel.error} />
      <button
        type="button"
        className="secondary"
        disabled={cancel.isPending}
        onClick={() => window.confirm(`Cancel order #${order.id}?`) && cancel.mutate()}
      >
        Cancel order
      </button>
    </>
  )
}
