import type { Schemas } from '../api/client'

export type OrderStatus = Schemas['OrderStatus']

/**
 * Same transitions as OrderStatus.canMoveTo on the server, used only to decide which buttons to
 * show. The server still validates every change and answers 409 to an invalid one.
 */
const NEXT: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ['CONFIRMED', 'CANCELLED'],
  CONFIRMED: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: [],
}

export function nextStatuses(status: OrderStatus): OrderStatus[] {
  return NEXT[status]
}
