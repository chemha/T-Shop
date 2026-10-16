import { Navigate, Route, Routes } from 'react-router'
import { AdminCategoriesPage } from './admin/AdminCategoriesPage'
import { AdminCustomersPage } from './admin/AdminCustomersPage'
import { AdminLayout } from './admin/AdminLayout'
import { AdminOrdersPage } from './admin/AdminOrdersPage'
import { AdminProductsPage } from './admin/AdminProductsPage'
import { LoginPage } from './auth/LoginPage'
import { CartPage } from './cart/CartPage'
import { CataloguePage } from './catalog/CataloguePage'
import { Layout } from './components/Layout'
import { RequireAuth } from './components/RequireAuth'
import { OrdersPage } from './orders/OrdersPage'

export function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<CataloguePage />} />
        <Route path="login" element={<LoginPage />} />
        {/* Public: visitors can fill and see a cart, and log in only to place the order */}
        <Route path="cart" element={<CartPage />} />
        <Route
          path="orders"
          element={
            <RequireAuth>
              <OrdersPage />
            </RequireAuth>
          }
        />
        {/* One guard for every admin page. The API checks the role again on every request */}
        <Route
          path="admin"
          element={
            <RequireAuth admin>
              <AdminLayout />
            </RequireAuth>
          }
        >
          <Route index element={<Navigate to="orders" replace />} />
          <Route path="orders" element={<AdminOrdersPage />} />
          <Route path="customers" element={<AdminCustomersPage />} />
          <Route path="products" element={<AdminProductsPage />} />
          <Route path="categories" element={<AdminCategoriesPage />} />
        </Route>
        <Route path="*" element={<p>Page not found.</p>} />
      </Route>
    </Routes>
  )
}
