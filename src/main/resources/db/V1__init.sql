-- src/main/resources/db/migration/V1__init.sql
CREATE SEQUENCE product_seq START WITH 1 INCREMENT BY 50;
-- TODO customer_seq, tag_seq, orders_seq, order_line_seq

CREATE TABLE product (
    id        BIGINT        PRIMARY KEY,
    sku       VARCHAR(20)   NOT NULL UNIQUE,
    name      VARCHAR(120)  NOT NULL,
    amount    DECIMAL(12,2) NOT NULL,
    currency  CHAR(3)       NOT NULL,
    category  VARCHAR(20)   NOT NULL,
    stock     INT           NOT NULL DEFAULT 0 CHECK (stock >= 0)
);
-- TODO customer (email UNIQUE, street/city/zip + ship_street/ship_city/ship_zip)
-- TODO tag, product_tag(product_id, tag_id, PRIMARY KEY (product_id, tag_id))
-- TODO orders (customer_id FK, status, placed_at TIMESTAMP WITH TIME ZONE, version BIGINT NOT NULL DEFAULT 0)
-- TODO order_line (order_id FK, product_id FK, quantity CHECK (quantity > 0), amount, currency)