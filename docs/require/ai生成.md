这里是调用这几个接口时，所需要的发送数据的具体 JSON 格式和参数内容：

### 1. 创建订单 (`POST /order/create`)
该接口使用 `@RequestBody` 接收数据，你需要发送以下 JSON 格式的 Body 数据（后端会自动计算价格和总金额）：

```json
{
  "userId": 1001,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

### 2. 订单列表 (`GET /order/list`)
GET 请求不需要 JSON Body 数据，数据跟在 URL 参数后面即可。对应的请求参数（键值对）如下：
*   **page**: 1 （缺省代表第1页）
*   **pageSize**: 10 （每页显示的数量）

实际请求的 URL 示例：`GET /order/list?page=1&pageSize=10`

### 3. 订单详情 (`GET /order/{id}`)
不需要 JSON Body，数据直接放在 URL 路径中。
*   **id**: 订单的自增 ID

实际请求的 URL 示例：`GET /order/1`

### 4. 取消订单 (`PUT /order/cancel/{id}`)
不需要 JSON Body，数据直接放在 URL 路径中。
*   **id**: 要取消的订单的 ID

实际请求的 URL 示例：`PUT /order/cancel/1`