-- V11__add_cost_to_items_and_orders.sql

-- Add cost field to items table
ALTER TABLE items ADD COLUMN cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Add unit_cost_at_order_time field to orders table to track historical cost
ALTER TABLE orders ADD COLUMN unit_cost_at_order_time DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
