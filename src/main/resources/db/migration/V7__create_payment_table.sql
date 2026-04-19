CREATE TABLE payments (
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