-- src/main/resources/db/migration/V1__init.sql
-- CREATE SEQUENCE product_seq START WITH 1 INCREMENT BY 50;
-- TODO customer_seq, tag_seq, orders_seq, order_line_seq

-- CREATE TABLE product (
--     id        BIGINT        PRIMARY KEY,
--     sku       VARCHAR(20)   NOT NULL UNIQUE,
--     name      VARCHAR(120)  NOT NULL,
--     amount    DECIMAL(12,2) NOT NULL,
--     currency  CHAR(3)       NOT NULL,
--     category  VARCHAR(20)   NOT NULL,
--     stock     INT           NOT NULL DEFAULT 0 CHECK (stock >= 0)
-- );

CREATE SEQUENCE customer_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE product_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE tag_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE order_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE order_line_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE customer (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    street VARCHAR(255),
    city VARCHAR(255),
    zip VARCHAR(50),
    ship_street VARCHAR(255),
    ship_city VARCHAR(255),
    ship_zip VARCHAR(50)
);

CREATE TABLE product (
    id BIGINT NOT NULL PRIMARY KEY,
    sku VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    category VARCHAR(50) NOT NULL,
    stock INT NOT NULL CHECK (stock >= 0)
);

CREATE TABLE tag (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE product_tag (
    product_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_product_tag_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_product_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
);

CREATE TABLE orders (
    id BIGINT NOT NULL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    placed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE order_line (
    id BIGINT NOT NULL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    amount NUMERIC(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    CONSTRAINT fk_order_line_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_line_product FOREIGN KEY (product_id) REFERENCES product(id)
);

-- TODO customer (email UNIQUE, street/city/zip + ship_street/ship_city/ship_zip)
-- TODO tag, product_tag(product_id, tag_id, PRIMARY KEY (product_id, tag_id))
-- TODO orders (customer_id FK, status, placed_at TIMESTAMP WITH TIME ZONE, version BIGINT NOT NULL DEFAULT 0)
-- TODO order_line (order_id FK, product_id FK, quantity CHECK (quantity > 0), amount, currency)