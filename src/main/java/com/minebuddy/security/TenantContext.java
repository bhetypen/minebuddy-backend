package com.minebuddy.security;

import java.util.UUID;

/**
 * Holder for the current store context (multi-tenancy).
 * Uses ThreadLocal to ensure the storeId is isolated to the current request.
 */
public class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_STORE_ID = new ThreadLocal<>();

    public static void setStoreId(UUID storeId) {
        CURRENT_STORE_ID.set(storeId);
    }

    public static UUID getStoreId() {
        return CURRENT_STORE_ID.get();
    }

    public static void clear() {
        CURRENT_STORE_ID.remove();
    }
}
