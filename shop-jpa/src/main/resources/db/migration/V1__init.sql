CREATE SEQUENCE customer_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE order_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE order_line_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE product_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE tag_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE customer (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    street VARCHAR(255),
    city VARCHAR(255),
    zip VARCHAR(255),
    ship_street VARCHAR(255),
    ship_city VARCHAR(255),
    ship_zip VARCHAR(255)
);

CREATE TABLE orders (
    id BIGINT NOT NULL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    placed_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE TABLE product (
    id BIGINT NOT NULL PRIMARY KEY,
    sku VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    category VARCHAR(255) NOT NULL,
    stock INTEGER NOT NULL,
    CONSTRAINT ck_product_stock_nonnegative CHECK (stock >= 0)
);

CREATE TABLE tag (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE order_line (
    id BIGINT NOT NULL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL,
    CONSTRAINT ck_order_line_quantity_positive CHECK (quantity > 0),
    CONSTRAINT fk_order_line_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_line_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE product_tag (
    product_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_product_tag_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_product_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
);
