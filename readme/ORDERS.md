# MineBuddy: Order Management & Business Logic

The Order module is the most complex part of the MineBuddy ecosystem. It is designed to handle the high-volume, high-trust environment of Facebook Live selling, where items move quickly and payment is often split between deposits and final balances.

## 🏁 The Grand Lifecycle: Logic-Driven Fulfillment

This diagram reflects the exact technical implementation in the Java backend, specifically how the **Item SaleType** dictates the order's lifecycle track.

```mermaid
flowchart TD
    %% Phase 1: Security & Multitenancy
    subgraph Security_Phase ["1. Secure Entry (The Logical Wall)"]
        A[Login] --> B[JWT contains store_id]
        B --> C[API Call]
        C --> D[JwtAuthFilter sets TenantContext]
    end

    %% Phase 2: Order Creation (The 3 Sale Types)
    subgraph Order_Logic ["2. Order Engine (SaleType Branching)"]
        D --> E[createOrder]
        E --> F{Item.saleType?}

        %% ONHAND Path
        F -- ONHAND_ONLY --> G{Stock >= Qty?}
        G -- No --> H[Error: Sold Out]
        G -- Yes --> I[Deduct Stock & Status: RESERVED]

        %% PREORDER Path
        F -- PREORDER_ONLY --> J[Force PaymentType: PREORDER & Status: RESERVED]

        %% HYBRID Path
        F -- HYBRID --> K{Stock >= Qty?}
        K -- Yes --> I
        K -- No --> L{Stock > 0?}
        L -- Yes --> M[Split: Create Stock Order + Create Pre-Order]
        L -- No --> J
    end

    %% Phase 3: Lifecycle Paths
    subgraph Lifecycle_Paths ["3. Lifecycle Tracks"]
        I --> N{Path: On-Hand Fast Track}
        J & M --> O{Path: Pre-Order Supply Chain}

        %% On-Hand Path
        N --> P[Status: FULLY_PAID - Rank 6]
        P --> Q[Status: PACKED - Rank 7]

        %% Pre-Order Path
        O --> R[Status: DP_PAID - Rank 2]
        R --> S[Status: FOR_ORDERING - Rank 3]
        S --> T[Status: ORDERED_FROM_SUPPLIER - Rank 4]
        T --> U[Status: ARRIVED - Rank 5]
        U --> P
    end

    %% Phase 4: Final Fulfillment
    subgraph Fulfillment ["4. Final Fulfillment"]
        Q --> V[Status: SHIPPED - Rank 8]
        V --> W([Status: COMPLETED - Rank 9])
        
        %% Workflow Guards
        V -- "Guard" --> V1{Tracking # & Balance == 0}
        W -- "Guard" --> W1{Shipment == DELIVERED}
    end

    %% Styling
    style Order_Logic fill:#fff9c4,stroke:#fbc02d
    style Lifecycle_Paths fill:#e1f5fe,stroke:#01579b
    style Fulfillment fill:#e8f5e9,stroke:#2e7d32
```

---

## 🧠 Business Logic Deep Dive

### 1. The "Smart Split" Hybrid Fulfillment
When a customer orders a quantity that exceeds current stock for a **HYBRID** item, the `OrderService` automatically splits the request:
- **Order A**: Fulfilled from current stock (immediate deduction).
- **Order B**: Placed as a pre-order for the remaining quantity.
- **Shipping**: The shipping fee is pro-rated between both orders to prevent double-billing.

### 2. Logic-Driven Tracks
The system intelligently determines which steps are required based on the **Item SaleType**:
- **On-Hand Track**: Skips supplier-related statuses (Ordering, Arrived) and moves directly from Payment to Packing.
- **Pre-Order Track**: Enforces a strict supply-chain sequence: `DP_PAID` ➡️ `FOR_ORDERING` ➡️ `ORDERED` ➡️ `ARRIVED`.

### 3. State Machine & Rank Constraints
To maintain financial and logistical integrity, orders follow a strict rank-based progression:

| Status | Rank | Logical Requirement |
| :--- | :---: | :--- |
| **RESERVED** | 1 | Initial state. Inventory is committed. |
| **DP_PAID** | 2 | Verified by `totalPaid >= dpRequired`. |
| **FULLY_PAID** | 6 | Balance must be exactly zero. |
| **SHIPPED** | 8 | Requires **Tracking Number** AND **Zero Balance**. |
| **COMPLETED** | 9 | Requires Shipment record to be marked as **DELIVERED**. |

---

## 🛡 Security & Integrity
- **Multi-Tenant Isolation**: Every transaction is protected by the **TenantContext**, ensuring store-level data isolation.
- **Automatic Stock Restoration**: When an order is cancelled or edited, the `Item` inventory is automatically restored.
- **Financial Protection**: Orders with paid deposits are locked from simple cancellation to protect financial history.
