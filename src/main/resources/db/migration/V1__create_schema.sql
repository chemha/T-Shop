-- Customers and their addresses

CREATE TABLE customer
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    birth_date    DATE         NOT NULL,
    email         VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'ADMIN')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE address
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id  BIGINT       NOT NULL REFERENCES customer (id) ON DELETE CASCADE,
    country      VARCHAR(60)  NOT NULL,
    postal_code  VARCHAR(10)  NOT NULL,
    street       VARCHAR(100) NOT NULL,
    house_number VARCHAR(50),
    apartment    VARCHAR(50)
);

CREATE INDEX idx_address_customer ON address (customer_id);

-- Catalogue

CREATE TABLE category
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE product
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id BIGINT         NOT NULL REFERENCES category (id),
    name        VARCHAR(100)   NOT NULL,
    price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    brand       VARCHAR(50),
    color       VARCHAR(50),
    weight_kg   NUMERIC(10, 2) CHECK (weight_kg >= 0),
    volume_l    NUMERIC(10, 2) CHECK (volume_l >= 0),
    stock       INTEGER        NOT NULL CHECK (stock >= 0),
    -- Optimistic locking: two checkouts can't both take the last unit
    version     BIGINT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_category ON product (category_id);

-- Shopping cart: one row per product in a customer's cart

CREATE TABLE cart_item
(
    customer_id BIGINT      NOT NULL REFERENCES customer (id) ON DELETE CASCADE,
    product_id  BIGINT      NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    quantity    INTEGER     NOT NULL CHECK (quantity > 0),
    added_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (customer_id, product_id)
);

-- Orders. "order" is a reserved word, hence the plural table name.
-- The shipping address and the product name and price are copied into the order,
-- so editing an address or a product never changes past orders.

CREATE TABLE orders
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id           BIGINT         NOT NULL REFERENCES customer (id),
    status                VARCHAR(20)    NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    payment_method        VARCHAR(20)    NOT NULL CHECK (payment_method IN ('CASH', 'CARD')),
    payment_status        VARCHAR(20)    NOT NULL CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED')),
    delivery_method       VARCHAR(20)    NOT NULL CHECK (delivery_method IN ('STANDARD', 'PREMIUM')),
    shipping_country      VARCHAR(60)    NOT NULL,
    shipping_postal_code  VARCHAR(10)    NOT NULL,
    shipping_street       VARCHAR(100)   NOT NULL,
    shipping_house_number VARCHAR(50),
    shipping_apartment    VARCHAR(50),
    total                 NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    version               BIGINT         NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_customer ON orders (customer_id);

CREATE TABLE order_item
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    -- Kept as a plain reference: the product may be deleted later, the order line stays
    product_id   BIGINT         REFERENCES product (id) ON DELETE SET NULL,
    product_name VARCHAR(100)   NOT NULL,
    unit_price   NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    quantity     INTEGER        NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_item_order ON order_item (order_id);
