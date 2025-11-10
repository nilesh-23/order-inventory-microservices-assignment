# Order-Inventory Microservices Assignment

This project implements two Spring Boot microservices — **Inventory Service** and **Order Service** — which communicate with each other using REST APIs.
It demonstrates inter-service communication, modular architecture, and extensibility through the Factory Design Pattern.
Both services use **Spring Data JPA** with **H2 in-memory databases** and are fully tested using **JUnit 5** and **Mockito**.

---

## Project Setup Instructions

### Prerequisites

* Java 17+
* Maven 3.9+
* Git installed
* (Optional) Postman for API testing

---

### 1. Clone the Repository

```bash
git clone https://github.com/nilesh-23/order-inventory-microservices-assignment.git
cd order-inventory-microservices-assignment
```

---

### 2. Run Inventory Service


cd inventory-service
mvn spring-boot:run

* Service runs at: **[http://localhost:8081](http://localhost:8081)**


### 3. Run Order Service

```bash
cd ../order-service
mvn spring-boot:run
```

* Service runs at: **[http://localhost:8080](http://localhost:8080)**
* H2 console available at: **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**

    * JDBC URL: `jdbc:h2:mem:orderdb`

Both services start independently and communicate via REST — Order Service sends requests to Inventory Service on port `8081`.

---

## API Documentation

### Inventory Service (Port 8081)

#### 1. `GET /inventory/{productId}`

Fetches all batches of a given product sorted by expiry date.

**Example Request**

```
GET http://localhost:8081/inventory/100
```

**Example Response**

```json
[
  {
    "id": 2,
    "productId": 100,
    "batchNumber": "BATCH-100-B",
    "quantity": 30,
    "expiryDate": "2025-09-01"
  },
  {
    "id": 1,
    "productId": 100,
    "batchNumber": "BATCH-100-A",
    "quantity": 50,
    "expiryDate": "2025-12-01"
  }
]
```

---

#### 2. `POST /inventory/update`

Updates product stock after an order is placed.

**Example Request**

```json
{
  "productId": 100,
  "quantity": 5
}
```

**Example Success Response**

```json
{
  "status": "success"
}
```

**Example Error Response**

```json
{
  "status": "error",
  "message": "Insufficient stock for product 100"
}
```

---

### Order Service (Port 8080)

#### 1. `POST /order`

Places an order and updates inventory through REST communication.

**Example Request**

```json
{
  "productId": 100,
  "quantity": 5
}
```

**Example Success Response**

```json
{
  "id": 1,
  "productId": 100,
  "quantity": 5,
  "status": "COMPLETED"
}
```

**Example Failure Response (Insufficient stock or inventory service down)**

```json
{
  "id": 2,
  "productId": 100,
  "quantity": 9999,
  "status": "FAILED"
}
```

---

## Testing Instructions

Both microservices contain **unit** and **integration** tests.

### Inventory Service Tests

**Run:**

```bash
cd inventory-service
mvn test
```

**Includes:**

* `InventoryControllerTest` → Integration test using `@SpringBootTest` and `TestRestTemplate`
* `InventoryServiceTest` → Unit test for service logic using Mockito
* Uses H2 in-memory DB for full workflow testing

---

### Order Service Tests

**Run:**

```bash
cd ../order-service
mvn test
```

**Includes:**

* `OrderServiceTest` → Unit test for order processing using mocked `RestTemplate` and repository
* `OrderControllerTest` → Integration test using `MockRestServiceServer` and `TestRestTemplate`

All tests are configured with:

* **JUnit 5** for assertions
* **Mockito** for mocking external calls
* **Spring Boot Test** for integration testing

If all tests pass, you’ll see:

```
BUILD SUCCESS
```

---

## Summary

* **Java 17**, **Spring Boot 3**, **Maven**, **H2 Database**
* Two independent microservices communicating via REST
* Implements Factory Design Pattern for extensibility
* Includes both unit and integration tests

---

**Author:** Nilesh Kumar
GitHub: [@nilesh-23](https://github.com/nilesh-23)
