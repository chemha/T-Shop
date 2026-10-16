import { Route, Routes } from 'react-router'
import { AdminOrdersPage } from './admin/AdminOrdersPage'
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
        <Route
          path="admin/orders"
          element={
            <RequireAuth admin>
              <AdminOrdersPage />
            </RequireAuth>
          }
        />
        <Route path="*" element={<p>Page not found.</p>} />
      </Route>
    </Routes>
  )
}
