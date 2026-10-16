-- Demo data for local development. Not loaded with the prod profile.
-- A repeatable migration runs after the versioned ones, so it always sees the latest schema.
-- It only inserts into an empty database, so re-running it is harmless.
--
-- Passwords: admin@admin.admin -> admin, test@test.test -> test, everyone else -> password

DO
$$
    BEGIN
        IF EXISTS (SELECT 1 FROM customer) THEN
            RETURN;
        END IF;

        INSERT INTO customer (first_name, last_name, birth_date, email, password_hash, role)
        VALUES ('Admin', 'Admin', '2000-01-01', 'admin@admin.admin', '$2a$10$WmmU6.wZnniP524nKOCRy.CsHe6.k109..ezFI9OiidmsRYIR/3aW', 'ADMIN'),
               ('Test', 'Test', '2000-01-01', 'test@test.test', '$2a$10$2.3RP8B0uhCx8.vRPo7oQuyB8mNBK7L1I7zmiZ7scH4Tq37fSfSsu', 'CUSTOMER'),
               ('John', 'Doe', '1990-01-01', 'john.doe@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER'),
               ('Jane', 'Doe', '1992-05-10', 'jane.doe@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER'),
               ('Mike', 'Smith', '1985-12-25', 'mike.smith@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER'),
               ('Samantha', 'Johnson', '1998-07-20', 'samantha.johnson@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER'),
               ('David', 'Brown', '1995-09-15', 'david.brown@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER'),
               ('Sarah', 'Jones', '1994-03-12', 'sarah.jones@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER'),
               ('Robert', 'Lee', '1988-11-07', 'robert.lee@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER'),
               ('Emily', 'Wilson', '2000-02-18', 'emily.wilson@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER');

        INSERT INTO address (customer_id, country, postal_code, street, house_number, apartment)
        SELECT c.id, a.country, a.postal_code, a.street, a.house_number, a.apartment
        FROM (VALUES ('admin@admin.admin', 'USA', '10001', '5th Ave', '20', '10A'),
                     ('test@test.test', 'USA', '10002', 'Broadway', '25', '8B'),
                     ('john.doe@example.com', 'Canada', 'M5J 2T3', 'Queens Quay W', '15', NULL),
                     ('jane.doe@example.com', 'UK', 'EC1A 1BB', 'Cheapside', '30', '5C'),
                     ('mike.smith@example.com', 'Australia', '2000', 'George St', '40', '12A'),
                     ('samantha.johnson@example.com', 'USA', '10003', 'Union Square E', '35', '9D'),
                     ('david.brown@example.com', 'UK', 'SW1A 1AA', 'Whitehall', '10', '3B'),
                     ('sarah.jones@example.com', 'Canada', 'V6C 3G1', 'Hastings St W', '5', '6A'),
                     ('robert.lee@example.com', 'USA', '10004', 'Broad St', '45', '7C'),
                     ('emily.wilson@example.com', 'Australia', '3000', 'Swanston St', '50', NULL))
                 AS a (email, country, postal_code, street, house_number, apartment)
                 JOIN customer c ON c.email = a.email;

        INSERT INTO category (name)
        VALUES ('Laptops'),
               ('Desktops'),
               ('Tablets'),
               ('Printers'),
               ('Monitors'),
               ('Accessories');

        INSERT INTO product (category_id, name, price, brand, color, weight_kg, volume_l, stock)
        SELECT c.id, p.name, p.price, p.brand, p.color, p.weight_kg, p.volume_l, p.stock
        FROM (VALUES ('Laptops', 'Dell XPS 13', 1099.99, 'Dell', 'Silver', 1.27, 11.64, 10),
                     ('Laptops', 'MacBook Pro', 1799.99, 'Apple', 'Space Gray', 1.4, 14.9, 5),
                     ('Desktops', 'HP Pavilion Desktop', 599.99, 'HP', 'Black', 5.15, 13.28, 7),
                     ('Desktops', 'Alienware Aurora R12', 1999.99, 'Alienware', 'Lunar Light', 14.94, 28.2, 3),
                     ('Tablets', 'Microsoft Surface Pro 7', 899.99, 'Microsoft', 'Platinum', 0.77, 5.3, 8),
                     ('Tablets', 'Samsung Galaxy Tab S7+', 849.99, 'Samsung', 'Mystic Black', 0.91, 6.44, 6),
                     ('Printers', 'HP OfficeJet Pro 9025e', 349.99, 'HP', 'White', 8.2, 14.45, 4),
                     ('Printers', 'Canon PIXMA TR4520', 99.99, 'Canon', 'Black', 5.4, 11.7, 9),
                     ('Monitors', 'Dell UltraSharp U2720Q', 719.99, 'Dell', 'Black', 13.6, 15.61, 2),
                     ('Monitors', 'ASUS TUF Gaming VG27AQ', 499.99, 'ASUS', 'Black', 8.8, 16.92, 6),
                     ('Accessories', 'Logitech MX Master 3', 99.99, 'Logitech', 'Midnight Teal', 0.14, 0.07, 12),
                     ('Accessories', 'HyperX Cloud II', 99.99, 'HyperX', 'Red', 0.32, 0.14, 8),
                     ('Accessories', 'Logitech C920s Pro HD Webcam', 79.99, 'Logitech', 'Black', 0.16, 0.04, 5))
                 AS p (category, name, price, brand, color, weight_kg, volume_l, stock)
                 JOIN category c ON c.name = p.category;
    END
$$;
