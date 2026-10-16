import { describe, expect, it } from 'vitest'
import { nextStatuses } from './orderStatus'

// Must match OrderStatus.canMoveTo on the server (see OrderStatusTests.java)
describe('nextStatuses', () => {
  it.each([
    ['PENDING', ['CONFIRMED', 'CANCELLED']],
    ['CONFIRMED', ['SHIPPED', 'CANCELLED']],
    ['SHIPPED', ['DELIVERED']],
    ['DELIVERED', []],
    ['CANCELLED', []],
  ] as const)('%s → %j', (status, expected) => {
    expect(nextStatuses(status)).toEqual(expected)
  })
})
