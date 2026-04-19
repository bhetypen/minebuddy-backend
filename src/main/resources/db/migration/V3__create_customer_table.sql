CREATE TABLE customers (
                           customer_id CHAR(36) PRIMARY KEY,

                           first_name VARCHAR(255) NOT NULL,
                           last_name VARCHAR(255) NOT NULL,

                           handle VARCHAR(255),
                           platform VARCHAR(100),
                           phone VARCHAR(50),

                           address_id CHAR(36),

                           created_at TIMESTAMP NOT NULL
);