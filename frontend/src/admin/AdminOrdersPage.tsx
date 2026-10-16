import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { api, call, type Schemas } from '../api/client'
import { ErrorMessage } from '../components/ErrorMessage'
import { OrderCard } from '../orders/OrderCard'
import { nextStatuses, type OrderStatus } from '../orders/orderStatus'

const STATUSES: OrderStatus[] = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED']

export function AdminOrdersPage() {
  const [status, setStatus] = useState<OrderStatus | undefined>('PENDING')
  const [page, setPage] = useState(0)
  const orders = useQuery({
    queryKey: ['admin-orders', status, page],
    queryFn: () => call(api.GET('/admin/orders', { params: { query: { status, page, size: 10 } } })),
  })

  return (
    <section>
      <h1>Orders</h1>
      <div className="filters">
        <select
          aria-label="Status"
          value={status ?? ''}
          onChange={(event) => {
            setStatus((event.target.value || undefined) as OrderStatus | undefined)
            setPage(0)
          }}
        >
          <option value="">All statuses</option>
          {STATUSES.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>
      </div>
      <ErrorMessage error={orders.error} />
      {orders.data?.content.length === 0 && <p>No orders.</p>}
      <div className="stack">
        {orders.data?.content.map((order) => (
          <OrderCard key={order.id} order={order} actions={<StatusButtons order={order} />} />
        ))}
      </div>
      {orders.data && orders.data.page.totalPages > 1 && (
        <div className="pagination">
          <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </button>
          <button type="button" disabled={page + 1 >= orders.data.page.totalPages} onClick={() => setPage(page + 1)}>
            Next
          </button>
        </div>
      )}
    </section>
  )
}

function StatusButtons({ order }: { order: Schemas['Order'] }) {
  const queryClient = useQueryClient()
  const change = useMutation({
    mutationFn: (next: OrderStatus) =>
      call(api.PUT('/admin/orders/{orderId}/status', { params: { path: { orderId: order.id } }, body: { status: next } })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-orders'] }),
  })

  return (
    <span className="actions">
      <ErrorMessage error={change.error} />
      {nextStatuses(order.status).map((next) => (
        <button
          key={next}
          type="button"
          className={next === 'CANCELLED' ? 'secondary' : undefined}
          disabled={change.isPending}
          onClick={() => change.mutate(next)}
        >
          {next === 'CANCELLED' ? 'Cancel' : `Mark ${next.toLowerCase()}`}
        </button>
      ))}
    </span>
  )
}
