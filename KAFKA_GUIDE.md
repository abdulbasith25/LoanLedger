# Kafka Integration Guide - Loan Ledger System

This document explains the Event-Driven Architecture (EDA) implemented in this project using Apache Kafka.

## 1. Architecture Overview
The system uses a **Producer-Consumer** pattern to process ledger transactions asynchronously.

`User Action` -> `LedgerService (Producer)` -> `Kafka (ledger-topic)` -> `LedgerConsumer (Consumer)`

## 2. Component Breakdown

### A. Infrastructure (Docker)
We use `docker-compose.yml` to run:
* **Zookeeper**: Manages Kafka cluster state.
* **Kafka Broker**: The central nervous system where messages are stored and distributed.
* **PostgreSQL**: The primary database for persistent storage.

### B. The Event (Data Transfer Object)
`LedgerCreatedEvent.java`: An immutable DTO containing:
* `ledgerId`: The ID from the database.
* `userId`: Who made the transaction.
* `amount`: The value of the transaction.
* `type`: DEBIT or CREDIT.

### C. The Producer (`LedgerService.java`)
1. **Persistence**: Saves the transaction to the database first.
2. **Internal Event**: Publishes a Spring `ApplicationEvent`.
3. **Kafka Bridge**: An `@EventListener` marked with `@Async` sends the message to Kafka using `KafkaTemplate`.
   * *Why Async?* To ensure the main web request finishes instantly without waiting for the network call to Kafka.

### D. The Consumer (`LedgerConsumer.java`)
Uses `@KafkaListener` to monitor the `ledger-topic`.
* **Current Logic**: Logs transaction details and triggers a "High Value Alert" for transactions exceeding 10,000.
* **Real-world use**: This is where you would trigger emails, push notifications, or fraud detection algorithms.

## 3. Configuration (`application.properties`)
Key properties configured:
* `bootstrap-servers`: Points to the Kafka broker (`localhost:9092`).
* `JsonSerializer/Deserializer`: Automatically converts Java objects to/from JSON strings.
* `trusted.packages`: Allows Kafka to safely recreate the `LedgerCreatedEvent` class.

## 4. Key Benefits
1. **Durability**: If the consumer is down, messages stay in Kafka and are processed once the consumer comes back online.
2. **Scalability**: You can run 10 instances of the Consumer to handle heavy traffic without changing the Producer code.
3. **Decoupling**: The Ledger recording logic is completely separate from the Alert/Notification logic.

---
*Created for learning purposes - Abdul Basith's Loan Ledger Project*
