import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { api, call, type Schemas } from '../api/client'
import { useAuth } from '../auth/useAuth'
import { ErrorMessage } from '../components/ErrorMessage'
import { formatDateTime } from '../format'

type Customer = Schemas['Customer']

export function AdminCustomersPage() {
  const [page, setPage] = useState(0)
  const customers = useQuery({
    queryKey: ['admin-customers', page],
    queryFn: () => call(api.GET('/admin/customers', { params: { query: { page, size: 20 } } })),
    placeholderData: keepPreviousData,
  })

  return (
    <section>
      <h1>Customers</h1>
      <p className="muted">
        New admins register like any customer and are then given the admin role here, so nobody chooses a password
        for someone else.
      </p>
      <ErrorMessage error={customers.error} />
      {customers.isPending && <p>Loading…</p>}
      {customers.data && (
        <table className="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Birth date</th>
              <th>Registered</th>
              <th>Role</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {customers.data.content.map((customer) => (
              <CustomerRow key={customer.id} customer={customer} />
            ))}
          </tbody>
        </table>
      )}
      {customers.data && customers.data.page.totalPages > 1 && (
        <div className="pagination">
          <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </button>
          <span>
            Page {page + 1} of {customers.data.page.totalPages}
          </span>
          <button type="button" disabled={page + 1 >= customers.data.page.totalPages} onClick={() => setPage(page + 1)}>
            Next
          </button>
        </div>
      )}
    </section>
  )
}

function CustomerRow({ customer }: { customer: Customer }) {
  const { session } = useAuth()
  const queryClient = useQueryClient()
  // The server also refuses these two on yourself; hiding them just avoids a pointless error
  const isMe = session?.customerId === customer.id
  const refresh = () => queryClient.invalidateQueries({ queryKey: ['admin-customers'] })

  const changeRole = useMutation({
    mutationFn: (role: Schemas['Role']) =>
      call(api.PUT('/admin/customers/{customerId}/role', { params: { path: { customerId: customer.id } }, body: { role } })),
    onSuccess: refresh,
  })
  const remove = useMutation({
    mutationFn: () => call(api.DELETE('/admin/customers/{customerId}', { params: { path: { customerId: customer.id } } })),
    onSuccess: refresh,
  })
  const busy = changeRole.isPending || remove.isPending
  const fullName = `${customer.firstName} ${customer.lastName}`

  return (
    <tr>
      <td>
        {fullName}
        {isMe && <span className="muted"> (you)</span>}
        <ErrorMessage error={changeRole.error ?? remove.error} />
      </td>
      <td>{customer.email}</td>
      <td>{customer.birthDate}</td>
      <td>{formatDateTime(customer.createdAt)}</td>
      <td>{customer.role === 'ADMIN' ? 'Admin' : 'Customer'}</td>
      <td>
        {!isMe && (
          <span className="actions">
            <button
              type="button"
              disabled={busy}
              onClick={() => changeRole.mutate(customer.role === 'ADMIN' ? 'CUSTOMER' : 'ADMIN')}
            >
              {customer.role === 'ADMIN' ? 'Remove admin' : 'Make admin'}
            </button>
            <button
              type="button"
              className="secondary"
              disabled={busy}
              onClick={() => window.confirm(`Delete ${fullName}? This can't be undone.`) && remove.mutate()}
            >
              Delete
            </button>
          </span>
        )}
      </td>
    </tr>
  )
}
