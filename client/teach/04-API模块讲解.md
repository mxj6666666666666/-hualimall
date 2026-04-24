# API 模块讲解（src/api/modules）

这些文件是“接口清单层”，让页面不直接写 URL 字符串。

## user.js
- `register(data)` -> `POST /users`
- `login(data)` -> `POST /users/sessions`
- `profile()` -> `GET /users/me`

用途：注册、登录、拉取当前用户资料。

---

## product.js
- `list(params)` -> `GET /products`
- `detail(id)` -> `GET /products/:id`

用途：商品列表和商品详情。

---

## cart.js
- `list()` -> `GET /carts`
- `add(data)` -> `POST /carts`
- `update(id, data)` -> `PUT /carts/:id`
- `remove(id)` -> `DELETE /carts/:id`

用途：购物车增删改查。

---

## order.js
- `create(data)` -> `POST /orders`
- `list(params)` -> `GET /orders`
- `detail(id)` -> `GET /orders/:id`
- `cancel(id)` -> `PUT /orders/:id/cancel`

用途：订单创建、列表、详情、取消。

---

## admin.js（管理端）
- 商品管理：查询、新增、修改、删除、批量删除
- 订单管理：查询订单、修改状态

典型区别：普通用户接口一般是 `/products`、`/orders`，管理端写在 `/admin/...` 下。

---

## 设计价值
把接口都收敛到这里后：
1. 页面更干净（只关心业务流程）
2. 改接口地址时只改一处
3. 更容易统一维护
