CREATE SEQUENCE IF NOT EXISTS customer_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS orders_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS product_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS tag_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS order_line_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE customer (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    zip VARCHAR(255) NOT NULL,
    ship_street VARCHAR(255),
    ship_city VARCHAR(255),
    ship_zip VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE orders (
    id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    placed_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE product (
    id BIGINT NOT NULL,
    sku VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    category VARCHAR(20) NOT NULL,
    stock INTEGER NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_sku UNIQUE (sku)
);

CREATE TABLE tag (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tag_name UNIQUE (name)
);

CREATE TABLE product_tag (
    product_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_product_tag_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_product_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
);

CREATE TABLE order_line (
    id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_line_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_line_product FOREIGN KEY (product_id) REFERENCES product(id)
);
