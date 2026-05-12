# 🛍️ HualiMall（华丽商城）

> 一个前后端分离的商城系统示例项目，兼顾**大学生个人项目的完整业务闭环**与**工程化项目的安全实践**。  
> 前端基于 Vue3 + Vite，后端基于 Spring Boot + MyBatis/JPA，集成 JWT 登录认证与支付宝沙箱支付。

---

## 📌 1. 项目简介

### 项目功能介绍
HualiMall 实现了电商核心流程：用户注册登录、商品浏览与搜索、购物车、下单、支付、订单管理、商家端商品管理与后台管理能力。

### 项目定位
- 🎓 **学习型项目**：适合课程设计、毕业设计、简历项目展示
- 🧩 **工程化项目**：具备模块化结构、权限控制、安全加固思路

### 技术亮点
- 前后端分离架构（Vue3 + Spring Boot）
- MyBatis + JPA 混合数据访问
- JWT 鉴权 + HttpOnly Cookie 会话实践
- BCrypt 密码哈希存储
- 支付宝沙箱支付 + 异步回调处理
- 订单/支付链路权限校验（防越权）
- 环境变量管理敏感配置（数据库/JWT/支付密钥）

---

## 🧱 2. 技术栈

| 分类 | 技术 |
|---|---|
| 前端技术 | Vue 3、Vue Router 4、Pinia、Axios |
| 后端技术 | Spring Boot 4、Spring MVC、MyBatis、Spring Data JPA |
| 数据库 | MySQL |
| 支付功能 | 支付宝沙箱（`alipay-sdk-java`） |
| 安全认证 | JWT、BCrypt、HandlerInterceptor、HttpOnly Cookie |
| 构建工具 | Maven、npm、Vite |

---

## ✅ 3. 项目功能

### 用户与认证
- 用户注册（买家/商家）
- 用户登录/退出
- JWT 鉴权（服务端校验）
- 基于角色的访问控制（BUYER / MERCHANT / ADMIN）

### 商城业务
- 商品列表与商品详情
- 关键词搜索、分页查询
- 购物车增删改查
- 创建订单、取消订单、查询订单
- 订单状态流转（待支付/已支付/已取消/已完成）

### 支付能力
- 支付宝沙箱下单
- 支付跳转收银台
- 支付异步回调（验签、金额校验、幂等更新）
- 防重复回调与支付状态机更新

### 管理与扩展
- 管理员商品管理、订单管理
- 商家商品管理与类目统计
- 文件上传（商品图片）
- 支付接口鉴权与订单归属校验（防越权）

---

## 🗂️ 4. 项目结构

```text
HualiMall/
├─ client/                       # 前端 Vue3 + Vite
│  ├─ src/
│  │  ├─ api/                    # axios 请求封装与接口模块
│  │  ├─ views/                  # 页面（登录、商品、购物车、订单、支付等）
│  │  ├─ router/                 # 路由与前端权限守卫
│  │  ├─ store/                  # Pinia 状态管理
│  │  └─ components/             # 复用组件
│  └─ package.json
│
├─ server/                       # 后端 Spring Boot
│  ├─ src/main/java/com/xinjiema/hualimall/
│  │  ├─ config/                 # 配置类（JWT/支付/Cookie/WebMvc 等）
│  │  ├─ controll/               # Controller 层（接口入口）
│  │  ├─ service/                # 业务接口
│  │  ├─ service/impl/           # 业务实现
│  │  ├─ mapper/                 # MyBatis Mapper 接口
│  │  ├─ interceptor/            # 鉴权拦截器
│  │  ├─ pojo/                   # 实体/DTO
│  │  └─ utils/                  # 工具类（JWT、支付单号等）
│  ├─ src/main/resources/
│  │  ├─ application.yml
│  │  └─ com/.../mapper/*.xml    # MyBatis XML
│  ├─ docs/
│  │  ├─ schema.sql              # 建表脚本
│  │  └─ data.sql                # 初始化数据
│  └─ pom.xml
│
└─ README.md
```

