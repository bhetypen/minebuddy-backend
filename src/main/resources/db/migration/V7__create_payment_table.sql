-- Orders table additions (safe to re-run)
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS item_total DECIMAL(10,2) NOT NULL DEFAULT 0;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0;

-- Backfill only rows where item_total is still 0 (from default)
UPDATE orders
SET item_total = total_amount
WHERE item_total = 0 AND total_amount > 0;

-- Payments table (safe to re-run)
CREATE TABLE IF NOT EXISTS payments (
                                        payment_id CHAR(36) PRIMARY KEY,
                                        order_id CHAR(36) NOT NULL,
                                        amount DECIMAL(10,2) NOT NULL,
                                        payment_method VARCHAR(20) NOT NULL,
                                        payment_reference VARCHAR(100),
                                        receipt_url VARCHAR(500),
                                        payment_date DATETIME NOT NULL,
                                        payment_updated_date DATETIME NOT NULL,
                                        INDEX idx_payment_order (order_id)
);