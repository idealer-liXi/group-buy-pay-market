# group-buy-pay-market Docker Design

## 1. Goal

为 `group-buy-pay-market` 补充本地 Docker 运行文件，放置在 `group-buy-pay-market/docs/dev-ops/` 下。

本次目标是基于当前机器上已经存在的 Docker 镜像，生成一套可直接启动的本地依赖环境文件，并统一容器名前缀为 `gbpm-*`。

## 2. Scope

本次设计包含：

- 在 `group-buy-pay-market/docs/dev-ops/` 下新增基础环境 `docker-compose-environment.yml`
- 在 `group-buy-pay-market/docs/dev-ops/` 下新增观测环境 `docker-compose-observability.yml`
- 新增基础中间件所需目录与配置文件
- 新增最小可用的 observability 配置文件
- 新增 `README.md` 说明启动、停止、端口、容器和用途

本次设计不包含：

- 不构建业务应用镜像
- 不生成生产部署编排
- 不改写 Java 项目代码以适配生产级容器化
- 不把所有中间件合并到单个 compose 文件中

## 3. Existing Context

### 3.1 Project State

- `group-buy-pay-market/docs/` 当前不存在 Docker 相关目录与文件
- 项目本地已完成首轮工程合并，并保留 `auth/goods/order/activity/trade` 子域
- 当前 `application-dev.yml` 已使用以下本地依赖端口：
  - MySQL `13306`
  - Redis `16379`
  - RabbitMQ `15673`

### 3.2 Existing Reference Files

可参考的现有文件包括：

- `group-buy-market/docs/dev-ops/docker-compose-environment.yml`
- `s-pay-mall/docs/dev-ops/docker-compose.yml`

这些文件已经提供了：

- MySQL / Redis / phpMyAdmin 基础组织方式
- 原项目习惯的端口和卷挂载位置

### 3.3 Local Images Already Available

当前机器已有以下可复用镜像：

- `mysql:8.0.32`
- `redis:6.2`
- `phpmyadmin:5.2.1`
- `spryker/redis-commander:0.8.0`
- `registry.cn-hangzhou.aliyuncs.com/xfg-studio/rabbitmq:3.12.9`
- `elasticsearch:7.17.28`
- `logstash:7.17.28`
- `kibana:7.17.28`
- `bitnami/prometheus:2.47.2`
- `grafana/grafana:10.2.0`

## 4. Chosen Approach

采用双 compose 文件方案：

1. `docker-compose-environment.yml`
   - 仅承载项目运行必需基础依赖
2. `docker-compose-observability.yml`
   - 仅承载 ELK、Prometheus、Grafana

这是本次推荐方案，原因如下：

- 符合“基础运行环境”和“观测环境”职责边界
- 与当前需求相符：应用先跑起来，观测组件按需启用
- 比单文件更清晰，比按服务拆文件更易用

## 5. File Layout

目标目录结构如下：

```text
group-buy-pay-market/
  docs/
    dev-ops/
      README.md
      docker-compose-environment.yml
      docker-compose-observability.yml
      mysql/
        sql/
          01-*.sql
          02-*.sql
      redis/
        redis.conf
      logstash/
        logstash.conf
      prometheus/
        prometheus.yml
```

## 6. Base Environment Compose

### 6.1 Services

`docker-compose-environment.yml` 包含以下容器：

- `gbpm-mysql`
- `gbpm-phpmyadmin`
- `gbpm-redis`
- `gbpm-redis-admin`
- `gbpm-rabbitmq`

### 6.2 Images

- `gbpm-mysql` -> `mysql:8.0.32`
- `gbpm-phpmyadmin` -> `phpmyadmin:5.2.1`
- `gbpm-redis` -> `redis:6.2`
- `gbpm-redis-admin` -> `spryker/redis-commander:0.8.0`
- `gbpm-rabbitmq` -> `registry.cn-hangzhou.aliyuncs.com/xfg-studio/rabbitmq:3.12.9`

### 6.3 Ports

基础环境端口规划：

- MySQL: `13306:3306`
- phpMyAdmin: `18899:80`
- Redis: `16379:6379`
- Redis Commander: `18081:8081`
- RabbitMQ AMQP: `15673:5672`
- RabbitMQ Console: `15674:15672`

端口选择原则：

- 尽量复用当前项目配置中已使用的端口
- 新增控制台端口避免与原项目已有端口混淆

### 6.4 Naming

所有容器名必须带 `gbpm-*` 前缀：

- `gbpm-mysql`
- `gbpm-phpmyadmin`
- `gbpm-redis`
- `gbpm-redis-admin`
- `gbpm-rabbitmq`

### 6.5 Volumes And Inputs

- MySQL
  - `./mysql/sql:/docker-entrypoint-initdb.d`
