# group-buy-pay-market-backend DevOps Files

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
