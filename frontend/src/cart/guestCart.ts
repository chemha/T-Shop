/**
 * The cart of a visitor who hasn't logged in, kept in localStorage: it survives closing the
 * browser, like the session cookie of the v1 shop did while the session lasted. Only product ids
 * and quantities are stored; names, prices and stock are always read from the catalogue.
 * When the visitor logs in, it is merged into their cart on the server (POST /me/cart/merge).
 */

const STORAGE_KEY = 'tshop.guestCart'

export type GuestItem = { productId: number; quantity: number }

const listeners = new Set<() => void>()
let cachedRaw: string | null = null
let cachedItems: GuestItem[] = []

/** Same array instance while the stored value doesn't change, as useSyncExternalStore requires. */
export function readGuestCart(): GuestItem[] {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (raw !== cachedRaw) {
    cachedRaw = raw
    cachedItems = parse(raw)
  }
  return cachedItems
}

export function setGuestItem(productId: number, quantity: number): void {
  const others = readGuestCart().filter((item) => item.productId !== productId)
  write(quantity > 0 ? [...others, { productId, quantity }] : others)
}

export function removeGuestItem(productId: number): void {
  setGuestItem(productId, 0)
}

export function clearGuestCart(): void {
  localStorage.removeItem(STORAGE_KEY)
  listeners.forEach((listener) => listener())
}

export function onGuestCartChange(listener: () => void): () => void {
  listeners.add(listener)
  // Also follow changes made in other tabs
  const onStorage = (event: StorageEvent) => event.key === STORAGE_KEY && listener()
  window.addEventListener('storage', onStorage)
  return () => {
    listeners.delete(listener)
    window.removeEventListener('storage', onStorage)
  }
}

function write(items: GuestItem[]): void {
  if (items.length === 0) {
    clearGuestCart()
    return
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
  listeners.forEach((listener) => listener())
}

/** Anything in localStorage could have been edited by hand: keep only well-formed lines. */
function parse(raw: string | null): GuestItem[] {
  if (!raw) {
    return []
  }
  try {
    const value: unknown = JSON.parse(raw)
    return Array.isArray(value)
      ? value.filter(
          (item): item is GuestItem =>
            Number.isInteger(item?.productId) && Number.isInteger(item?.quantity) && item.quantity > 0,
        )
      : []
  } catch {
    return []
  }
}
