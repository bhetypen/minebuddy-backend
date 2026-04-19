package com.minebuddy.model.enums;

public enum SaleType {
    ONHAND_ONLY,    // Always must have stock
    PREORDER_ONLY,  // Never has stock, always ordered from supplier
    HYBRID          // Can be sold from stock OR pre-ordered if stock is 0
}
