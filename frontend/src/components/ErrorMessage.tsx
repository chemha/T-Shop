import { ApiError } from '../api/client'

/** Shows an API problem's "detail", or a generic message for network and unexpected errors. */
export function ErrorMessage({ error }: { error: unknown }) {
  if (!error) {
    return null
  }
  const message = error instanceof ApiError ? error.message : 'Something went wrong. Please try again.'
  return (
    <p role="alert" className="error">
      {message}
    </p>
  )
}