---

## ⚙️ 5. 环境配置

### 基础环境建议
- **JDK**：25（与当前 `pom.xml` 保持一致）
- **Node.js**：18+（推荐 20 LTS）
- **MySQL**：8.0+
- **Maven**：3.9+
- **npm**：9+

### 后端配置建议（本地）
建议使用 `application-local.yml` 管理本地配置，并通过启动参数激活：

```yaml
# server/src/main/resources/application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hualimall?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your-db-password

jwt:
  secret: your-jwt-secret

alipay:
  app-id: your-app-id
  app-private-key: your-private-key
  alipay-public-key: your-alipay-public-key
  notify-url: https://your-domain/payments/notify/alipay
  return-url: http://localhost:5173/pay-result.html
```

### 更推荐：环境变量
当前项目已支持通过环境变量读取敏感信息（数据库密码、JWT 密钥、支付宝私钥等）：

```powershell
setx DB_PASSWORD "your-db-password"
setx JWT_SECRET "your-jwt-secret"
setx ALIPAY_APP_PRIVATE_KEY "your-private-key"
```

---

## 🚀 6. 项目启动教程

### 1) 初始化数据库
```sql
CREATE DATABASE hualimall DEFAULT CHARACTER SET utf8mb4;
```

执行：
- `server/docs/schema.sql`
- `server/docs/data.sql`

> 备注：服务启动时会自动检查并补齐部分历史缺失表/字段。

### 2) 启动后端 （需要自行配置环境变量）
```bash
cd server
mvn spring-boot:run
```

### 3) 启动前端
```bash
cd client
npm install
npm run dev
```

前端默认通过 Vite 代理请求后端（`/api -> http://localhost:8080`）。

---

## 💳 7. 支付宝沙箱配置

### 步骤 1：申请沙箱账号
1. 进入支付宝开放平台  
2. 开通沙箱应用并获取 `app-id`

### 步骤 2：配置 RSA2 密钥
1. 生成应用私钥（商户私钥）
2. 上传应用公钥到沙箱后台
3. 获取支付宝公钥并配置到项目

### 步骤 3：配置关键参数
- `alipay.app-id`
- `alipay.app-private-key`
- `alipay.alipay-public-key`
- `alipay.notify-url`（异步回调）
- `alipay.return-url`（支付完成跳转页）

### 步骤 4：本地回调调试（natapp）
支付回调需要公网地址，可使用 natapp 内网穿透：

```bash
natapp -authtoken=你的token
```

将生成的公网地址填入 `notify-url`，例如：
`https://xxxx.natappfree.cc/payments/notify/alipay`

---

## 🔐 8. JWT 与安全

### JWT 工作原理（简版）
1. 用户登录成功后，服务端签发 JWT  
2. 客户端后续请求携带 JWT（项目采用 HttpOnly Cookie）  
3. 后端拦截器校验签名、过期时间、用户信息  

### BCrypt 密码加密
- 注册时：`encode(rawPassword)`
- 登录时：`matches(raw, encoded)`
- 好处：即使数据库泄露，也难以直接还原原密码



---

## 🖼️ 10. 项目截图



*** 买家端 ：***

![image](https://github.com/mxj6666666666666/-hualimall/blob/master/image/image.png)

![image](https://github.com/mxj6666666666666/-hualimall/blob/master/image/imagecopy.png)

![image](https://github.com/mxj6666666666666/-hualimall/blob/master/image/imagecopy2.png)

![image](https://github.com/mxj6666666666666/-hualimall/blob/master/image/imagecopy3.png)

![image](https://github.com/mxj6666666666666/-hualimall/blob/master/image/imagecopy4.png)





*** 商家端：***

![image](https://github.com/mxj6666666666666/-hualimall/blob/master/image/imagecopy5.png)

![image](https://github.com/mxj6666666666666/-hualimall/blob/master/image/imagecopy6.png)



