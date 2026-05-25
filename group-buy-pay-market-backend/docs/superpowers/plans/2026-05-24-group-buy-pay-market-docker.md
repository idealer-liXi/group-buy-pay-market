# group-buy-pay-market Docker Files Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add local Docker environment and observability files under `group-buy-pay-market/docs/dev-ops/` using images that already exist on this machine, with all container names prefixed `gbpm-*`.

**Architecture:** Create two Docker Compose files: one for required runtime dependencies and one for observability services. Reuse existing SQL and middleware patterns from `s-pay-mall` and `group-buy-market`, keep the app's current local ports (`13306`, `16379`, `15673`), and document exact startup and shutdown commands in a project-local README.

**Tech Stack:** Docker Compose v3.9, MySQL 8.0.32, Redis 6.2, RabbitMQ 3.12.9 image, phpMyAdmin 5.2.1, Redis Commander 0.8.0, Elasticsearch 7.17.28, Logstash 7.17.28, Kibana 7.17.28, Prometheus 2.47.2, Grafana 10.2.0

---

### File Structure Map

**Files:**
- Create: `group-buy-pay-market/docs/dev-ops/docker-compose-environment.yml`
- Create: `group-buy-pay-market/docs/dev-ops/docker-compose-observability.yml`
- Create: `group-buy-pay-market/docs/dev-ops/README.md`
- Create: `group-buy-pay-market/docs/dev-ops/mysql/sql/01-s-pay-mall-ddd-market.sql`
- Create: `group-buy-pay-market/docs/dev-ops/mysql/sql/02-group-buy-market.sql`
- Create: `group-buy-pay-market/docs/dev-ops/redis/redis.conf`
- Create: `group-buy-pay-market/docs/dev-ops/prometheus/prometheus.yml`
- Create: `group-buy-pay-market/docs/dev-ops/logstash/logstash.conf`

Responsibilities:

- `docker-compose-environment.yml`: required app dependencies only
- `docker-compose-observability.yml`: ELK + Prometheus + Grafana only
- `README.md`: startup, shutdown, ports, container names, and limits
- `mysql/sql/*.sql`: boot-time initialization inputs
- `redis/redis.conf`: minimal Redis runtime config
- `prometheus/prometheus.yml`: basic scrape config
- `logstash/logstash.conf`: minimal ingest pipeline

### Task 1: Create Base Environment Compose

**Files:**
- Create: `group-buy-pay-market/docs/dev-ops/docker-compose-environment.yml`
- Test: `group-buy-pay-market/docs/dev-ops/docker-compose-environment.yml`

- [ ] **Step 1: Write the environment compose file**

```yaml
version: '3.9'

services:
  mysql:
    image: mysql:8.0.32
    container_name: gbpm-mysql
    command: --default-authentication-plugin=mysql_native_password
    restart: always
    environment:
      TZ: Asia/Shanghai
      MYSQL_ROOT_PASSWORD: 123456
    ports:
      - "13306:3306"
    volumes:
      - ./mysql/sql:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      timeout: 10s
      retries: 10
      start_period: 15s
    networks:
      - gbpm-network

  phpmyadmin:
    image: phpmyadmin:5.2.1
    container_name: gbpm-phpmyadmin
    hostname: gbpm-phpmyadmin
    ports:
      - "18899:80"
    environment:
      - PMA_HOST=mysql
      - PMA_PORT=3306
      - MYSQL_ROOT_PASSWORD=123456
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - gbpm-network

  redis:
    image: redis:6.2
    container_name: gbpm-redis
    restart: always
    hostname: gbpm-redis
    privileged: true
    ports:
      - "16379:6379"
    volumes:
      - ./redis/redis.conf:/usr/local/etc/redis/redis.conf
    command: redis-server /usr/local/etc/redis/redis.conf
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - gbpm-network

  redis-admin:
    image: spryker/redis-commander:0.8.0
    container_name: gbpm-redis-admin
    hostname: gbpm-redis-admin
    restart: always
    ports:
      - "18081:8081"
    environment:
      - REDIS_HOSTS=local:redis:6379
      - HTTP_USER=admin
      - HTTP_PASSWORD=admin
      - LANG=C.UTF-8
      - LANGUAGE=C.UTF-8
      - LC_ALL=C.UTF-8
    depends_on:
      redis:
        condition: service_healthy
    networks:
      - gbpm-network

  rabbitmq:
    image: registry.cn-hangzhou.aliyuncs.com/xfg-studio/rabbitmq:3.12.9
    container_name: gbpm-rabbitmq
    restart: always
    hostname: gbpm-rabbitmq
    ports:
      - "15673:5672"
      - "15674:15672"
    networks:
      - gbpm-network

networks:
  gbpm-network:
    driver: bridge
```

