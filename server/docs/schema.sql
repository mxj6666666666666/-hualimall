CREATE TABLE IF NOT EXISTS product
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(200)   NOT NULL,
    category_id BIGINT         NULL,
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    image_url   VARCHAR(500)   NULL,
    description TEXT           NULL,
    status      INT            NOT NULL DEFAULT 1,
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no    VARCHAR(64) UNIQUE NOT NULL,
    user_id     BIGINT             NOT NULL,
    total_amount DECIMAL(10, 2)    NOT NULL DEFAULT 0.00,
    status      INT                NOT NULL DEFAULT 0,
    create_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_item
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id      BIGINT         NOT NULL,
    product_id    BIGINT         NOT NULL,
    product_name  VARCHAR(200)   NOT NULL,
    product_price DECIMAL(10, 2) NOT NULL,
    quantity      INT            NOT NULL,
    total_price   DECIMAL(10, 2) NOT NULL,
    KEY idx_order_id (order_id)
);

CREATE TABLE IF NOT EXISTS `user`
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(200) NOT NULL,
    nickname    VARCHAR(100) NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status      INT          NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    product_id  BIGINT   NOT NULL,
    quantity    INT      NOT NULL DEFAULT 1,
    selected    INT      NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cart_user_product (user_id, product_id)
);
