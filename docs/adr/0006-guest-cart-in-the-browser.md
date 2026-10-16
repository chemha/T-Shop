# 0006. Guest cart in the browser, merged at login

## Context

In v1, visitors could add products to the cart without an account: the cart lived in the server's
HTTP session, identified by the `JSESSIONID` cookie. v2 first required logging in for any cart
action, because the API is stateless (see [0002](0002-stateless-jwt-authentication.md)) and the
cart is stored per customer. That made the shop worse for visitors: most shops let people fill a
cart and only ask for an account at checkout.

## Decision

- Visitors can add products and see their cart. Logging in or registering is only required to
  place the order.
- The guest cart is kept **by the client** in `localStorage`: only product ids and quantities.
  Names, prices and stock are read from the public catalogue, so they are always current.
- Right after logging in (or registering), the client sends it to `POST /me/cart/merge`. Quantities
  are added to the customer's existing cart, capped at the available stock, and products that no
  longer exist or are sold out are skipped. Nothing is rejected, because the visitor can't fix
  anything at that point. The client then empties the guest cart.

## Alternatives considered

- **Anonymous carts on the server**, identified by a cart id in a cookie or header. It brings back
  server-side state for people who may never return, needs a job to clean up abandoned carts, and
  a cookie would reintroduce the CSRF concerns that the token-based API avoids.
- **Sending each guest line with `PUT /me/cart/items/{id}`** after login. One request per line,
  not atomic, and a line over the stock would fail with 409 instead of being adjusted.

## Consequences

- The backend stays stateless; the guest cart costs one small endpoint.
- The guest cart lives in one browser. It survives closing the browser, but isn't shared across
  devices until the visitor logs in.
- The guest cart computes its total in the browser with floating-point numbers, only for display.
  The order total is always calculated by the server with `BigDecimal`.
