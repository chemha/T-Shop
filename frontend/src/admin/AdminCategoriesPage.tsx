import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { api, call, type Schemas } from '../api/client'
import { categoriesQuery } from '../catalog/queries'
import { ErrorMessage } from '../components/ErrorMessage'

export function AdminCategoriesPage() {
  const queryClient = useQueryClient()
  const categories = useQuery(categoriesQuery)
  const create = useMutation({
    mutationFn: (name: string) => call(api.POST('/categories', { body: { name } })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: categoriesQuery.queryKey }),
  })

  function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    create.mutate(String(new FormData(form).get('name')), { onSuccess: () => form.reset() })
  }

  return (
    <section>
      <h1>Categories</h1>
      <form className="inline-form" onSubmit={handleCreate}>
        <label className="filter">
          New category
          <input name="name" required maxLength={50} />
        </label>
        <button type="submit" disabled={create.isPending}>
          Add
        </button>
      </form>
      <ErrorMessage error={create.error} />

      <ErrorMessage error={categories.error} />
      <table className="table">
        <thead>
          <tr>
            <th>Name</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {categories.data?.map((category) => (
            <CategoryRow key={category.id} category={category} />
          ))}
        </tbody>
      </table>
    </section>
  )
}

function CategoryRow({ category }: { category: Schemas['Category'] }) {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: categoriesQuery.queryKey })
    queryClient.invalidateQueries({ queryKey: ['products'] }) // products show their category name
  }

  const rename = useMutation({
    mutationFn: (name: string) =>
      call(api.PUT('/categories/{categoryId}', { params: { path: { categoryId: category.id } }, body: { name } })),
    onSuccess: () => {
      setEditing(false)
      refresh()
    },
  })
  const remove = useMutation({
    mutationFn: () => call(api.DELETE('/categories/{categoryId}', { params: { path: { categoryId: category.id } } })),
    onSuccess: refresh,
  })

  function handleRename(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    rename.mutate(String(new FormData(event.currentTarget).get('name')))
  }

  return (
    <tr>
      <td>
        {editing ? (
          <form className="inline-form" onSubmit={handleRename}>
            <input name="name" aria-label={`New name for ${category.name}`} required maxLength={50} defaultValue={category.name} autoFocus />
            <button type="submit" disabled={rename.isPending}>
              Save
            </button>
            <button type="button" className="link" onClick={() => setEditing(false)}>
              Cancel
            </button>
          </form>
        ) : (
          category.name
        )}
        {/* e.g. 409 "The category 'Laptops' still has products" */}
        <ErrorMessage error={rename.error ?? remove.error} />
      </td>
      <td>
        {!editing && (
          <span className="actions">
            <button type="button" onClick={() => setEditing(true)}>
              Rename
            </button>
            <button
              type="button"
              className="secondary"
              disabled={remove.isPending}
              onClick={() => window.confirm(`Delete the category "${category.name}"?`) && remove.mutate()}
            >
              Delete
            </button>
          </span>
        )}
      </td>
    </tr>
  )
}
