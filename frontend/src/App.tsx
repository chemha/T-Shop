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
        <Route
          path="cart"
          element={
            <RequireAuth>
              <CartPage />
            </RequireAuth>
          }
        />
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
