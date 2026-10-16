import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent, type ReactNode } from 'react'
import { api, ApiError, call, type Schemas } from '../api/client'
import { categoriesQuery } from '../catalog/queries'
import { ErrorMessage } from '../components/ErrorMessage'
import { formatPrice } from '../format'

type Product = Schemas['Product']
type ProductRequest = Schemas['ProductRequest']

/** Which form is open: none, a new product, or one being edited. */
type Editing = null | 'new' | Product

export function AdminProductsPage() {
  const [page, setPage] = useState(0)
  const [q, setQ] = useState('')
  const [editing, setEditing] = useState<Editing>(null)
  const products = useQuery({
    queryKey: ['products', 'admin', { q, page }],
    queryFn: () => call(api.GET('/products', { params: { query: { q: q || undefined, page, size: 20 } } })),
    placeholderData: keepPreviousData,
  })

  return (
    <section>
      <h1>Products</h1>
      <div className="filters">
        <label className="filter filter-search">
          Search
          <input
            type="search"
            placeholder="Name or brand, then Enter"
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                setQ(event.currentTarget.value)
                setPage(0)
              }
            }}
          />
        </label>
        <button type="button" onClick={() => setEditing('new')}>
          New product
        </button>
      </div>

      {editing && (
        <ProductForm
          // A new key per product resets the form's fields when switching products
          key={editing === 'new' ? 'new' : editing.id}
          product={editing === 'new' ? undefined : editing}
          onDone={() => setEditing(null)}
        />
      )}

      <ErrorMessage error={products.error} />
      {products.data && (
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Category</th>
              <th>Price</th>
              <th>Stock</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {products.data.content.map((product) => (
              <ProductRow key={product.id} product={product} onEdit={() => setEditing(product)} />
            ))}
          </tbody>
        </table>
      )}
      {products.data && products.data.page.totalPages > 1 && (
        <div className="pagination">
          <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </button>
          <span>
            Page {page + 1} of {products.data.page.totalPages}
          </span>
          <button type="button" disabled={page + 1 >= products.data.page.totalPages} onClick={() => setPage(page + 1)}>
            Next
          </button>
        </div>
      )}
    </section>
  )
}

function ProductRow({ product, onEdit }: { product: Product; onEdit: () => void }) {
  const queryClient = useQueryClient()
  const remove = useMutation({
    mutationFn: () => call(api.DELETE('/products/{productId}', { params: { path: { productId: product.id } } })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['products'] }),
  })

  return (
    <tr>
      <td>
        {product.name}
        {product.brand && <span className="muted"> · {product.brand}</span>}
        <ErrorMessage error={remove.error} />
      </td>
      <td>{product.categoryName}</td>
      <td>{formatPrice(product.price)}</td>
      <td>{product.stock}</td>
      <td>
        <span className="actions">
          <button type="button" onClick={onEdit}>
            Edit
          </button>
          <button
            type="button"
            className="secondary"
            disabled={remove.isPending}
            onClick={() =>
              window.confirm(`Delete "${product.name}"? Past orders keep its name and price.`) && remove.mutate()
            }
          >
            Delete
          </button>
        </span>
      </td>
    </tr>
  )
}

function ProductForm({ product, onDone }: { product?: Product; onDone: () => void }) {
  const queryClient = useQueryClient()
  const categories = useQuery(categoriesQuery)
  const save = useMutation({
    mutationFn: (body: ProductRequest) =>
      product
        ? call(api.PUT('/products/{productId}', { params: { path: { productId: product.id } }, body }))
        : call(api.POST('/products', { body })),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['products'] })
      onDone()
    },
  })

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const text = (name: string) => String(form.get(name)).trim() || undefined
    const number = (name: string) => (text(name) === undefined ? undefined : Number(form.get(name)))
    save.mutate({
      categoryId: Number(form.get('categoryId')),
      name: String(form.get('name')),
      price: Number(form.get('price')),
      stock: Number(form.get('stock')),
      brand: text('brand'),
      color: text('color'),
      weightKg: number('weightKg'),
      volumeL: number('volumeL'),
    })
  }

  const fieldError = (field: string) => (save.error instanceof ApiError ? save.error.fieldError(field) : undefined)

  return (
    <form className="form card product-form" onSubmit={handleSubmit}>
      <h2>{product ? `Edit ${product.name}` : 'New product'}</h2>
      <div className="row">
        <Field label="Name" error={fieldError('name')}>
          <input name="name" required maxLength={100} defaultValue={product?.name} />
        </Field>
        <Field label="Category" error={fieldError('categoryId')}>
          <select name="categoryId" required defaultValue={product?.categoryId ?? ''}>
            <option value="" disabled>
              Choose…
            </option>
            {categories.data?.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </Field>
      </div>
      <div className="row">
        <Field label="Price (€)" error={fieldError('price')}>
          <input name="price" type="number" required min={0} step={0.01} defaultValue={product?.price} />
        </Field>
        <Field label="Stock" error={fieldError('stock')}>
          <input name="stock" type="number" required min={0} step={1} defaultValue={product?.stock} />
        </Field>
      </div>
      <div className="row">
        <Field label="Brand" error={fieldError('brand')}>
          <input name="brand" maxLength={50} defaultValue={product?.brand} />
        </Field>
        <Field label="Color" error={fieldError('color')}>
          <input name="color" maxLength={50} defaultValue={product?.color} />
        </Field>
      </div>
      <div className="row">
        <Field label="Weight (kg)" error={fieldError('weightKg')}>
          <input name="weightKg" type="number" min={0} step={0.01} defaultValue={product?.weightKg} />
        </Field>
        <Field label="Volume (l)" error={fieldError('volumeL')}>
          <input name="volumeL" type="number" min={0} step={0.01} defaultValue={product?.volumeL} />
        </Field>
      </div>
      <ErrorMessage error={save.error} />
      <span className="actions">
        <button type="submit" disabled={save.isPending}>
          {product ? 'Save changes' : 'Create product'}
        </button>
        <button type="button" className="link" onClick={onDone}>
          Cancel
        </button>
      </span>
    </form>
  )
}

function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <div className="field">
      <label>
        {label}
        {children}
      </label>
      {error && <small className="error">{error}</small>}
    </div>
  )
}
