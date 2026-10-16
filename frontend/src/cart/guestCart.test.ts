import { afterEach, describe, expect, it } from 'vitest'
import { clearGuestCart, readGuestCart, removeGuestItem, setGuestItem } from './guestCart'

describe('guest cart', () => {
  afterEach(() => localStorage.clear())

  it('keeps one line per product, replacing the quantity', () => {
    setGuestItem(1, 2)
    setGuestItem(2, 1)
    setGuestItem(1, 5)

    expect(readGuestCart()).toEqual([
      { productId: 2, quantity: 1 },
      { productId: 1, quantity: 5 },
    ])
  })

  it('removes lines and clears', () => {
    setGuestItem(1, 2)
    setGuestItem(2, 1)

    removeGuestItem(1)
    expect(readGuestCart()).toEqual([{ productId: 2, quantity: 1 }])

    clearGuestCart()
    expect(readGuestCart()).toEqual([])
  })

  it('returns the same array while nothing changes', () => {
    setGuestItem(1, 1)

    expect(readGuestCart()).toBe(readGuestCart())
  })

  it('ignores corrupted or hand-edited storage', () => {
    localStorage.setItem('tshop.guestCart', '{not json')
    expect(readGuestCart()).toEqual([])

    localStorage.setItem('tshop.guestCart', JSON.stringify([{ productId: 1, quantity: -3 }, { productId: 'x' }, { productId: 2, quantity: 1 }]))
    expect(readGuestCart()).toEqual([{ productId: 2, quantity: 1 }])
  })
})
