import { useId, type FormEvent } from 'react'

/**
 * A search field with its button. Being a form, both Enter and the button submit it, and
 * role="search" lets screen readers jump straight to it.
 */
export function SearchBox({
  initial = '',
  placeholder,
  onSearch,
}: {
  initial?: string
  placeholder?: string
  onSearch: (text: string) => void
}) {
  const id = useId()

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onSearch(String(new FormData(event.currentTarget).get('q')).trim())
  }

  return (
    <form role="search" className="filter filter-search" onSubmit={handleSubmit}>
      <label htmlFor={id}>Search</label>
      <span className="search-row">
        <input id={id} name="q" type="search" placeholder={placeholder} defaultValue={initial} />
        <button type="submit">Search</button>
      </span>
    </form>
  )
}