- [ ] **Step 2: Verify the environment compose parses**

Run: `docker compose -f "D:\projects\group-buy-pay-market\docs\dev-ops\docker-compose-environment.yml" config`
Expected: exit 0 and rendered YAML output.

- [ ] **Step 3: Record repo state instead of commit**

Run: `git rev-parse --is-inside-work-tree`
Expected: FAIL with `not a git repository` because `group-buy-pay-market` is currently not a git repo.

```text
Do not commit in this task. The project directory is not currently a Git repository.
```

### Task 2: Add Initialization Inputs For MySQL And Redis

**Files:**
- Create: `group-buy-pay-market/docs/dev-ops/mysql/sql/01-s-pay-mall-ddd-market.sql`
- Create: `group-buy-pay-market/docs/dev-ops/mysql/sql/02-group-buy-market.sql`
- Create: `group-buy-pay-market/docs/dev-ops/redis/redis.conf`
- Test: `group-buy-pay-market/docs/dev-ops/mysql/sql/01-s-pay-mall-ddd-market.sql`

- [ ] **Step 1: Copy the payment-side SQL into the new docs tree**

Source file:

```text
D:\projects\s-pay-mall\s-pay-mall-idealer-ddd\docs\dev-ops\mysql\sql\s-pay-mall-ddd-market.sql
```

Target file:

```text
group-buy-pay-market/docs/dev-ops/mysql/sql/01-s-pay-mall-ddd-market.sql
```

- [ ] **Step 2: Copy the group-buy SQL into the new docs tree**

Source file:

```text
D:\projects\group-buy-market\idealer\group-by-market\docs\dev-ops\mysql\sql\2-29-group_buy_market.sql
```

Target file:

```text
group-buy-pay-market/docs/dev-ops/mysql/sql/02-group-buy-market.sql
```

- [ ] **Step 3: Write a minimal Redis config**

```conf
bind 0.0.0.0
protected-mode no
port 6379
timeout 0
tcp-keepalive 300
daemonize no
appendonly no
```

- [ ] **Step 4: Verify the SQL and Redis inputs exist**

Run: `Test-Path -LiteralPath "D:\projects\group-buy-pay-market\docs\dev-ops\mysql\sql\01-s-pay-mall-ddd-market.sql"; Test-Path -LiteralPath "D:\projects\group-buy-pay-market\docs\dev-ops\mysql\sql\02-group-buy-market.sql"; Test-Path -LiteralPath "D:\projects\group-buy-pay-market\docs\dev-ops\redis\redis.conf"`
Expected: `True` for all three paths.

- [ ] **Step 5: Record repo state instead of commit**

Run: `git rev-parse --is-inside-work-tree`
Expected: FAIL with `not a git repository`.

```text
Do not commit in this task. The project directory is not currently a Git repository.
```

### Task 3: Create Observability Compose And Config Files

**Files:**
- Create: `group-buy-pay-market/docs/dev-ops/docker-compose-observability.yml`
- Create: `group-buy-pay-market/docs/dev-ops/prometheus/prometheus.yml`
- Create: `group-buy-pay-market/docs/dev-ops/logstash/logstash.conf`
- Test: `group-buy-pay-market/docs/dev-ops/docker-compose-observability.yml`

- [ ] **Step 1: Write the observability compose file**

