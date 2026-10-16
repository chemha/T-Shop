import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useEffectEvent, useRef, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { api, call, type Schemas } from '../api/client'
import { useAuth } from '../auth/useAuth'
import { ErrorMessage } from '../components/ErrorMessage'
import { formatPrice } from '../format'
import { cartQuery } from './queries'
import { useCart, type CartLine } from './useCart'

export function CartPage() {
  const { session } = useAuth()
  const cart = useCart()

  if (cart.isPending) {
    return <p>Loading…</p>
  }
  if (cart.error) {
    return <ErrorMessage error={cart.error} />
  }
  if (cart.lines.length === 0) {
    return (
      <section>
        <h1>Cart</h1>
        <p>
          Your cart is empty. <Link to="/">Browse the catalogue</Link>
        </p>
      </section>
    )
  }

  return (
    <section>
      <h1>Cart</h1>
      <table className="table">
        <thead>
          <tr>
            <th>Product</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Subtotal</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {cart.lines.map((line) => (
            <CartRow
              key={line.productId}
              line={line}
              disabled={cart.updating}
              onQuantity={(quantity) => cart.setQuantity(line.productId, quantity)}
              onRemove={() => cart.remove(line.productId)}
            />
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan={3}>Total</td>
            <td className="price">{formatPrice(cart.total)}</td>
            <td />
          </tr>
        </tfoot>
      </table>
      {/* Below the table, so the table doesn't jump when the message appears or goes away */}
      <ErrorMessage error={cart.updateError} />
      {session ? (
        <Checkout />
      ) : (
        <p className="checkout">
          <Link to="/login" state={{ from: '/cart' }}>
            Log in or create an account
          </Link>{' '}
          to place your order. Your cart will be kept.
        </p>
      )}
    </section>
  )
}

function CartRow({
  line,
  disabled,
  onQuantity,
  onRemove,
}: {
  line: CartLine
  disabled: boolean
  onQuantity: (quantity: number) => void
  onRemove: () => void
}) {
  return (
    <tr>
      <td>
        {line.productName}
        {!line.inStock && <small className="error"> Not enough stock for this quantity</small>}
      </td>
      <td>{formatPrice(line.unitPrice)}</td>
      <td>
        {/* The key resets the field whenever the cart's quantity changes (saved, corrected, other tab) */}
        <QuantityInput
          key={line.quantity}
          label={`Quantity of ${line.productName}`}
          quantity={line.quantity}
          onChange={onQuantity}
        />
      </td>
      <td>{formatPrice(line.subtotal)}</td>
      <td>
        <button type="button" className="link" disabled={disabled} onClick={onRemove}>
          Remove
        </button>
      </td>
    </tr>
  )
}

/**
 * Applies the new quantity when the user presses Enter, or a moment after they stop changing it
 * (arrows or typing), so typing "12" sends one update instead of two.
 */
function QuantityInput({
  label,
  quantity,
  onChange,
}: {
  label: string
  quantity: number
  onChange: (quantity: number) => void
}) {
  const [value, setValue] = useState(String(quantity))
  // The same number is never sent twice in a row: if the server rejects it (not enough stock),
  // re-rendering the page to show the error must not send it again
  const lastSent = useRef(quantity)

  function apply() {
    const next = Number(value)
    if (!Number.isInteger(next) || next < 1 || next > 99 || next === lastSent.current) {
      return
    }
    lastSent.current = next
    onChange(next)
  }

  // An Effect Event always sees the latest props and state but isn't a dependency of the effect:
  // the timer restarts only when the value changes, not every time the parent re-renders
  const applyAfterDelay = useEffectEvent(apply)

  useEffect(() => {
    const timer = setTimeout(applyAfterDelay, 400)
    return () => clearTimeout(timer) // a newer change cancels the pending one
  }, [value])

  return (
    <input
      type="number"
      min={1}
      max={99}
      aria-label={label}
      value={value}
      onChange={(event) => setValue(event.target.value)}
      onKeyDown={(event) => event.key === 'Enter' && apply()}
    />
  )
}

function Checkout() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const addresses = useQuery({ queryKey: ['addresses'], queryFn: () => call(api.GET('/me/addresses')) })
  const [addressId, setAddressId] = useState<number>()

  const placeOrder = useMutation({
    mutationFn: (body: Schemas['PlaceOrderRequest']) => call(api.POST('/me/orders', { body })),
    onSuccess: async () => {
      // The order changed the cart, the stock shown in the catalogue and the order list
      await queryClient.invalidateQueries({ queryKey: cartQuery.queryKey })
      await queryClient.invalidateQueries({ queryKey: ['products'] })
      await queryClient.invalidateQueries({ queryKey: ['orders'] })
      navigate('/orders')
    },
  })

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    placeOrder.mutate({
      addressId: Number(form.get('addressId')),
      paymentMethod: form.get('payment') as Schemas['PaymentMethod'],
      deliveryMethod: form.get('delivery') as Schemas['DeliveryMethod'],
    })
  }

  if (addresses.data?.length === 0) {
    return <NewAddress onCreated={setAddressId} />
  }

  return (
    <form className="form checkout" onSubmit={handleSubmit}>
      <h2>Checkout</h2>
      <label>
        Ship to
        <select
          name="addressId"
          required
          value={addressId ?? addresses.data?.[0]?.id ?? ''}
          onChange={(event) => setAddressId(Number(event.target.value))}
        >
          {addresses.data?.map((address) => (
            <option key={address.id} value={address.id}>
              {[address.street, address.houseNumber, address.apartment, address.postalCode, address.country]
                .filter(Boolean)
                .join(', ')}
            </option>
          ))}
        </select>
      </label>
      <fieldset>
        <legend>Payment</legend>
        <label className="inline">
          <input type="radio" name="payment" value="CARD" defaultChecked /> Card
        </label>
        <label className="inline">
          <input type="radio" name="payment" value="CASH" /> Cash on delivery
        </label>
      </fieldset>
      <fieldset>
        <legend>Delivery</legend>
        <label className="inline">
          <input type="radio" name="delivery" value="STANDARD" defaultChecked /> Standard
        </label>
        <label className="inline">
          <input type="radio" name="delivery" value="PREMIUM" /> Premium
        </label>
      </fieldset>
      <ErrorMessage error={placeOrder.error} />
      <button type="submit" disabled={placeOrder.isPending || !addresses.data}>
        Place order
      </button>
    </form>
  )
}

function NewAddress({ onCreated }: { onCreated: (id: number) => void }) {
  const queryClient = useQueryClient()
  const create = useMutation({
    mutationFn: (body: Schemas['AddressDetails']) => call(api.POST('/me/addresses', { body })),
    onSuccess: async (address) => {
      await queryClient.invalidateQueries({ queryKey: ['addresses'] })
      onCreated(address.id)
    },
  })

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    create.mutate({
      street: String(form.get('street')),
      houseNumber: String(form.get('houseNumber')) || undefined,
      apartment: String(form.get('apartment')) || undefined,
      postalCode: String(form.get('postalCode')),
      country: String(form.get('country')),
    })
  }

  return (
    <form className="form checkout" onSubmit={handleSubmit}>
      <h2>Where should we send it?</h2>
      <label>
        Street
        <input name="street" required maxLength={100} />
      </label>
      <div className="row">
        <label>
          Number
          <input name="houseNumber" maxLength={50} />
        </label>
        <label>
          Apartment
          <input name="apartment" maxLength={50} />
        </label>
      </div>
      <div className="row">
        <label>
          Postal code
          <input name="postalCode" required maxLength={10} />
        </label>
        <label>
          Country
          <input name="country" required maxLength={60} />
        </label>
      </div>
      <ErrorMessage error={create.error} />
      <button type="submit" disabled={create.isPending}>
        Save address
      </button>
    </form>
  )
}
