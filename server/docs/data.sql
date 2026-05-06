INSERT INTO `user` (username, password, nickname, avatar_url, role, status)
VALUES ('user', '123456', '普通用户', '/uploads/avatars/user.jpg', 'USER', 1),
       ('admin', '123456', '管理员', '/uploads/avatars/admin.jpg', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    nickname = VALUES(nickname),
    avatar_url = VALUES(avatar_url),
    role = VALUES(role),
    status = VALUES(status);

INSERT INTO product (name, category_id, price, stock, image_url, description, status)
VALUES ('华为 Mate60 Pro', 1, 6999.00, 120, '/uploads/products/mate60pro.jpg', '华为旗舰手机', 1),
       ('iPhone 15 Pro', 1, 8999.00, 80, '/uploads/products/iphone15pro.jpg', 'Apple 旗舰手机', 1),
       ('小米 14 Ultra', 1, 6499.00, 100, '/uploads/products/xiaomi14ultra.jpg', '小米影像旗舰', 1),
       ('MacBook Pro 14', 2, 14999.00, 30, '/uploads/products/macbookpro14.jpg', 'Apple 笔记本电脑', 1),
       ('联想拯救者 Y9000P', 2, 9999.00, 45, '/uploads/products/y9000p.jpg', '高性能游戏本', 1),
       ('华硕天选 5', 2, 7899.00, 50, '/uploads/products/tianxuan5.jpg', '电竞笔记本', 1),
       ('索尼 WH-1000XM5', 3, 2299.00, 200, '/uploads/products/xm5.jpg', '降噪耳机', 1),
       ('Apple Watch S10', 3, 3199.00, 70, '/uploads/products/watchs10.jpg', '智能手表', 1),
       ('iPad Pro 11', 4, 6799.00, 60, '/uploads/products/ipadpro11.jpg', '高性能平板电脑', 1),
       ('机械键盘 K8 Pro', 5, 599.00, 300, '/uploads/products/k8pro.jpg', '无线机械键盘', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

UPDATE product
SET image_url = CASE id
                    WHEN 1 THEN '/uploads/products/iphone15pro.jpg'
                    WHEN 2 THEN '/uploads/products/mijia-toothbrush.jpg'
                    WHEN 3 THEN '/uploads/products/default-product.jpg'
                    WHEN 4 THEN '/uploads/products/iphone15promax.jpg'
                    WHEN 5 THEN '/uploads/products/mate60pro.jpg'
                    WHEN 9 THEN '/uploads/products/mate60pro.jpg'
                    WHEN 10 THEN '/uploads/products/iphone15pro.jpg'
                    WHEN 11 THEN '/uploads/products/xiaomi14ultra.jpg'
                    WHEN 12 THEN '/uploads/products/macbookpro14.jpg'
                    WHEN 13 THEN '/uploads/products/y9000p.jpg'
                    WHEN 14 THEN '/uploads/products/tianxuan5.jpg'
                    WHEN 15 THEN '/uploads/products/xm5.jpg'
                    WHEN 16 THEN '/uploads/products/watchs10.jpg'
                    WHEN 17 THEN '/uploads/products/ipadpro11.jpg'
                    WHEN 18 THEN '/uploads/products/k8pro.jpg'
                    ELSE image_url
    END
WHERE id IN (1, 2, 3, 4, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
