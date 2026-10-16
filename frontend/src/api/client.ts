import createClient, { type Middleware } from 'openapi-fetch'
import { clearSession, loadSession } from '../auth/session'
import type { components, paths } from './schema'

export type Schemas = components['schemas']
export type Problem = Schemas['ProblemDetail']

/** An error response from the API, carrying its RFC 9457 problem details. */
export class ApiError extends Error {
  readonly status: number
  readonly problem: Problem | undefined

  constructor(status: number, problem: Problem | undefined) {
    super(problem?.detail ?? `Request failed with status ${status}`)
    this.status = status
    this.problem = problem
  }

  /** Message for a field, from a 400 validation error. */
  fieldError(field: string): string | undefined {
    return this.problem?.errors?.find((error) => error.field === field)?.message
  }
}

const auth: Middleware = {
  onRequest({ request }) {
    const session = loadSession()
    if (session) {
      request.headers.set('Authorization', `Bearer ${session.token}`)
    }
    return request
  },
  onResponse({ request, response }) {
    // The token expired or was rejected: forget it, so the app shows the login again
    if (response.status === 401 && request.headers.has('Authorization')) {
      clearSession()
    }
    return response
  },
}

/**
 * Typed client generated from openapi.yaml: paths, parameters, request bodies and responses are
 * all checked by TypeScript, e.g. api.GET('/products/{productId}', ...) only compiles with a
 * productId path parameter and returns a Product.
 */
export const api = createClient<paths>({
  // Absolute URL on the page's own origin: relative URLs can't build a Request outside a browser (tests)
  baseUrl: `${globalThis.location?.origin ?? ''}/api/v1`,
  // Look fetch up on every call (not once at startup) so tests can replace it
  fetch: (request) => globalThis.fetch(request),
})
api.use(auth)

/** Returns the response body, or throws an ApiError with the problem details. */
export async function call<T>(request: Promise<{ data?: T; error?: unknown; response: Response }>): Promise<T> {
  const { data, error, response } = await request
  if (error !== undefined || !response.ok) {
    throw new ApiError(response.status, error as Problem | undefined)
  }
  return data as T
}
