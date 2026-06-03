# Spring boot and Microservice with Kubernates 
Spring demo with Microservice and Kubernetes


## Running kubernates dashboard
- kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.5.0/aio/deploy/recommended.yaml

- kubectl proxy

- Get token : kubectl -n kubernetes-dashboard create token admin-user

## Understanding building individual Microservice

Each microservice contains pom.xml. Before building make sure the docker hub credentials are setting settings.xml file.

To run only build : mvn clean install

To build and push to docker : mvn clean install jlib:build

## Applying kubernates configuration to make pod running.

Go to k8s directory which contains 
- Apply config maps first : kubectl apply -f generic-config-map.yaml
- Setting up mysql pod : kubectl apply -f mysql.yaml
This yaml contains mysql password as well , you can use secrets/config for passwords too.
- Set up zipkin : kubectl apply -f zipkin.yaml
Access zipkin dashboard with http://localhost:9411 which was exposed using loadbalancer
- Set up service registry pod : kubectl apply -f service-registry-deployment.yaml
- Set up config server pod : kubectl apply -f config-service-deployment.yaml
- Set up cloud gateway pod : kubectl apply -f cloud-gateway-deployment.yaml
- Set up other microservices pod : kubectl apply -f payment-service-deployment.yaml/order-service-deployment.yaml/product-service-deployment.yaml

## Running with Docker Compose and local MySQL

This repository includes Docker support for the Spring Boot services while keeping MySQL on the host machine. Service images use Amazon Corretto 17.

Make sure local MySQL has these databases:

```sql
CREATE DATABASE IF NOT EXISTS productdb;
CREATE DATABASE IF NOT EXISTS paymentdb;
CREATE DATABASE IF NOT EXISTS orderdb;
CREATE DATABASE IF NOT EXISTS userdb;
```

Build the jars, build Docker images, and start the full stack:

```bash
scripts/docker-ecosystem.sh start
```

Deploy one service independently:

```bash
scripts/docker-ecosystem.sh start product-service
scripts/docker-ecosystem.sh restart cloud-gateway
scripts/docker-ecosystem.sh logs order-service
```

For a full working environment, start the platform services first:

```bash
scripts/docker-ecosystem.sh start service-registry config-server redis
```

Use custom local database credentials if needed:

```bash
DB_USER=root DB_PASSWORD=yourpass scripts/docker-ecosystem.sh start
```

Useful commands:

```bash
scripts/docker-ecosystem.sh status
scripts/docker-ecosystem.sh logs
scripts/docker-ecosystem.sh stop
scripts/docker-ecosystem.sh clean
```

`start` and `restart` now recreate containers from a freshly built local image. Use `clean` when you only want to remove old containers/images without starting again.

The Docker services connect to local MySQL through `host.docker.internal:3306` by default. If your MySQL is not reachable from Docker, set `DB_HOST` to a reachable host address or allow MySQL to listen on the Docker bridge interface.

## Running locally with jars

Use `scripts/deploy-ecosystem.sh` to build and run the services directly with `java -jar`.

Service ports:

| Service | Port |
| --- | ---: |
| service-registry | 8761 |
| ConfigServer | 9296 |
| ProductService | 8080 |
| PaymentService | 8081 |
| OrderService | 8082 |
| UserService | 8083 |
| CloudGateway | 9090 |

Start everything:

```bash
scripts/deploy-ecosystem.sh start
```

Start or stop one service without restarting the whole ecosystem:

```bash
scripts/deploy-ecosystem.sh up UserService
scripts/deploy-ecosystem.sh down UserService
```

Other useful local commands:

```bash
scripts/deploy-ecosystem.sh status
scripts/deploy-ecosystem.sh logs
scripts/deploy-ecosystem.sh stop
scripts/deploy-ecosystem.sh clean
```

The script expects MySQL on `localhost:3306` by default and starts Redis with Docker if Redis is not already reachable on `localhost:6379`.

## Logging high level design

The application uses consistent operational logging across CloudGateway, OrderService, ProductService, PaymentService, ConfigServer, and service-registry.

### Goals

- Trace one user request across gateway and downstream services.
- Capture business flow checkpoints such as order creation, quantity reduction, payment creation, and fallback execution.
- Record failures with enough context to debug without logging secrets or full request payloads.
- Keep log format consistent across services so logs can be searched by service, trace ID, span ID, or correlation ID.

### Log flow

1. CloudGateway receives the external request.
2. `GatewayLoggingFilter` reads `X-Correlation-ID` or creates a new UUID.
3. Gateway logs request start and completion, then forwards `X-Correlation-ID` downstream.
4. MVC services use `RequestLoggingFilter` to put `correlationId` in MDC and return the same header in the response.
5. OrderService propagates the same correlation ID through Feign and RestTemplate calls.
6. Service, controller, and exception logs automatically include the same correlation ID through the shared console pattern.

### Log format

Each service config uses this shape:

```text
timestamp level [traceId,spanId] [correlationId=value] [SERVICE-NAME] logger - message
```

Micrometer trace/span IDs are included when tracing creates them. `correlationId` is application-level and is always created at the request boundary if the client does not send one.

### Layer responsibilities

- Gateway filter: log request entry, route completion, failures, and fallback triggers.
- Controller layer: log API intent and successful completion using IDs and safe business fields.
- Service layer: log business decisions, external service calls, state transitions, and persistence success.
- Exception handlers: log domain errors at `WARN` and unexpected failures at `ERROR`.
- Outbound clients: propagate `X-Correlation-ID` and log failed downstream responses.

### Logging rules

- Do not log access tokens, refresh tokens, passwords, client secrets, or authorization headers.
- Do not log full request objects when they may contain sensitive data.
- Prefer structured key-value style, for example `orderId=10 productId=4 status=PLACED`.
- Use `INFO` for normal business checkpoints, `WARN` for expected domain failures, and `ERROR` for unexpected failures.
