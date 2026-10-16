import type { ReactNode } from 'react'
import type { Schemas } from '../api/client'
import { formatDateTime, formatPrice } from '../format'

export function OrderCard({ order, actions }: { order: Schemas['Order']; actions?: ReactNode }) {
  const address = order.shippingAddress
  return (
    <article className="card order">
      <header>
        <h2>Order #{order.id}</h2>
        <span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span>
      </header>
      <p className="muted">
        {formatDateTime(order.createdAt)} · {order.paymentMethod} ({order.paymentStatus}) · {order.deliveryMethod}{' '}
        delivery
      </p>
      <p className="muted">
        Ship to {[address.street, address.houseNumber, address.postalCode, address.country].filter(Boolean).join(', ')}
      </p>
      <ul>
        {order.items.map((item, index) => (
          <li key={index}>
            {item.quantity} × {item.productName} — {formatPrice(item.subtotal)}
          </li>
        ))}
      </ul>
      <footer>
        <strong className="price">{formatPrice(order.total)}</strong>
        {actions}
      </footer>
    </article>
  )
}
