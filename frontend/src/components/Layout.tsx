import { useQuery } from '@tanstack/react-query'
import { Link, NavLink, Outlet } from 'react-router'
import { useAuth } from '../auth/useAuth'
import { isAdmin } from '../auth/session'
import { cartQuery } from '../cart/queries'

export function Layout() {
  const { session, logout } = useAuth()
  const cart = useQuery({ ...cartQuery, enabled: session !== null })
  const items = cart.data?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0

  return (
    <>
      <header className="header">
        <Link to="/" className="brand">
          T-Shop
        </Link>
        <nav>
          <NavLink to="/" end>
            Catalogue
          </NavLink>
          {session && (
            <>
              <NavLink to="/cart">Cart{items > 0 && <span className="badge">{items}</span>}</NavLink>
              <NavLink to="/orders">My orders</NavLink>
            </>
          )}
          {isAdmin(session) && <NavLink to="/admin/orders">Admin</NavLink>}
          {session ? (
            <button type="button" className="link" onClick={logout}>
              Log out
            </button>
          ) : (
            <NavLink to="/login">Log in</NavLink>
          )}
        </nav>
      </header>
      <main className="main">
        <Outlet />
      </main>
    </>
  )
}
