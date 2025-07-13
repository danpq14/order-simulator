# Order Simulator

A microservices-based order management system with event-driven architecture using Spring Boot, Kafka, and MySQL.

## Architecture

- **Common Library**: Shared library containing common DTOs, enums, exceptions, and constants used by both services
- **Handler Service**: REST API service that manages orders and publishes events to Kafka
- **ETL Service**: Event consumer service that processes order events and stores them in the database
- **MySQL**: Database for storing orders and events
- **Kafka**: Message broker for event-driven communication

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
| Handler Service | 8080 | REST API for order management |
| ETL Service | - | Internal Kafka consumer |
| MySQL | 3306 | Database |
| Kafka | 9092 | Message broker |
| Kafka UI | 8081 | Kafka management interface |
| Zookeeper | 2181 | Kafka coordination |

### API Endpoints

Once the services are running, you can access the Handler Service APIs:

#### Create Order
```bash
curl -X POST http://localhost:8080/orders \
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
curl http://localhost:8080/orders
```

#### Get Order by ID
```bash
curl http://localhost:8080/orders/1
```

#### Cancel Order
```bash
curl -X POST http://localhost:8080/orders/1/cancel
```

#### Simulate Order Execution (Admin)
```bash
curl -X POST http://localhost:8080/orders/simulation-execution
```

### Monitoring

- **Kafka UI**: http://localhost:8081 - Monitor Kafka topics and messages
- **Health Checks**:
  - Handler: http://localhost:8080/actuator/health
  - ETL: http://localhost:8080/actuator/health (internal)

### Database Access

Connect to MySQL:
```bash
# Using Docker
docker-compose exec mysql mysql -u root -proot order_simulator

# Using external client
mysql -h localhost -P 3306 -u root -p root order_simulator
```

### Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker-compose down -v
```

## Development

### Local Development Setup

1. **Start infrastructure only**:
   ```bash
   docker-compose up -d mysql kafka zookeeper kafka-ui
   ```

2. **Run services locally**:
   ```bash
   # Terminal 1 - ETL Service
   ./gradlew :etl:bootRun

   # Terminal 2 - Handler Service
   ./gradlew :handler:bootRun
   ```

### Building Locally

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :common:build
./gradlew :handler:build
./gradlew :etl:build
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :common:test
./gradlew :handler:test
./gradlew :etl:test
```

## Project Structure

```
order-simulator/
├── common/                 # Shared Library
│   ├── src/main/java/
│   │   └── com/example/common/
│   │       ├── dto/        # Shared Data Transfer Objects
│   │       ├── enums/      # Shared Enumerations
│   │       ├── exception/  # Shared Custom Exceptions
│   │       └── constant/   # Shared Constants
│   └── build.gradle
├── handler/                # REST API Service
│   ├── src/main/java/
│   │   └── com/example/handler/
│   │       ├── controller/ # REST Controllers
│   │       ├── service/    # Business Logic
│   │       ├── repository/ # Data Access
│   │       └── model/      # JPA Entities (Order)
│   ├── build.gradle
│   └── Dockerfile
├── etl/                    # Event Consumer Service
│   ├── src/main/java/
│   │   └── com/example/etl/
│   │       ├── service/    # Kafka Consumers
│   │       ├── repository/ # Data Access
│   │       └── model/      # JPA Entities (Event)
│   ├── build.gradle
│   └── Dockerfile
├── docker-compose.yml      # Docker Compose configuration
├── init-database.sql       # Database initialization
└── build.gradle           # Root build configuration
```

## Event Flow

1. **Order Creation**: Handler receives REST request → Creates order → Publishes `ORDER_CREATED` event
2. **Order Update**: Handler updates order → Publishes `ORDER_UPDATED` event
3. **Order Cancellation**: Handler cancels order → Publishes `ORDER_CANCELLED` event
4. **Order Execution**: Handler executes order → Publishes `ORDER_EXECUTED` event
5. **Event Processing**: ETL consumes events → Stores event history in database

## Troubleshooting

### Common Issues

1. **Services not starting**: Check if ports are available and Docker has enough memory
2. **Database connection issues**: Ensure MySQL is fully started before services
3. **Kafka connection issues**: Ensure Kafka and Zookeeper are running

### Useful Commands

```bash
# Restart specific service
docker-compose restart handler-service

# View service logs
docker-compose logs -f etl-service

# Execute command in container
docker-compose exec mysql bash

# Check Kafka topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092
```
