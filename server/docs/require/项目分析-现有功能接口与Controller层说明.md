# hualimalll 项目分析（现有功能接口 + Controller 层详解）

## 1. 项目定位与分层现状

该项目是一个基于 **Spring Boot + MyBatis + PageHelper** 的商城后端，采用典型三层结构：

- **Controller 层**：处理 HTTP 请求、参数绑定、日志记录、返回统一 `Result<T>`
- **Service 层**：业务校验与业务编排（如登录校验、库存扣减、订单创建事务）
- **Mapper 层**：MyBatis 接口 + XML SQL，实现数据库读写

返回结构统一为：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

异常统一由 `GlobalExceptionHandler` 处理：

- `IllegalArgumentException` -> `400`
- `SecurityException` -> `401`
- `RuntimeException` -> `500`

---

## 2. 现有功能接口分析

> 说明：项目存在一套新路由与一套兼容旧路由并存的设计（如 `/users` 与 `/user` 同时存在）。

### 2.1 用户模块（UserController）

| 接口 | 方法 | 说明 | 鉴权 |
|---|---|---|---|
| `/users`、`/user/register` | POST | 用户注册 | 否 |
| `/users/sessions`、`/user/login` | POST | 用户登录，返回 token | 否 |
| `/users/me`、`/users/info`、`/user/me`、`/user/info` | GET | 获取当前登录用户信息 | 是 |

关键行为：

- 登录后由 `JwtUtils.createToken` 生成 Bearer Token。
- `getUserInfo()` 从 `AuthContext` 获取当前用户 ID，不依赖前端传 userId。

### 2.2 商品模块（ProductController + SuProductController）

#### 用户侧（ProductController）

| 接口 | 方法 | 说明 | 鉴权 |
|---|---|---|---|
| `/products/{id}` | GET | 查询商品详情 | 否 |
| `/products`、`/products/list` | GET | 分页查询商品 | 否 |

查询参数（`ProQueryParams`）：

- `page`（默认 1）
- `pageSize`（默认 10）
- `categoryId`
- `status`
- `keyword`

#### 管理侧（SuProductController）

| 接口 | 方法 | 说明 | 鉴权 |
|---|---|---|---|
| `/admin/products` | POST | 新增商品 | 是 |
| `/admin/products/batch` | POST | 批量新增商品 | 是 |
| `/admin/products/{id}` | PUT | 修改商品 | 是 |
| `/admin/products/batch` | PUT | 批量修改商品 | 是 |
| `/admin/products/{id}` | DELETE | 删除商品 | 是 |
| `/admin/products/batch` | DELETE | 批量删除商品 | 是 |

关键行为：

- 管理端通过路径变量传入 `id` 时，Controller 会将 `id` 回填到 `Product` 对象（避免只靠请求体中的 id）。
- 批量增删改直接透传到 Service/Mapper，依赖 MyBatis `<foreach>` 完成批处理 SQL。

### 2.3 购物车模块（CartController）

| 接口 | 方法 | 说明 | 鉴权 |
|---|---|---|---|
| `/carts`、`/cart/list` | GET | 查询当前用户购物车 | 是 |
| `/carts`、`/cart/add` | POST | 添加购物车 | 是 |
| `/carts/{id}` | PUT | 更新购物车项 | 是 |
| `/cart/update?id=...` | PUT | 更新购物车项（兼容旧接口） | 是 |
| `/carts/{id}`、`/cart/remove/{id}` | DELETE | 删除购物车项 | 是 |

关键行为：

- 业务层以 `AuthContext` 的 userId 做数据隔离（按当前用户查/改/删）。
- `CartUpdateRequest` 支持更新 `quantity` 与 `selected`，并要求至少更新一个字段。

### 2.4 订单模块（OrderController + SuOrderController）

#### 用户侧（OrderController）

