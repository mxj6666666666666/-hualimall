INSERT INTO `user` (username, password, nickname, role, status)
VALUES ('user', '123456', '普通用户', 'USER', 1),
       ('admin', '123456', '管理员', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO product (name, category_id, price, stock, image_url, description, status)
VALUES ('华为 Mate60 Pro', 1, 6999.00, 120, 'https://example.com/mate60pro.jpg', '华为旗舰手机', 1),
       ('iPhone 15 Pro', 1, 8999.00, 80, 'https://example.com/iphone15pro.jpg', 'Apple 旗舰手机', 1),
       ('小米 14 Ultra', 1, 6499.00, 100, 'https://example.com/xiaomi14ultra.jpg', '小米影像旗舰', 1),
       ('MacBook Pro 14', 2, 14999.00, 30, 'https://example.com/macbookpro14.jpg', 'Apple 笔记本电脑', 1),
       ('联想拯救者 Y9000P', 2, 9999.00, 45, 'https://example.com/y9000p.jpg', '高性能游戏本', 1),
       ('华硕天选 5', 2, 7899.00, 50, 'https://example.com/tianxuan5.jpg', '电竞笔记本', 1),
       ('索尼 WH-1000XM5', 3, 2299.00, 200, 'https://example.com/xm5.jpg', '降噪耳机', 1),
       ('Apple Watch S10', 3, 3199.00, 70, 'https://example.com/watchs10.jpg', '智能手表', 1),
       ('iPad Pro 11', 4, 6799.00, 60, 'https://example.com/ipadpro11.jpg', '高性能平板电脑', 1),
       ('机械键盘 K8 Pro', 5, 599.00, 300, 'https://example.com/k8pro.jpg', '无线机械键盘', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);