```yaml
version: '3.9'

services:
  elasticsearch:
    image: elasticsearch:7.17.28
    container_name: gbpm-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
    ports:
      - "19200:9200"
    networks:
      - gbpm-network

  logstash:
    image: logstash:7.17.28
    container_name: gbpm-logstash
    ports:
      - "14560:4560"
    volumes:
      - ./logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on:
      - elasticsearch
    networks:
      - gbpm-network

  kibana:
    image: kibana:7.17.28
    container_name: gbpm-kibana
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "15601:5601"
    depends_on:
      - elasticsearch
    networks:
      - gbpm-network

  prometheus:
    image: bitnami/prometheus:2.47.2
    container_name: gbpm-prometheus
    ports:
      - "19090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/opt/bitnami/prometheus/conf/prometheus.yml
    networks:
      - gbpm-network

  grafana:
    image: grafana/grafana:10.2.0
    container_name: gbpm-grafana
    ports:
      - "13000:3000"
    depends_on:
      - prometheus
    networks:
      - gbpm-network

networks:
  gbpm-network:
    driver: bridge
```

- [ ] **Step 2: Write the Prometheus config**

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: prometheus
    static_configs:
      - targets: ['prometheus:9090']

  - job_name: group-buy-pay-market-app
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['host.docker.internal:8080']
```

- [ ] **Step 3: Write the Logstash pipeline**

```conf
input {
  tcp {
    port => 4560
    codec => json_lines
  }
}

output {
  elasticsearch {
    hosts => ["http://elasticsearch:9200"]
    index => "gbpm-logs-%{+YYYY.MM.dd}"
  }
  stdout { codec => rubydebug }
}
```

- [ ] **Step 4: Verify the observability compose parses**

Run: `docker compose -f "D:\projects\group-buy-pay-market\docs\dev-ops\docker-compose-observability.yml" config`
Expected: exit 0 and rendered YAML output.

- [ ] **Step 5: Record repo state instead of commit**

Run: `git rev-parse --is-inside-work-tree`
Expected: FAIL with `not a git repository`.

```text
Do not commit in this task. The project directory is not currently a Git repository.
```

### Task 4: Document Usage And Validate Both Compose Files Together

**Files:**
- Create: `group-buy-pay-market/docs/dev-ops/README.md`
- Test: `group-buy-pay-market/docs/dev-ops/README.md`

- [ ] **Step 1: Write the ops README**

```markdown
# group-buy-pay-market DevOps Files

## Base Environment

Starts:

- gbpm-mysql
- gbpm-phpmyadmin
- gbpm-redis
- gbpm-redis-admin
- gbpm-rabbitmq

Command:

```bash
docker compose -f docs/dev-ops/docker-compose-environment.yml up -d
```

Stop:

```bash
docker compose -f docs/dev-ops/docker-compose-environment.yml down
```

## Observability

Starts:

- gbpm-elasticsearch
- gbpm-logstash
- gbpm-kibana
- gbpm-prometheus
- gbpm-grafana

Command:

```bash
docker compose -f docs/dev-ops/docker-compose-observability.yml up -d
```

Stop:

```bash
docker compose -f docs/dev-ops/docker-compose-observability.yml down
```

## Ports

- MySQL: 13306
- phpMyAdmin: 18899
- Redis: 16379
- Redis Commander: 18081
- RabbitMQ AMQP: 15673
- RabbitMQ Console: 15674
- Elasticsearch: 19200
- Logstash TCP: 14560
- Kibana: 15601
- Prometheus: 19090
- Grafana: 13000

## Notes

- All container names use `gbpm-*`
- Base and observability compose files use the same bridge network name
- This setup targets local development only
- The app currently maps to MySQL 13306, Redis 16379, and RabbitMQ 15673 in `application-dev.yml`
```

- [ ] **Step 2: Verify both compose files still parse after README and config placement**

Run: `docker compose -f "D:\projects\group-buy-pay-market\docs\dev-ops\docker-compose-environment.yml" config; if ($?) { docker compose -f "D:\projects\group-buy-pay-market\docs\dev-ops\docker-compose-observability.yml" config }`
Expected: both commands exit 0.

- [ ] **Step 3: Verify the documentation file exists**

Run: `Test-Path -LiteralPath "D:\projects\group-buy-pay-market\docs\dev-ops\README.md"`
Expected: `True`

- [ ] **Step 4: Record repo state instead of commit**

Run: `git rev-parse --is-inside-work-tree`
Expected: FAIL with `not a git repository`.

```text
Do not commit in this task. The project directory is not currently a Git repository.
```
