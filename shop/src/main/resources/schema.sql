CREATE TABLE product (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(80) NOT NULL UNIQUE,
                         price DECIMAL(10,2),
                         stock INT CHECK (stock >= 0)
);