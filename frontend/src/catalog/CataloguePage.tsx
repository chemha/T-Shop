import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router'
import { api, call, type Schemas } from '../api/client'
import { useCart } from '../cart/useCart'
import { ErrorMessage } from '../components/ErrorMessage'
import { SearchBox } from '../components/SearchBox'
import { formatPrice } from '../format'

type Sort = 'name' | 'price_asc' | 'price_desc' | 'newest'

const PAGE_SIZE = 9

export function CataloguePage() {
  // Filters live in the URL: searches can be shared and the back button works
  const [params, setParams] = useSearchParams()
  const q = params.get('q') ?? ''
  const categoryId = params.get('category') ? Number(params.get('category')) : undefined
  const sort = (params.get('sort') ?? 'name') as Sort
  const page = Number(params.get('page') ?? 0)

  const categories = useQuery({
    queryKey: ['categories'],
    queryFn: () => call(api.GET('/categories')),
    staleTime: 5 * 60_000,
  })
  const products = useQuery({
    queryKey: ['products', { q, categoryId, sort, page }],
    queryFn: () =>
      call(api.GET('/products', { params: { query: { q: q || undefined, categoryId, sort, page, size: PAGE_SIZE } } })),
    // Keep showing the current page while the next one loads, instead of flashing empty
    placeholderData: keepPreviousData,
  })

  function update(changes: Record<string, string | undefined>) {
    const next = new URLSearchParams(params)
    for (const [key, value] of Object.entries(changes)) {
      if (value) {
        next.set(key, value)
      } else {
        next.delete(key)
      }
    }
    if (!('page' in changes)) {
      next.delete('page') // a new filter starts again at the first page
    }
    setParams(next)
  }

  return (
    <section>
      <h1>Catalogue</h1>
      <div className="filters">
        <SearchBox
          key={q} // reset the text when the URL changes (e.g. back button)
          initial={q}
          placeholder="Name or brand"
          onSearch={(text) => update({ q: text })}
        />
        <label className="filter">
          Category
          <select value={categoryId ?? ''} onChange={(event) => update({ category: event.target.value })}>
            <option value="">All categories</option>
            {categories.data?.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
        <label className="filter">
          Sort by
          <select value={sort} onChange={(event) => update({ sort: event.target.value })}>
            <option value="name">Name</option>
            <option value="price_asc">Price: low to high</option>
            <option value="price_desc">Price: high to low</option>
            <option value="newest">Newest</option>
          </select>
        </label>
      </div>

      <ErrorMessage error={products.error} />
      {products.isPending && <p>Loading…</p>}
      {products.data && products.data.content.length === 0 && <p>No products match your search.</p>}

      <div className="grid">
        {products.data?.content.map((product) => <ProductCard key={product.id} product={product} />)}
      </div>

      {products.data && products.data.page.totalPages > 1 && (
        <div className="pagination">
          <button type="button" disabled={page === 0} onClick={() => update({ page: String(page - 1) })}>
            Previous
          </button>
          <span>
            Page {page + 1} of {products.data.page.totalPages}
          </span>
          <button
            type="button"
            disabled={page + 1 >= products.data.page.totalPages}
            onClick={() => update({ page: String(page + 1) })}
          >
            Next
          </button>
        </div>
      )}
    </section>
  )
}

function ProductCard({ product }: { product: Schemas['Product'] }) {
  // Logged in or not: visitors fill a guest cart and only log in to place the order
  const cart = useCart()
  // Setting a quantity replaces it, so adding one more needs the current one
  const inCart = cart.lines.find((line) => line.productId === product.id)?.quantity ?? 0

  return (
    <article className="card">
      <p className="muted">{product.categoryName}</p>
      <h2>{product.name}</h2>
      <p className="muted">{[product.brand, product.color].filter(Boolean).join(' · ')}</p>
      <p className="price">{formatPrice(product.price)}</p>
      <p className="muted">{product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}</p>
      <button
        type="button"
        disabled={inCart >= product.stock || cart.updating}
        onClick={() => cart.setQuantity(product.id, inCart + 1)}
      >
        {inCart > 0 ? `In cart (${inCart}) · Add one more` : 'Add to cart'}
      </button>
      <ErrorMessage error={cart.updateError} />
    </article>
  )
}
