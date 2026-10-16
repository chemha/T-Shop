const currency = new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' })

export function formatPrice(amount: number): string {
  return currency.format(amount)
}

const dateTime = new Intl.DateTimeFormat('en-GB', { dateStyle: 'medium', timeStyle: 'short' })

export function formatDateTime(iso: string): string {
  return dateTime.format(new Date(iso))
}
