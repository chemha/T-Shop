import { useState, type ComponentProps, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { api, ApiError, call } from '../api/client'
import { ErrorMessage } from '../components/ErrorMessage'
import { useAuth } from './useAuth'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [error, setError] = useState<unknown>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const email = String(form.get('email'))
    const password = String(form.get('password'))
    setError(null)
    setSubmitting(true)
    try {
      if (mode === 'register') {
        await call(
          api.POST('/auth/register', {
            body: {
              firstName: String(form.get('firstName')),
              lastName: String(form.get('lastName')),
              birthDate: String(form.get('birthDate')),
              email,
              password,
            },
          }),
        )
      }
      await login(email, password)
      // Back to the page that asked for a login, or the catalogue
      navigate((location.state as { from?: string } | null)?.from ?? '/', { replace: true })
    } catch (e) {
      setError(e)
    } finally {
      setSubmitting(false)
    }
  }

  const fieldError = (field: string) => (error instanceof ApiError ? error.fieldError(field) : undefined)

  return (
    <section className="narrow">
      <h1>{mode === 'login' ? 'Log in' : 'Create an account'}</h1>
      <form onSubmit={handleSubmit} className="form">
        {mode === 'register' && (
          <>
            <Field label="First name" name="firstName" error={fieldError('firstName')} />
            <Field label="Last name" name="lastName" error={fieldError('lastName')} />
            <Field label="Birth date" name="birthDate" type="date" error={fieldError('birthDate')} />
          </>
        )}
        <Field label="Email" name="email" type="email" autoComplete="email" error={fieldError('email')} />
        <Field
          label="Password"
          name="password"
          type="password"
          autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          error={fieldError('password')}
        />
        <ErrorMessage error={error} />
        <button type="submit" disabled={submitting}>
          {mode === 'login' ? 'Log in' : 'Create account'}
        </button>
      </form>
      <p>
        {mode === 'login' ? 'New here? ' : 'Already have an account? '}
        <button
          type="button"
          className="link"
          onClick={() => {
            setMode(mode === 'login' ? 'register' : 'login')
            setError(null)
          }}
        >
          {mode === 'login' ? 'Create an account' : 'Log in'}
        </button>
      </p>
      {mode === 'login' && <p className="muted">Demo: test@test.test / test, or admin@admin.admin / admin</p>}
    </section>
  )
}

/**
 * The error sits outside the label and is linked with aria-describedby: screen readers announce
 * "Password, invalid, size must be between 8 and 72" instead of reading it as part of the name.
 */
function Field({ label, error, ...input }: { label: string; error?: string } & ComponentProps<'input'>) {
  const errorId = `${input.name}-error`
  return (
    <div className="field">
      <label>
        {label}
        <input required aria-invalid={error ? true : undefined} aria-describedby={error ? errorId : undefined} {...input} />
      </label>
      {error && (
        <small id={errorId} className="error">
          {error}
        </small>
      )}
    </div>
  )
}
