# Order Simulator

A microservices-based order management system with event-driven architecture using Spring Boot, Kafka, and MySQL. The system provides secure REST APIs for order management with comprehensive error handling, retry mechanisms, and dead letter queue support.

## Architecture

- **Common Library**: Shared library containing common DTOs, enums, exceptions, and constants used by both services
- **Handler Service**: Secure REST API service that manages orders and publishes events to Kafka
- **ETL Service**: Event consumer service that processes order events and stores them in the database with retry and DLQ support
- **MySQL**: Database for storing orders and events
- **Kafka**: Message broker for event-driven communication with dead letter queue support

## Features

- 🔐 **Security**: Basic authentication with hardcoded admin credentials
- 🔄 **Event-Driven Architecture**: Asynchronous order processing via Kafka
- 🛡️ **Error Handling**: Comprehensive error responses with proper HTTP status codes
- 🔁 **Retry Mechanism**: Automatic retry with Dead Letter Queue for failed messages
- 📊 **Monitoring**: Health checks and Kafka UI for system monitoring
- 🐳 **Docker Support**: Full containerization with Docker Compose

## Quick Start with Docker Compose

### Prerequisites

- Docker and Docker Compose installed
- At least 4GB of available RAM

### Running the Application

1. **Clone the repository and navigate to the project root**

2. **Start all services**:
   ```bash
   docker-compose up -d
   ```

3. **Check service status**:
   ```bash
   docker-compose ps
   ```

4. **View logs**:
   ```bash
   # All services
   docker-compose logs -f

   # Specific service
   docker-compose logs -f handler-service
   docker-compose logs -f etl-service
   ```

### Services and Ports

| Service | Port | Description |
|---------|------|-------------|
| Handler Service | 8080 | Secure REST API for order management |
| ETL Service | - | Internal Kafka consumer with DLQ support |
| MySQL | 3306 | Database |
| Kafka | 9092 | Message broker |
| Kafka UI | 8081 | Kafka management interface |
| Zookeeper | 2181 | Kafka coordination |

## API Security

The Handler Service is secured with **HTTP Basic Authentication**. All endpoints (except `/login` and health checks) require authentication.

### Default Credentials
- **Username**: `admin`
- **Password**: `admin`

### Authentication Methods

#### 1. Login API
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

**Response:**
```json
{
  "message": "Login successful",
  "username": "admin",
  "success": true
}
```

#### 2. Basic Authentication
Include credentials in request headers for all protected endpoints:
```bash
curl -u admin:admin http://localhost:8080/orders
```

### Public Endpoints
- `POST /login` - Authentication endpoint
- `GET /actuator/health` - Health check endpoint

### Protected Endpoints
All other endpoints require authentication:
- Order management APIs
- Order simulation APIs

## API Endpoints

### Authentication

#### Login
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

### Order Management (Requires Authentication)

#### Create Order
```bash
curl -u admin:admin -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "VCB",
    "quantity": 15,
    "price": 50000.00,
    "side": "BUY"
  }'
```

#### Get All Orders
```bash
curl -u admin:admin http://localhost:8080/orders
```

#### Get Order by ID
```bash
curl -u admin:admin http://localhost:8080/orders/1
```

#### Cancel Order
```bash
curl -u admin:admin -X POST http://localhost:8080/orders/1/cancel
```

#### Simulate Order Execution (Admin)
```bash
curl -u admin:admin -X POST http://localhost:8080/orders/simulation-execution
```

## Testing with Postman

For easier API testing, you can import our pre-configured Postman collection:

