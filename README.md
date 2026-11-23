# 电商交易微服务系统（Spring Cloud Alibaba）

按业务域拆分的电商交易后端，用来把微服务的几件事亲手做一遍：注册与配置、限流规则持久化、分布式事务、链路追踪。**11 个 Maven 模块、7 个库 13 张表、107 个 Java 类。**

课程项目（《软件框架技术》/微服务实践），个人独立完成。

## 技术栈

Spring Cloud Alibaba（Nacos / Sentinel / Seata / Gateway / OpenFeign）+ Sleuth + Zipkin + Spring Boot + MyBatis-Plus + MySQL

| 组件 | 版本/说明 |
|---|---|
| JDK | 17 |
| Spring Boot / Spring Cloud | 3.x / 2023.x（Spring Cloud Alibaba 2023 系） |
| Nacos | 2.x，兼作注册中心与配置中心（命名空间 `mall-dev`） |
| Seata | Server 1.4.2 + `seata-spring-boot-starter` 1.7.0，AT 模式 |

## 模块划分

| 模块 | 职责 | 端口 |
|---|---|---|
| `mall-gateway` | 统一入口、路由、限流 | 8080 |
| `mall-user` | 用户与地址 | 8081 |
| `mall-product` | 商品与分类 | 8082 |
| `mall-stock` | 库存扣减与流水 | 8083 |
| `mall-order` | 下单、订单明细（全局事务发起方） | 8084 |
| `mall-payment` | 支付（模拟） | 8085 |
| `mall-delivery` | 发货与物流 | 8086 |
| `mall-auth` | 登录与 token | 8088 |
| `mall-admin` | 后台管理 | 8089 |
| `mall-common` | 统一 `Result`、错误码、全局异常、用户上下文 | — |
| `mall-api` | Feign 接口与 DTO | — |

## 调用链（下单）

```
POST /orders → gateway → mall-order
                          ├─ ProductFeign  查商品价格
                          ├─ 写 t_order / t_order_item
                          └─ StockFeign.deduct（逐个 SKU 扣库存）
                                 │  任一失败抛 BizException
                                 ▼
                          Seata TC 驱动全局回滚（undo_log 反向补偿）
```

`OrderServiceImpl.createOrder` 上是 `@GlobalTransactional(name = "mall-create-order", rollbackFor = Exception.class)`。

## 关键实现

**1. 注册与配置中心**
Nacos 兼作注册中心与配置中心，各服务的 `bootstrap.yml` 通过 `extension-configs` 拉取 `mall-*.yaml`，改配置不用重启打包。

**2. 流控规则持久化**
Sentinel 默认把规则放内存，重启即丢。这里把流控/降级/热点规则（`nacos-config/mall-sentinel-*.json`）推到 Nacos 数据源，服务重启后规则自动恢复。

**3. 分布式事务（Seata AT）**
下单一链路由 `@GlobalTransactional` 覆盖"创建订单 → 扣库存"，任一环节失败由 TC 驱动全局回滚，按 `undo_log` 反向补偿，避免"订单已建而库存未扣"的部分提交。

**4. 链路追踪**
Sleuth + Zipkin 打通全链路 TraceId，排查从"逐个服务翻日志"变成"按链路看各段耗时"。

## 踩坑记录：全局事务"报了回滚成功"，库存却没回滚

这是做这个项目最有价值的一段，记录下来。

**现象**：故意让第二个 SKU 扣减失败，接口正确返回 `库存不足`，订单也确实没落库，但 `t_stock.available` 被扣掉了，`undo_log` 两张表都是 0 行，`t_stock_log` 里还留下一条 order_no 在 `t_order` 里查不到的孤儿流水。换句话说是**有扣无单**。

**排查**：
- TC 侧日志正常：`Begin new global transaction ...`、`transaction will be rollback`、`rollback status: Rollbacked`；
- 但库存服务的日志里**根本没有 `branch register`**——说明库存服务压根没加入这个全局事务，它是按本地事务提交的，TC 自然"回滚了个寂寞"。

**根因**：只引了 `seata-spring-boot-starter`，没有 `spring-cloud-starter-alibaba-seata`，OpenFeign 调用时不会自动透传 `TX_XID` 请求头，下游 RM 拿不到 XID 就无法注册分支。

**修复**（`mall-order/src/main/java/com/mall/order/config/SeataXidFeignInterceptor.java`）：

```java
@Configuration
public class SeataXidFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String xid = RootContext.getXID();
        if (StringUtils.hasText(xid) && !template.headers().containsKey(RootContext.KEY_XID)) {
            template.header(RootContext.KEY_XID, xid);
        }
    }
}
```

**验证**：重跑故障单后，库存服务日志出现

```
branch register success, xid:..., branchId:..., lockKeys:t_stock:5001;t_stock_log:...
Branch Rollbacking: ...
undo_log deleted with GlobalFinished
branch rollback success, xid:..., branchId:...
```

库存原地不动、订单数不变、孤儿流水被补偿掉——回滚真正生效。

> 顺带发现：`sql/03`、`sql/04` 里 `undo_log` 建表缺 Seata 1.4+ 需要的 `ext varchar(100)` 字段，已在本地库补上。

## 本地启动

```
1. Nacos（8848）         startup.cmd -m standalone
2. Seata Server（8091）  依赖上面 Nacos 的 mall-dev / SEATA_GROUP
3. 业务服务              mall-user → mall-product → mall-stock → mall-order → ...
4. 网关（8080）
```

配置与会话脚本在 `tools/`：`init-database.bat`（建库导表）、`import-nacos-config.bat`（导入 Nacos 配置）、`start-all.bat`（批量起服务）。

> ⚠️ `sql/00_init_all.sql` 与各分库脚本含 `DROP DATABASE IF EXISTS`，执行前确认库里没有要保留的数据。

## 如何自己验证分布式事务

```bash
# 前置：t_stock 里 1001 库存充足，1005 库存设成 0
curl -X POST http://127.0.0.1:8084/orders \
  -H "Content-Type: application/json" -H "X-User-Id: 1" -H "X-Username: tester" \
  -d '{"receiver":"张三","receiverPhone":"13800000000","receiverAddress":"测试地址",
       "items":[{"productId":1001,"quantity":1},{"productId":1005,"quantity":1}]}'
```

然后查三个库应满足：`t_order` 无新记录、`t_stock.available` 与调用前一致、两个库的 `undo_log` 为 0 行、`t_stock_log` 无孤儿流水。

## 已知限制

- 没有做系统性压测，全局锁在高并发下的表现尚未量化
- 支付为模拟实现，未接真实渠道；下单链路未覆盖"支付"环节的全局事务
- `undo_log` 依赖 Seata 默认异步清理，未自建清理任务
- 各库口令为本地演示值（`root/123456`），正式环境应走配置中心加密

## 目录结构

```
mall-cloud/
├── mall-*/                 11 个业务与公共模块
├── nacos-config/           Nacos 配置（各服务 yaml + Sentinel 规则 + Seata 配置）
├── sql/                    7 个分库脚本 + 一键初始化
└── tools/                  建库/导入配置/批量启动脚本
```