ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS item_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0;

UPDATE orders SET item_total = total_amount WHERE item_total = 0 AND total_amount > 0;