- Redis
  - `./redis/redis.conf:/usr/local/etc/redis/redis.conf`
- RabbitMQ
  - 第一版优先不依赖额外配置文件，除非镜像启动验证要求补充

### 6.6 Healthcheck

基础环境加 healthcheck：

- MySQL: `mysqladmin ping`
- Redis: `redis-cli ping`
- RabbitMQ: 使用镜像支持的健康检查命令，若镜像不稳定则先省略复杂探针

phpMyAdmin 与 Redis Commander 第一版不强制增加复杂健康检查。

## 7. Observability Compose

### 7.1 Services

`docker-compose-observability.yml` 包含：

- `gbpm-elasticsearch`
- `gbpm-logstash`
- `gbpm-kibana`
- `gbpm-prometheus`
- `gbpm-grafana`

### 7.2 Images

- `gbpm-elasticsearch` -> `elasticsearch:7.17.28`
- `gbpm-logstash` -> `logstash:7.17.28`
- `gbpm-kibana` -> `kibana:7.17.28`
- `gbpm-prometheus` -> `bitnami/prometheus:2.47.2`
- `gbpm-grafana` -> `grafana/grafana:10.2.0`

### 7.3 Purpose

- ELK: 接收并展示应用日志
- Prometheus: 抓取应用和中间件指标
- Grafana: 指标可视化

### 7.4 Coupling Rules

- 观测 compose 不依赖基础 compose 才能被解析
- 但若要真实采集业务应用与中间件指标，应与基础 compose 共用网络
- 两个 compose 使用统一网络名，便于后续互通

## 8. Network Strategy

两个 compose 统一使用一个 bridge 网络，例如：

```text
gbpm-network
```

规则：

- 基础依赖容器统一加入 `gbpm-network`
- observability 容器统一加入 `gbpm-network`
- 文档中明确两个 compose 可以分别启动，但建议先起基础环境，再起观测环境

## 9. Initialization Strategy

### 9.1 MySQL SQL

第一版直接复用现有项目 SQL：

- 来自 `s-pay-mall`
- 来自 `group-buy-market`

落地方式：

- 复制到 `docs/dev-ops/mysql/sql/`
- 使用顺序前缀命名，避免执行顺序不确定

目标：

- 先确保容器启动后可以自动初始化或至少可直接导入
- 不在本次设计里强行重构为“完美单库脚本”

### 9.2 Redis Config

提供最小 `redis.conf`：

- 允许容器正常启动
- 保留项目需要的基础行为
- 不加入无关的复杂配置

### 9.3 Logstash Config

提供最小 `logstash.conf`：

- 可接收应用发往 Logstash 的日志输入
- 能输出到 Elasticsearch

### 9.4 Prometheus Config

提供最小 `prometheus.yml`：

- 先抓 Prometheus 自身
- 预留对 `group-buy-pay-market` 应用 `actuator/prometheus` 的 scrape 配置

## 10. Documentation Requirements

`docs/dev-ops/README.md` 需要说明：

- 基础环境 compose 的用途
- 观测环境 compose 的用途
- 启动命令
- 停止命令
- 容器名列表
- 端口映射表
- 当前 `application-dev.yml` 对应关系
- 观测 compose 是否可单独启动
- 已知限制：当前为本机已有镜像驱动的本地开发环境，不等同生产部署方案

## 11. Command Shape

文档中应提供类似命令：

```bash
docker compose -f docs/dev-ops/docker-compose-environment.yml up -d
docker compose -f docs/dev-ops/docker-compose-observability.yml up -d
docker compose -f docs/dev-ops/docker-compose-environment.yml down
docker compose -f docs/dev-ops/docker-compose-observability.yml down
```

## 12. Constraints

- 仅使用本机已有镜像
- 所有容器名前缀统一为 `gbpm-*`
- 基础环境与观测环境分两个 compose
- 第一版优先保证可启动、可理解、可维护
- 不为了“看起来完整”引入当前不需要的服务

## 13. Risks And Controls

### 13.1 Risks

- 复用现有 SQL 可能存在执行顺序依赖
- RabbitMQ 镜像的默认管理端口和插件状态可能与预期不同
- ELK / Prometheus / Grafana 首轮配置可能只能做到“容器可启动”，不保证全量面板完整

### 13.2 Controls

- SQL 文件按序号命名
- 以当前已存在镜像为准，不额外改镜像来源
- 文档中显式标注第一版目标和已知限制

## 14. Success Criteria

满足以下条件视为完成本次 Docker 文档补充：

- `group-buy-pay-market/docs/dev-ops/` 目录建立完成
- 基础环境 compose 文件可创建 `gbpm-*` 前缀容器
- 观测环境 compose 文件可创建 `gbpm-*` 前缀容器
- 所需配置文件与初始化目录齐备
- 文档中写清楚启动方式、端口和用途
