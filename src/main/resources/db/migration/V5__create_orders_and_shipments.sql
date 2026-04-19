CREATE TABLE shipments (
    shipment_id       CHAR(36)       PRIMARY KEY,
    order_id          CHAR(36)       NOT NULL UNIQUE,
    carrier           VARCHAR(100)   NOT NULL,
    tracking_number   VARCHAR(100),
    shipping_fee      DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    shipment_status   VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMP      NOT NULL,
    updated_at        TIMESTAMP      NOT NULL,

    CONSTRAINT chk_shipments_fee_nonneg    CHECK (shipping_fee >= 0),
    CONSTRAINT chk_shipments_status_valid  CHECK (shipment_status IN (
        'PENDING', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY',
        'DELIVERED', 'FAILED', 'RETURNED'
    ))
);

CREATE INDEX idx_shipments_carrier   ON shipments(carrier);
CREATE INDEX idx_shipments_status    ON shipments(shipment_status);
CREATE INDEX idx_shipments_tracking  ON shipments(tracking_number);

CREATE TABLE orders (
    order_id                 CHAR(36)       PRIMARY KEY,
    customer_id              CHAR(36)       NOT NULL,
    item_id                  CHAR(36)       NOT NULL,
    quantity                 INT            NOT NULL,
    payment_type             VARCHAR(20)    NOT NULL,
    unit_price_at_order_time DECIMAL(10,2)  NOT NULL,
    total_amount             DECIMAL(10,2)  NOT NULL,
    dp_required              DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    dp_paid                  DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    final_paid               DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    balance                  DECIMAL(10,2)  NOT NULL,
    status                   VARCHAR(30)    NOT NULL,
    created_at               TIMESTAMP      NOT NULL,
    updated_at               TIMESTAMP      NOT NULL,

    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_orders_item     FOREIGN KEY (item_id)     REFERENCES items(item_id),

    CONSTRAINT chk_orders_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_orders_amounts_nonneg    CHECK (
        unit_price_at_order_time >= 0 AND total_amount >= 0 AND
        dp_required >= 0 AND dp_paid >= 0 AND final_paid >= 0
    ),
    CONSTRAINT chk_orders_payment_type CHECK (payment_type IN ('ONHAND', 'PREORDER')),
    CONSTRAINT chk_orders_status       CHECK (status IN (
        'RESERVED', 'DP_PAID', 'FOR_ORDERING', 'ORDERED_FROM_SUPPLIER',
        'ARRIVED', 'FULLY_PAID', 'PACKED', 'SHIPPED', 'COMPLETED', 'CANCELLED'
    ))
);

CREATE INDEX idx_orders_customer   ON orders(customer_id);
CREATE INDEX idx_orders_item       ON orders(item_id);
CREATE INDEX idx_orders_status     ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

ALTER TABLE shipments
    ADD CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES orders(order_id);
