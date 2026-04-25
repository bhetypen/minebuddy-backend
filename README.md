# MineBuddy: Multi-Tenant E-Commerce Platform

MineBuddy is a specialized e-commerce backend and frontend solution designed for social media sellers (e.g., Facebook Live). It features a robust **1-to-1 Multi-Tenancy Architecture**, ensuring complete data isolation between different stores.

## 🚀 Key Architectural Concepts

### 1. The "Logical Wall"
Data isolation is enforced at the database level and the application layer. Every business entity is "stamped" with a `store_id`.
*   **Zero-Knowledge Frontend:** The frontend never handles raw Store UUIDs. The identity is securely stored within the encrypted JWT.
*   **Automatic Context:** A `ThreadLocal` TenantContext manages the store identity for every request lifecycle.

---

## 📦 Orders: The Core Engine (Full CRUD)

The Order module is the heart of MineBuddy, designed for the high-velocity world of Facebook Live selling. It features a sophisticated state machine and automated stock management.

**See [ORDERS.md](readme/ORDERS.md) for a deep dive into the business logic and flowcharts.**

### 🚀 High-Impact Features
- **Smart Split Hybrid Logic:** Automatically handles orders that are partially in-stock and partially pre-order, splitting them into separate tracks while pro-rating shipping fees.
- **Workflow Guardrails:** A rank-based state machine prevents out-of-order operations (e.g., shipping without tracking or balance).
- **Stock Integrity:** Automatic stock restoration on order cancellation or edits.

---

## 📊 Identity & Security Diagrams

### A. Registration & Onboarding
```mermaid
sequenceDiagram
    participant U as New Seller
    participant A as Angular App
    participant C as AuthController
    participant S as AuthService
    participant DB as MariaDB

    U->>A: Enter Name, Email, Store Name
    A->>C: POST /api/auth/register
    C->>S: register(RegisterRequestDTO)
    Note over S: Transaction Start
    S->>DB: INSERT INTO stores (name)
    DB-->>S: Return store_id
    S->>S: Hash Password
    S->>DB: INSERT INTO users (email, store_id, active=false)
    Note over S: Transaction Commit
    S-->>C: Return UserResponseDTO
    C-->>A: 201 Created
    A->>U: Show "Pending Activation" Toast
```

### B. Secure Request Handling (The Logical Wall)
```mermaid
sequenceDiagram
    participant U as Authenticated Seller
    participant F as JwtAuthFilter
    participant TX as TenantContext
    participant S as ItemService
    participant R as ItemRepository
    participant DB as MariaDB

    U->>F: GET /api/items (Authorization: Bearer JWT)
    F->>F: Extract storeId from JWT Claims
    F->>TX: setStoreId(UUID)
    F->>S: listAll()
    S->>TX: getStoreId()
    S->>R: findAllByStoreId(storeId)
    R->>DB: SELECT * FROM items WHERE store_id = ?
    DB-->>R: Return Store's Items Only
    R-->>S: List<Item>
    S-->>U: JSON Response (Filtered)
    F->>TX: clear()
```

---

## 🏗 Class Diagram

```mermaid
classDiagram
    class User {
        +UUID userId
        +String email
        +boolean active
        +Store store
    }

    class Store {
        +UUID storeId
        +String name
    }

    class Order {
        +UUID orderId
        +OrderStatus status
        +BigDecimal totalAmount
        +BigDecimal balance
        +recalculateTotals()
        +addDownPayment()
    }

    class Item {
        +UUID itemId
        +SaleType saleType
        +int stock
        +decreaseStock()
    }

    User "1" -- "1" Store : Owns
    Store "1" -- "*" Item : Owns
    Store "1" -- "*" Order : Processes
    Store "1" -- "*" Customer : Manages
    
    Order "1" -- "*" Payment : Has
    Order "1" -- "1" Shipment : Requires
```

---

## 🛠 Technology Stack

### Backend (Spring Boot 4.0.3)
- **Security:** JWT with Refresh Token Rotation & BCrypt.
- **Persistence:** Hibernate 6 (JPA) with MariaDB.
- **Migrations:** Flyway (Versioned SQL).
- **Isolation:** Custom `TenantContext` + JPA `@PrePersist` hooks.

### Frontend (Angular 19+)
- **State:** Signals-based architecture.
- **Security:** Functional HTTP Interceptors.
- **UI:** Tailwind CSS with dynamic Admin/Super-Admin views.
