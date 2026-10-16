# 0005. Package by feature, references between aggregates by id

## Context

v1 was organised by technical layer (`controllers`, `services`, `repositories`, `entities`...):
everything about orders was spread across six packages, and entities were linked in every
direction (a client loaded its addresses eagerly, an order pointed to client entities, etc.).

## Decision

- Code is grouped by feature: `catalog`, `customer`, `cart`, `order`, `admin`, `auth`, plus
  `shared` for error handling and mapping. Classes are package-private unless another feature
  needs them.
- Entities from different features reference each other **by id**, not with JPA associations:
  `Order`, `Address` and `CartItem` hold a `customerId`. Associations are kept only inside a
  feature (`Product` → `Category`, `Order` → its lines).
- All associations are lazy. Queries that always need related data load it explicitly: an entity
  graph for products and their category, batch fetching for orders and their lines. Tests count
  the SQL statements to catch N+1 problems.
- `spring.jpa.open-in-view` is disabled: all database access happens inside services.

## Consequences

- A feature can be read, changed and tested on its own; its dependencies are visible in its imports.
- Loading is always explicit, so no query happens by accident in a controller or during JSON
  serialization.
- Joining data from two features takes an extra query by ids (e.g. the cart loads its products
  with one `findAllById`).
