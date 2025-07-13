#!/bin/bash

echo "Stopping services..."
docker-compose down

echo "Removing Kafka data volumes..."
docker volume rm order-simulator_kafka_data order-simulator_zookeeper_data 2>/dev/null || true

echo "Starting infrastructure services..."
docker-compose up -d mysql kafka zookeeper kafka-ui

echo "Waiting for Kafka to be ready..."
sleep 30

echo "Creating topic with proper configuration..."
docker-compose exec kafka kafka-topics --create --topic order-events --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --if-not-exists

echo "Starting application services..."
docker-compose up -d handler-service etl-service

echo "Done! Kafka has been reset and services are starting up."