| 接口 | 方法 | 说明 | 鉴权 |
|---|---|---|---|
| `/orders`、`/order/create` | POST | 创建订单 | 是 |
| `/orders`、`/order/list` | GET | 分页查询订单 | 是 |
| `/orders/{id}` | GET | 查询订单详情 | 是 |
| `/orders/{id}/cancel`、`/order/cancel/{id}` | PUT | 取消订单 | 是 |

#### 管理侧（SuOrderController）

| 接口 | 方法 | 说明 | 鉴权 |
|---|---|---|---|
| `/admin/orders/{id}/status`、`/admin/order/{id}/status/update` | PUT | 更新订单状态（0~3） | 是 |

关键行为：

- 创建订单支持两种来源：请求体自带 `items`，或自动读取当前用户已勾选购物车项。
- 下单过程包含库存扣减、订单/订单项写入、清理已勾选购物车，使用事务保证一致性。

---

## 3. Controller 层详细说明

## 3.1 统一风格与职责边界

Controller 层整体遵循“**薄控制器**”模式：

- 负责路由映射（`@RequestMapping/@GetMapping/...`）
- 负责参数绑定（`@RequestBody/@PathVariable/@RequestParam`）
- 记录请求日志（`@Slf4j` + `log.info`）
- 调用 Service 并直接返回 `Result.success(...)`

复杂校验和核心业务大多在 Service 层处理，如：

- 页码/分页大小合法性校验
- 登录/权限前置条件校验
- 库存校验与扣减
- 订单状态流转校验

## 3.2 路由设计特点

1. 同时保留 RESTful 路由与旧接口路由，减小前端迁移成本。  
2. 管理端统一前缀 `/admin/**`，普通端按业务前缀 `/users` `/products` `/carts` `/orders`。  
3. 同一个控制器内常见“新旧接口并存”的写法，例如：
   - `@PostMapping({"", "/create"})`
   - `@GetMapping({"", "/list"})`

## 3.3 参数绑定与对象建模

- 查询场景：使用参数对象（`ProQueryParams`、`OrdQueryParams`）自动绑定 query 参数。
- 写操作：以 DTO/POJO 接收 JSON 请求体（如 `LoginRequest`、`CartAddRequest`、`OrderStatusUpdateRequest`）。
- 路径变量与请求体组合时，优先信任路径变量（例如商品更新接口强制 `product.setId(id)`）。

## 3.4 鉴权如何作用到 Controller

- `WebMvcConfig` 注册 `AuthInterceptor`，对以下路径做登录拦截：
  - `/orders/**`、`/order/**`
  - `/carts/**`、`/cart/**`
  - `/admin/**`
  - 用户信息查询相关路径（`/users/me` 等）
- `AuthInterceptor` 从 `Authorization: Bearer <token>` 解析用户信息写入 `AuthContext(ThreadLocal)`。
- Controller/Service 通过 `AuthContext.getCurrentUserId()` 获取当前登录用户，无需客户端显式传用户 ID。

## 3.5 错误处理与响应一致性

- Controller 与 Service 主要通过抛异常表达失败，不在每个接口重复写 try-catch。
- `GlobalExceptionHandler` 统一转换为 `Result.error(...)`，保证返回结构一致，前端处理成本低。

---

## 4. 当前 Controller 层的可见优点与注意点

优点：

- 接口命名清晰，读写分离明显（用户侧/管理侧）。
- 兼容路由设计对存量前端友好。
- 配合拦截器 + ThreadLocal，上下文用户信息传递简洁。

注意点（现状说明）：

- `/admin/**` 当前是“已登录可访问”，代码中未看到基于 `role` 的细粒度管理员授权判断。
- 订单详情/列表等接口在 Controller 层不限制“仅本人订单”，是否需要按业务再收紧需后续确认。

---

## 5. 小结

当前项目的 Controller 层实现风格统一，已经覆盖商城核心闭环（注册登录 -> 商品浏览 -> 购物车 -> 下单 -> 管理端维护）。  
接口兼容性设计较完整，统一返回与统一异常处理也已建立，后续可重点加强“管理权限校验”和“订单访问控制”的细粒度安全策略。
