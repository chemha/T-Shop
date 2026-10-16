import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { api, call, type Schemas } from '../api/client'
import { ErrorMessage } from '../components/ErrorMessage'
import { formatPrice } from '../format'
import { cartQuery, useSetCartItem } from './queries'

export function CartPage() {
  const cart = useQuery(cartQuery)

  if (cart.isPending) {
    return <p>Loading…</p>
  }
  if (cart.error) {
    return <ErrorMessage error={cart.error} />
  }
  if (cart.data.items.length === 0) {
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
          {cart.data.items.map((item) => (
            <CartLine key={item.productId} item={item} />
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan={3}>Total</td>
            <td className="price">{formatPrice(cart.data.total)}</td>
            <td />
          </tr>
        </tfoot>
      </table>
      <Checkout />
    </section>
  )
}

function CartLine({ item }: { item: Schemas['CartItem'] }) {
  const queryClient = useQueryClient()
  const setItem = useSetCartItem()
  const remove = useMutation({
    mutationFn: () => call(api.DELETE('/me/cart/items/{productId}', { params: { path: { productId: item.productId } } })),
    onSuccess: (cart) => queryClient.setQueryData(cartQuery.queryKey, cart),
  })

  return (
    <tr>
      <td>
        {item.productName}
        {!item.inStock && <small className="error"> Not enough stock for this quantity</small>}
        <ErrorMessage error={setItem.error ?? remove.error} />
      </td>
      <td>{formatPrice(item.unitPrice)}</td>
      <td>
        <input
          type="number"
          min={1}
          max={99}
          aria-label={`Quantity of ${item.productName}`}
          defaultValue={item.quantity}
          onBlur={(event) => {
            const quantity = Number(event.target.value)
            if (quantity >= 1 && quantity !== item.quantity) {
              setItem.mutate({ productId: item.productId, quantity })
            }
          }}
        />
      </td>
      <td>{formatPrice(item.subtotal)}</td>
      <td>
        <button type="button" className="link" disabled={remove.isPending} onClick={() => remove.mutate()}>
          Remove
        </button>
      </td>
    </tr>
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
        <select name="addressId" required value={addressId ?? addresses.data?.[0]?.id ?? ''} onChange={(event) => setAddressId(Number(event.target.value))}>
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
