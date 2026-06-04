# NotificationService

Dedicated Spring Boot 3 MVC notification service for event-driven, multi-channel messaging.

## Architecture

```text
OrderService / PaymentService / ProductService / UserService
        |
        | publish NotificationEvent
        v
RabbitMQ notification.exchange
        |
        v
NotificationService
        |
        |-- NotificationEventConsumer
        |-- NotificationOrchestrator
        |-- PreferenceService
        |-- TemplateService + HandlebarsTemplateRenderer
        |-- DeliveryLogService
        |-- NotificationProvider implementations
        |
        +--> SendGrid email
        +--> In-app MySQL storage
        +--> Webhook callbacks
        +--> SMS/push placeholder providers
```

## Event Contract

```json
{
  "eventId": "evt_123",
  "eventType": "order_created",
  "userId": "user_101",
  "data": {
    "customerName": "Damu",
    "email": "damu@example.com",
    "orderId": "ORD-9001",
    "totalAmount": 2499
  },
  "occurredAt": "2026-06-04T10:15:00Z"
}
```

## Main Endpoints

```text
POST   /api/admin/notification/templates
GET    /api/admin/notification/templates
GET    /api/admin/notification/templates/{id}
PUT    /api/admin/notification/templates/{id}
POST   /api/admin/notification/templates/{id}/activate

GET    /api/admin/notification/preferences/{userId}
PUT    /api/admin/notification/preferences/{userId}

GET    /api/admin/notification/logs/event/{eventId}
GET    /api/admin/notification/logs/user/{userId}

POST   /internal/notifications/events
POST   /internal/notifications/send

GET    /api/notifications/in-app?userId={userId}
PATCH  /api/notifications/in-app/{id}/read
```

## Gateway Route

The API gateway exposes this service under:

```text
/notification/**
```

Swagger is aggregated at:

```text
/notification/v3/api-docs
```

## Reliability Notes

- Delivery logs use `eventId + userId + channel` as an idempotency key.
- RabbitMQ uses a durable queue and dead-letter queue.
- Provider logic is isolated behind `NotificationProvider`.
- SendGrid is disabled safely when `SENDGRID_API_KEY` is empty, which keeps local development usable.
