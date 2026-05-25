# group-buy-pay-market-backend

Single Spring Boot application that merges:

- `s-pay-mall-idealer-ddd`
- `group-by-market`

Subdomains preserved:

- `auth`
- `goods`
- `order`
- `activity`
- `trade`

Current merge rules:

- MQ flows are preserved
- original `order -> group-buy-market` Retrofit calls are replaced with in-process calls
- one Maven root, one boot app, one merged config tree

Current verification focus:

- full project compile: `mvn -f group-buy-pay-market-backend/pom.xml -DskipTests compile`
- smoke tests: `mvn -f group-buy-pay-market-backend/pom.xml -pl group-buy-pay-market-backend-app -am "-Dtest=RegressionSmokeTest" test`

Environment note:

- integration tests that depend on MySQL, Redis, and RabbitMQ are not part of the current lightweight verification pass because local ports `13306/3307/15673/16379` were not available during this merge session.
