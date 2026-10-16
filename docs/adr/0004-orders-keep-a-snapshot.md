# 0004. Orders keep a snapshot of prices and address

## Context

In v1 an order line (`good`) only pointed to the product. Changing a product's price changed the
total of every past order, and deleting a product broke them.

## Decision

When an order is placed it copies:

- the product **name and unit price** into each line (`order_item`),
- the **shipping address** into the order (`orders.shipping_*`, mapped as an embedded record).

`order_item.product_id` is still kept for reference, with `ON DELETE SET NULL`. The cart, on the
other hand, stores only quantities and always shows current prices and stock.

## Consequences

- An order is a historical record: it shows what was bought, at what price and where it was sent.
- Some data is duplicated on purpose, a normal trade-off for records that must not change.
- Money is `NUMERIC(10,2)` in the database and `BigDecimal` in Java; floating point is never used
  for prices (v1 used `Float`).
