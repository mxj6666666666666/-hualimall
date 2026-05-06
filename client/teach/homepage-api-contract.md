# 商城首页（/products）接口与字段补充清单

## 1. 当前接口现状

前端当前仅使用：

- `GET /products`（分页列表）
- `GET /products/{id}`（详情）

当前 `Product` 实体字段（后端）：

- `id`
- `name`
- `categoryId`
- `price`
- `stock`
- `imageUrl`
- `description`
- `status`
- `createTime`
- `updateTime`

> 结论：可满足基础商品展示，不足以支撑首页高级交互（已售件数、活动倒计时、分类图标、服务卖点、购物车角标数量等）。

---

## 2. 首页设计所需补充字段（严格真实数据）

### 2.1 商品列表项补充

建议在商品列表返回的每个商品对象中补充：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `soldCount` | number | 否 | 已售件数，用于“已售 xxx 件” |
| `originPrice` | number | 否 | 划线价（展示优惠感） |
| `coverRatio` | string | 否 | 图片比例提示（如 `4:3`），便于前端裁切策略 |
| `badge` | string | 否 | 商品角标（如“新品/热卖”） |
| `campaign` | object | 否 | 活动信息（见 2.2） |

### 2.2 活动倒计时字段

`campaign` 对象建议结构：

```json
{
  "campaign": {
    "id": 1001,
    "title": "限时秒杀",
    "startTime": "2026-04-24T10:00:00",
    "endTime": "2026-04-25T10:00:00",
    "status": "ONGOING"
  }
}
```

字段说明：

- `startTime`、`endTime`：前端倒计时翻牌依据
- `status`：`NOT_STARTED | ONGOING | ENDED`

### 2.3 分类图标导航区数据

新增分类导航接口，建议：

- `GET /categories/nav`

返回项建议：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 分类ID |
| `name` | string | 是 | 分类名 |
| `iconUrl` | string | 是 | 线性图标URL（或SVG） |
| `sort` | number | 否 | 排序 |
| `productCount` | number | 否 | 分类下商品数量 |

### 2.4 首页服务保障区数据

新增服务卖点接口，建议：

- `GET /home/services`

返回项建议：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 服务ID |
| `title` | string | 是 | 如“正品保证” |
| `subtitle` | string | 否 | 如“平台严格验真” |
| `iconUrl` | string | 是 | 渐变蓝风格图标 |
| `enabled` | boolean | 是 | 是否启用 |

### 2.5 购物车角标数量（加购反馈）

建议提供：

- `GET /cart/count`

返回：

```json
{ "count": 3 }
```

用于“加入购物车后右上角角标弹跳”反馈。

---

## 3. 推荐接口形态（可选整合）

如果希望减少首屏请求数，建议聚合接口：

- `GET /home/products`

返回结构示例：

```json
{
  "categories": [],
  "recommendProducts": [],
  "services": [],
  "cartCount": 0
}
```

---

## 4. 前端展示约束（严格依赖后端真实字段）

- 缺 `soldCount`：不显示“已售 xxx 件”。
- 缺 `campaign` 或时间字段：不显示倒计时组件。
- 缺 `iconUrl`：分类项仅显示文字，不渲染图标占位假数据。
- 缺 `cart/count`：不展示角标数量，仅保留加购按钮反馈。

---

## 5. 与现有后端代码的对应关系

- 当前商品查询入口：`ProductController#list` -> `ProductServiceImpl#findall` -> `ProductMapper#selectProductPage`
- 当前实体：`pojo/Product.java`（暂无首页增强字段）

建议优先顺序：

1. 商品列表补充 `soldCount` + `campaign`  
2. 分类导航接口 `/categories/nav`  
3. 购物车数量接口 `/cart/count`  
4. 服务保障接口 `/home/services`
