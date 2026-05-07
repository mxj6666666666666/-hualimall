# hualimalll

基于 Spring Boot + MyBatis + PageHelper 的商城后端示例，采用 Controller / Service / Mapper 三层结构，支持商品、购物车、订单、用户登录注册与管理端基础操作。

## 技术栈
- Java 25
- Spring Boot 4.0.5
- MyBatis
- MySQL

## 快速启动
1. 创建数据库 `hualimall`。
2. 执行 `docs/schema.sql` 与 `docs/data.sql`。
3. 修改 `src/main/resources/application.yml` 中数据库连接配置。
4. 运行 `HualimalllApplication`。
5. 若旧库缺少新表/字段，服务启动时会自动补齐：`buyer_profile`、`merchant_profile`、`product.merchant_id`。

## RESTful 接口（核心）

### 用户
- `POST /users`（兼容：`/user/register`）注册
- `POST /users/sessions`（兼容：`/user/login`）登录
- `GET /users/me`（兼容：`/user/info`）个人信息（需 Bearer Token）

### 商品
- `GET /products`（兼容：`/products/list`）商品列表（支持 `page/pageSize/categoryId/status/keyword`）
- `GET /products/{id}` 商品详情

### 购物车
- `GET /carts`（兼容：`/cart/list`）购物车列表
- `POST /carts`（兼容：`/cart/add`）添加购物车
- `PUT /carts/{id}`（兼容：`/cart/update?id={id}`）修改数量/勾选状态
- `DELETE /carts/{id}`（兼容：`/cart/remove/{id}`）删除购物车项

### 订单
- `POST /orders`（兼容：`/order/create`）创建订单
- `GET /orders`（兼容：`/order/list`）订单列表
- `GET /orders/{id}` 订单详情
- `PUT /orders/{id}/cancel`（兼容：`/order/cancel/{id}`）取消订单

### 管理端
- `POST /admin/products`
- `POST /admin/products/batch`
- `PUT /admin/products/{id}`
- `PUT /admin/products/batch`
- `DELETE /admin/products/{id}`
- `DELETE /admin/products/batch`
- `PUT /admin/orders/{id}/status`（兼容：`/admin/order/{id}/status/update`）

### 商家端
- `GET /merchant/products` 商家商品列表（仅本人）
- `POST /merchant/products` 商家新增商品（自动绑定本人）
- `PUT /merchant/products/{id}` 商家修改商品（仅本人）
- `DELETE /merchant/products/{id}` 商家删除商品（仅本人）
- `POST /merchant/products/image`（或 `/merchant/products/upload`）商家上传商品图片（`multipart/form-data`，字段名 `file`，落盘到 `uploads/products/{merchantId}`，文件名结构为 `原始文件名_UUIDv7`）
- `GET /merchant/orders/category-stats` 商家类目销售额与订单数统计

## 鉴权说明
- 登录成功后返回 token。
- 调用受保护接口时，Header 需携带：
  - `Authorization: Bearer <token>`
- 角色模型：`BUYER`（买家）/ `MERCHANT`（商家）/ `ADMIN`（平台管理员）
- 注册仅允许买家与商家，管理员账号不支持自助注册

## 统一返回格式
所有接口通过 `Result<T>` 返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```
