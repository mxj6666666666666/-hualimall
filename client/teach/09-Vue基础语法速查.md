# Vue 基础语法速查（结合本项目）

## 1. `<script setup>` 是什么
你看到的大多数页面都写成：
```vue
<script setup>
// 直接写变量、函数
</script>
```
它是 Vue 3 推荐写法，语法更简洁，不需要 `export default`。

---

## 2. 常见响应式 API
- `ref(0)`：适合基本类型（数字、字符串、布尔）
- `reactive({})`：适合对象
- `computed(() => ...)`：由已有数据自动推导出的值
- `onMounted(() => {})`：组件初次渲染后执行（常用于请求数据）

---

## 3. 模板里最常用指令
- `v-model`：双向绑定输入框
  - `v-model.trim` 去空格
  - `v-model.number` 转数字
- `v-if / v-else-if / v-else`：条件渲染
- `v-for="item in list" :key="item.id"`：列表渲染
- `:disabled="loading"`：动态绑定属性
- `@click="submit"`：事件绑定

---

## 4. 路由相关
- `<RouterLink to="/products">`：页面跳转链接
- `useRouter()`：代码里主动跳转（`router.push(...)`）
- `useRoute()`：读取当前路由参数（如 `route.params.id`）

---

## 5. 一个页面的标准写法（你可以照这个套路学）
1. 定义状态：`loading/error/data/form`
2. 写请求函数：`fetchXxx()`
3. 写交互函数：`submit/update/delete`
4. `onMounted(fetchXxx)` 初始化
5. 模板中根据 `loading/error/empty/data` 做分支渲染

---

## 6. 你接下来怎么练
1. 先只改一个页面文案，感受热更新
2. 再给商品列表加一个筛选项
3. 再给购物车“提交订单”加一个确认弹窗

这样练 3 次，你会对 Vue 单文件组件有实战感觉。
