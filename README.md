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

## Related to OKTA Configuration
First create new application in okta . Application type should be open id application.
Authroization server has configuration related to access token and refresh token validity. 

Go to security → API → default audience to modify access policies and timing.

To get refresh token , we need to ensure we have grant type refresh token for application and also scope is offline_access provided when retriving tokens. Refer application yaml of cloud gateway.

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

Sleuth trace/span IDs are included when Sleuth creates them. `correlationId` is application-level and is always created at the request boundary if the client does not send one.

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
