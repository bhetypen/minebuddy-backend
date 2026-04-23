-- V1__create_stores_table_and_add_multitenancy.sql

-- 1. Create the Stores table
CREATE TABLE stores (
                        store_id CHAR(36) PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        facebook_link VARCHAR(500),
                        instagram_link VARCHAR(500),
                        tiktok_link VARCHAR(500),
                        phone_number VARCHAR(20),
                        gcash_number VARCHAR(20),
                        maya_number VARCHAR(20),
                        address_id CHAR(36),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_store_address FOREIGN KEY (address_id) REFERENCES address(address_id)
);

-- 2. Link the Users Table
ALTER TABLE users ADD COLUMN store_id CHAR(36);
ALTER TABLE users ADD CONSTRAINT fk_user_store FOREIGN KEY (store_id) REFERENCES stores(store_id);

-- 3. Tag Business Data (Strict Mode: NOT NULL)
ALTER TABLE items ADD COLUMN store_id CHAR(36) NOT NULL;
ALTER TABLE customers ADD COLUMN store_id CHAR(36) NOT NULL;
ALTER TABLE orders ADD COLUMN store_id CHAR(36) NOT NULL;
ALTER TABLE shipments ADD COLUMN store_id CHAR(36) NOT NULL;
ALTER TABLE payments ADD COLUMN store_id CHAR(36) NOT NULL;
ALTER TABLE address ADD COLUMN store_id CHAR(36) NOT NULL;

-- 4. Add Integrity Constraints
ALTER TABLE items ADD CONSTRAINT fk_item_store FOREIGN KEY (store_id) REFERENCES stores(store_id);
ALTER TABLE customers ADD CONSTRAINT fk_customer_store FOREIGN KEY (store_id) REFERENCES stores(store_id);
ALTER TABLE orders ADD CONSTRAINT fk_order_store FOREIGN KEY (store_id) REFERENCES stores(store_id);
ALTER TABLE shipments ADD CONSTRAINT fk_shipment_store FOREIGN KEY (store_id) REFERENCES stores(store_id);
ALTER TABLE payments ADD CONSTRAINT fk_payment_store FOREIGN KEY (store_id) REFERENCES stores(store_id);
ALTER TABLE address ADD CONSTRAINT fk_address_store FOREIGN KEY (store_id) REFERENCES stores(store_id);

-- 5. Performance Indices
CREATE INDEX idx_item_store ON items(store_id);
CREATE INDEX idx_customer_store ON customers(store_id);
CREATE INDEX idx_order_store ON orders(store_id);
CREATE INDEX idx_shipment_store ON shipments(store_id);
CREATE INDEX idx_payment_store ON payments(store_id);
CREATE INDEX idx_address_store ON address(store_id);