import { NavLink, Outlet } from 'react-router'

/** Sub-navigation shared by every admin page. */
export function AdminLayout() {
  return (
    <>
      <nav className="subnav" aria-label="Admin">
        <NavLink to="/admin/orders">Orders</NavLink>
        <NavLink to="/admin/customers">Customers</NavLink>
        <NavLink to="/admin/products">Products</NavLink>
        <NavLink to="/admin/categories">Categories</NavLink>
      </nav>
      <Outlet />
    </>
  )
}
