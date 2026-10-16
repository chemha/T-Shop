import { Link, NavLink, Outlet } from 'react-router'
import { isAdmin } from '../auth/session'
import { useAuth } from '../auth/useAuth'
import { useCart } from '../cart/useCart'

export function Layout() {
  const { session, logout } = useAuth()
  const cart = useCart()
  const items = cart.lines.reduce((sum, line) => sum + line.quantity, 0)

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
          <NavLink to="/cart">Cart{items > 0 && <span className="badge">{items}</span>}</NavLink>
          {session && <NavLink to="/orders">My orders</NavLink>}
          {/* Links to /admin so it stays highlighted on every admin page */}
          {isAdmin(session) && <NavLink to="/admin">Admin</NavLink>}
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