**Postman Collection**: [Order Simulator APIs](https://web.postman.co/workspace/My-Workspace~b992ba0a-628f-40f9-8da9-51f5f3d0c6ae/collection/39128812-7705dff2-4c70-4e2b-a649-d4be540016c8?action=share&source=copy-link&creator=39128812)

The collection includes:
- Pre-configured authentication (admin/admin)
- All order management endpoints
- Login API example
- Error handling scenarios
- Sample request bodies

**How to use:**
1. Click the collection link above
2. Import to your Postman workspace
3. Update the `baseUrl` variable if needed (default: `http://localhost:8080`)
4. Start testing the APIs!

## Error Handling

The API provides comprehensive error handling with appropriate HTTP status codes:

### HTTP Status Codes
- **200 OK**: Successful requests
- **400 Bad Request**: Invalid request data or order state
- **401 Unauthorized**: Missing or invalid credentials
- **404 Not Found**: Order not found or non-existent endpoint
- **405 Method Not Allowed**: Unsupported HTTP method
- **500 Internal Server Error**: Server-side errors

### Error Response Format
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 999",
  "timestamp": "2025-01-13T15:30:45.123Z"
}
```

### Validation Errors
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "validationErrors": {
    "symbol": "must not be blank",
    "quantity": "must be positive"
  },
  "timestamp": "2025-01-13T15:30:45.123Z"
}
```

## Event Processing & Reliability

### Event Flow
1. **Order Creation**: Handler receives REST request → Creates order → Publishes `ORDER_CREATED` event
2. **Order Cancellation**: Handler cancels order → Publishes `ORDER_CANCELLED` event
3. **Order Execution**: Handler executes order → Publishes `ORDER_EXECUTED` event
4. **Order Failure**: Handler fails order → Publishes `ORDER_FAILED` event
5. **Event Processing**: ETL consumes events → Stores event history in database

### Dead Letter Queue (DLQ) Support
- **Automatic Retry**: Failed messages are retried up to 3 times
- **DLQ Processing**: Messages exceeding retry limit are sent to `order-events-dlq` topic
- **Error Monitoring**: DLQ consumer logs failed messages for analysis
- **Manual Acknowledgment**: Prevents message loss during processing failures

### Kafka Topics
- `order-events`: Main topic for order events
- `order-events-dlq`: Dead letter queue for failed messages

## Monitoring

### Health Checks
- **Handler**: http://localhost:8080/actuator/health
- **ETL**: Internal health monitoring via logs

### Kafka Management
- **Kafka UI**: http://localhost:8081 - Monitor topics, messages, and consumer groups

### Useful Commands
```bash
# Check Kafka topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# View DLQ messages
docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic order-events-dlq --from-beginning

# Monitor ETL service logs
docker-compose logs -f etl-service
```

## Database Access

Connect to MySQL:
```bash
# Using Docker
docker-compose exec mysql mysql -u root -proot order_simulator

# Using external client
mysql -h localhost -P 3306 -u root -p root order_simulator
```

### Database Tables
- **orders**: Order data (symbol, quantity, price, status, side)
- **events**: Event history (order_id, event_type, event_data, created_at)

## Project Structure

```
order-simulator/
├── common/                 # Shared Library
│   ├── src/main/java/
│   │   └── com/example/common/
│   │       ├── dto/        # Shared Data Transfer Objects
│   │       ├── enums/      # Shared Enumerations (OrderStatus, EventType, OrderSide)
│   │       ├── exception/  # Shared Custom Exceptions
│   │       └── constant/   # Shared Constants (Kafka topics)
│   └── build.gradle
├── handler/                # REST API Service
│   ├── src/main/java/
│   │   └── com/example/handler/
│   │       ├── controller/ # REST Controllers (Order, Auth, Global Exception Handler)
│   │       ├── service/    # Business Logic (Order, Event Publisher)
│   │       ├── repository/ # Data Access (Order Repository)
│   │       ├── model/      # JPA Entities and DTOs
│   │       └── config/     # Security and Kafka Configuration
│   ├── build.gradle
│   └── Dockerfile
├── etl/                    # Event Consumer Service
│   ├── src/main/java/
│   │   └── com/example/etl/
│   │       ├── service/    # Kafka Consumers (Event, DLQ)
│   │       ├── repository/ # Data Access (Event Repository)
│   │       ├── model/      # JPA Entities
│   │       └── config/     # Kafka Configuration with DLQ support
│   ├── build.gradle
│   └── Dockerfile
├── docker-compose.yml      # Docker Compose configuration
├── init-database.sql       # Database initialization
└── build.gradle           # Root build configuration
```

## Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker-compose down -v
```

## Building from Source

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :common:build
./gradlew :handler:build
./gradlew :etl:build
```