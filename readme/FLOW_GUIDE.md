# 🗺️ MineBuddy: Deep-Dive Technical Execution Flow

This guide traces every layer of the MineBuddy architecture: **DTOs** (Data Transfer), **Services** (Orchestration), **Models** (Domain Logic), and **Repositories** (Persistence).

---

## 1. User Registration & Store Onboarding
*Goal: Create a multi-tenant identity.*

1.  **Request**: Client sends `RegisterRequestDTO`.
2.  **Service**: `AuthService.register(dto)`
    - Maps `dto.storeName()` to a new `Store` **Model**.
    - Calls `StoreRepository.save(store)`.
    - Maps `dto.email()`, `dto.password()`, and `dto.name()` to a new `User` **Model**.
    - Links `User` model to the saved `Store`.
    - Calls `UserRepository.save(user)`.
3.  **Response**: Converts `User` model into `UserResponseDTO` (with `active = false`).

---

## 2. Authentication & The "Logical Wall"
*Goal: Establish the TenantContext.*

1.  **Request**: Client sends `LoginRequestDTO`.
2.  **Service**: `AuthService.login(dto)`
    - Calls `UserRepository.findByEmail()` to get the `User` **Model**.
    - **Logic**: `User` model verifies password hash and `isActive` status.
    - Generates JWT containing `store_id` claim.
3.  **Response**: Returns `LoginResponseDTO` containing the Access Token.
4.  **Security Layer**: On every next request, `JwtAuthFilter` extracts `store_id` and calls `TenantContext.setStoreId()`.

---

## 3. The Order Engine: Hybrid Fulfillment
*Goal: Process complex stock logic.*

1.  **Request**: Client sends `OrderRequestDTO`.
2.  **Service**: `OrderService.createOrder(dto)`
    - Calls `ItemRepository.findByItemIdAndStoreId()` to fetch the `Item` **Model**.
    - **Domain Logic**: 
        - If `Item.saleType` is `HYBRID`, Service calculates stock vs. requested quantity.
        - Calls `Item.decreaseStock(qty)` to update internal state.
    - Instantiates `Order` **Model** using data from `Item` and `OrderRequestDTO`.
    - Calls `ItemRepository.save(item)` and `OrderRepository.save(order)`.
3.  **Response**: Converts `Order` model into `OrderResponseDTO`.

---

## 4. Payment: Financial Recalculation
*Goal: Trigger state changes via financial milestones.*

1.  **Request**: Client sends `PaymentRequestDTO`.
2.  **Service**: `PaymentService.processFinalPayment(dto)`
    - Calls `OrderRepository.findByOrderIdAndStoreId()` to get `Order` **Model**.
    - **Domain Logic**: 
        - Calls `Order.addFinalPayment(amount)`.
        - **Order Model** internally triggers `recalculateBalance()`: `totalAmount - dpPaid - finalPaid`.
        - If balance is 0, Service updates `Order.status` to `FULLY_PAID`.
    - Instantiates `Payment` **Model** using `PaymentRequestDTO`.
    - Calls `PaymentRepository.save(payment)` and `OrderRepository.save(order)`.
3.  **Response**: Converts `Payment` model into `PaymentResponseDTO`.

---

## 5. Shipping & Completion: The Final Guardrail
*Goal: Enforce "No COD" policy.*

1.  **Request**: Client sends `CreateShipmentRequestDTO`.
2.  **Service**: `ShippingService.createShipment(dto)`
    - Calls `OrderRepository` for the `Order` **Model**.
    - **Guard**: Service checks `Order.getBalance()`. If `> 0`, execution stops (Security Exception).
    - If `0`, instantiates `Shipment` **Model**.
    - Updates `Order.status` to `PACKED`.
    - Calls `ShipmentRepository.save(shipment)` and `OrderRepository.save(order)`.
3.  **Trigger (Delivery)**: `POST /api/shipping/{id}/delivered`
    - Calls `ShipmentRepository` to get `Shipment` **Model**.
    - Sets `Shipment.shipmentStatus` to `DELIVERED`.
    - **Cascade**: Calls `OrderRepository` to set `Order.status` to `COMPLETED`.
4.  **Response**: Final status is returned via `ShipmentResponseDTO`.

---

## 🏗️ Layer Summary
| Layer | Responsibility |
| :--- | :--- |
| **DTO** | Data validation and transport (JSON mapping). |
| **Controller** | Entry points; handles HTTP status codes. |
| **Service** | Orchestrates transactions; enforces multitenancy. |
| **Model** | Holds the data and performs core calculations (e.g. `recalculateBalance`). |
| **Repository** | SQL generation; enforces the "Logical Wall" in the WHERE clause. |

---

## 🛡️ Core Business Rule Summary (The Seller Protection System)

To ensure the highest level of security and financial protection for the seller, MineBuddy enforces these hard-coded rules:

1.  **The Logical Wall**: Data isolation is enforced at the JPA layer. A seller can only see and modify records that match their `store_id`.
2.  **No COD Policy**: Fulfillment is locked until payment is settled. The system blocks moving an order to `PACKED` or `SHIPPED` if `order.balance > 0`.
3.  **Transit Lock**: To prevent losses, an order cannot be cancelled once it reaches the `SHIPPED` status (Rank 8).
4.  **Smart Split**: The system automatically detects stock shortages for `HYBRID` items and splits the transaction into two managed tracks (In-stock vs. Pre-order).
5.  **Workflow Rank Integrity**: Prevents "skipping" steps. For example, a pre-order must be marked as `ARRIVED` in the warehouse before it is allowed to be `PACKED`.
6.  **Auto-Restoration**: Cancelling an order (pre-transit) automatically restores the committed stock back to the `Item` inventory.
