CREATE TABLE address (
                         address_id CHAR(36) PRIMARY KEY,
                         line_1 VARCHAR(255) NOT NULL,
                         line_2 VARCHAR(255),
                         barangay VARCHAR(150) NOT NULL,
                         city VARCHAR(150) NOT NULL,
                         province VARCHAR(150) NOT NULL,
                         region VARCHAR(150) NOT NULL,
                         zip VARCHAR(20) NOT NULL,
                         landmark VARCHAR(255)
);