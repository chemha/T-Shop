# 0003. Optimistic locking for stock, with retries

## Context

In v1 the checkout read the stock, compared it with the cart and then wrote the new stock in a
separate step, outside a single transaction. Two customers buying the last unit at the same time
could both succeed, leaving negative stock, and a failure halfway left a partial order behind.

## Decision

- The whole checkout (reserve stock, create the order with its lines, empty the cart) runs in
  **one transaction**: it happens completely or not at all.
- `product` has a `version` column (`@Version`). Every update runs
  `UPDATE ... WHERE id = ? AND version = ?`; if another transaction changed the row first, no row
  matches and the transaction fails instead of overwriting the other purchase.
- A failed checkout is **retried up to three times** with fresh data. The retry loop sits outside
  the transaction, because a failed transaction can't be reused. On retry, a customer who lost the
  race gets the real answer ("only 0 units left") instead of a technical error.
- `CHECK (stock >= 0)` in the database is the last line of defence.

`CheckoutConcurrencyTests` starts several checkouts at the same instant on virtual threads and
verifies that exactly as many succeed as there are units.

## Alternatives considered

- **Pessimistic locking** (`SELECT ... FOR UPDATE`): simpler to reason about, but checkouts of
  the same product always wait for each other, even when there's plenty of stock, and rows must be
  locked in a consistent order to avoid deadlocks between carts with several products.
- **Atomic update** (`UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?`): very
  fast, but moves the business rule into SQL and bypasses the entity.

Conflicts are rare in a small shop, so optimistic locking costs nothing in the normal case.

## Consequences

- Under very high contention on one product (a flash sale), many retries would fail; the client
  then receives a 409 asking to try again.
