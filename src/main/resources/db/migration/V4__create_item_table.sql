CREATE TABLE items (
    item_id      CHAR(36)       PRIMARY KEY,
    name         VARCHAR(255)   NOT NULL,
    category     VARCHAR(100)   NOT NULL,
    price        DECIMAL(10,2)  NOT NULL,
    stock        INT            NOT NULL DEFAULT 0,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    sale_type    VARCHAR(20)    NOT NULL,
    created_at   TIMESTAMP      NOT NULL,

    CONSTRAINT chk_items_stock_nonneg    CHECK (stock >= 0),
    CONSTRAINT chk_items_price_nonneg    CHECK (price >= 0),
    CONSTRAINT chk_items_sale_type_valid CHECK (sale_type IN ('ONHAND_ONLY', 'PREORDER_ONLY', 'HYBRID'))
);

CREATE INDEX idx_items_name     ON items(name);
CREATE INDEX idx_items_category ON items(category);
CREATE INDEX idx_items_active   ON items(active);
