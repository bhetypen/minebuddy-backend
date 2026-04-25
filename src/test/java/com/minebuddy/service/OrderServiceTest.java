package com.minebuddy.service;

import com.minebuddy.dto.request.OrderRequestDTO;
import com.minebuddy.dto.response.OrderResponseDTO;
import com.minebuddy.model.Customer;
import com.minebuddy.model.Item;
import com.minebuddy.model.Order;
import com.minebuddy.model.Shipment;
import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.model.enums.PaymentType;
import com.minebuddy.model.enums.SaleType;
import com.minebuddy.repository.CustomerRepository;
import com.minebuddy.repository.ItemRepository;
import com.minebuddy.repository.OrderRepository;
import com.minebuddy.repository.ShipmentRepository;
import com.minebuddy.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;
    @Mock
    private CustomerRepository customerRepo;
    @Mock
    private ItemRepository itemRepo;
    @Mock
    private ShipmentRepository shipmentRepo;

    @InjectMocks
    private OrderService orderService;

    private UUID storeId;
    private UUID customerId;
    private UUID itemId;
    private Item onhandItem;
    private Item preorderItem;
    private Item hybridItem;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        TenantContext.setStoreId(storeId);

        onhandItem = new Item("Onhand Item", "Cat", new BigDecimal("100.00"), new BigDecimal("70.00"), 10, SaleType.ONHAND_ONLY);
        preorderItem = new Item("Preorder Item", "Cat", new BigDecimal("100.00"), new BigDecimal("70.00"), 0, SaleType.PREORDER_ONLY);
        hybridItem = new Item("Hybrid Item", "Cat", new BigDecimal("100.00"), new BigDecimal("70.00"), 5, SaleType.HYBRID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // --- CREATE ORDER TESTS ---

    @Test
    void createOrder_CustomerNotFound_ShouldReturnNull() {
        OrderRequestDTO request = new OrderRequestDTO(customerId, itemId, 1, PaymentType.ONHAND, BigDecimal.ZERO, BigDecimal.ZERO);
        when(customerRepo.existsByCustomerIdAndStoreId(customerId, storeId)).thenReturn(false);

        OrderResponseDTO response = orderService.createOrder(request);

        assertNull(response);
        assertEquals("Customer not found.", orderService.getMessage());
    }

    @Test
    void createOrder_ItemNotFound_ShouldReturnNull() {
        OrderRequestDTO request = new OrderRequestDTO(customerId, itemId, 1, PaymentType.ONHAND, BigDecimal.ZERO, BigDecimal.ZERO);
        when(customerRepo.existsByCustomerIdAndStoreId(customerId, storeId)).thenReturn(true);
        when(itemRepo.findByItemIdAndStoreId(itemId, storeId)).thenReturn(Optional.empty());

        OrderResponseDTO response = orderService.createOrder(request);

        assertNull(response);
        assertEquals("Item not found or inactive.", orderService.getMessage());
    }

    @Test
    void createOrder_Onhand_Success() {
        OrderRequestDTO request = new OrderRequestDTO(customerId, itemId, 2, PaymentType.ONHAND, BigDecimal.ZERO, new BigDecimal("50.00"));
        when(customerRepo.existsByCustomerIdAndStoreId(customerId, storeId)).thenReturn(true);
        when(itemRepo.findByItemIdAndStoreId(itemId, storeId)).thenReturn(Optional.of(onhandItem));
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(8, onhandItem.getStock());
        assertEquals(OrderStatus.RESERVED, response.status());
        verify(itemRepo).save(onhandItem);
    }

    @Test
    void createOrder_Onhand_SoldOut_ShouldReturnNull() {
        OrderRequestDTO request = new OrderRequestDTO(customerId, itemId, 11, PaymentType.ONHAND, BigDecimal.ZERO, BigDecimal.ZERO);
        when(customerRepo.existsByCustomerIdAndStoreId(customerId, storeId)).thenReturn(true);
        when(itemRepo.findByItemIdAndStoreId(itemId, storeId)).thenReturn(Optional.of(onhandItem));

        OrderResponseDTO response = orderService.createOrder(request);

        assertNull(response);
        assertEquals("Sold out. This item is only available from on-hand stock.", orderService.getMessage());
    }

    @Test
    void createOrder_Preorder_Success() {
        OrderRequestDTO request = new OrderRequestDTO(customerId, itemId, 1, PaymentType.PREORDER, new BigDecimal("30.00"), new BigDecimal("50.00"));
        when(customerRepo.existsByCustomerIdAndStoreId(customerId, storeId)).thenReturn(true);
        when(itemRepo.findByItemIdAndStoreId(itemId, storeId)).thenReturn(Optional.of(preorderItem));
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(0, preorderItem.getStock());
        assertEquals(PaymentType.PREORDER, response.paymentType());
    }

    @Test
    void createOrder_Hybrid_SplitOrder_Success() {
        // Hybrid item has 5 stock. Requesting 8.
        OrderRequestDTO request = new OrderRequestDTO(customerId, itemId, 8, PaymentType.ONHAND, BigDecimal.ZERO, new BigDecimal("80.00"));
        when(customerRepo.existsByCustomerIdAndStoreId(customerId, storeId)).thenReturn(true);
        when(itemRepo.findByItemIdAndStoreId(itemId, storeId)).thenReturn(Optional.of(hybridItem));
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(0, hybridItem.getStock());
        verify(orderRepo, times(2)).save(any(Order.class));
        assertTrue(orderService.getMessage().contains("Order split: 5 from stock"));
    }

    // --- UPDATE STATUS TESTS ---

    @Test
    void updateStatus_SequenceError_BackwardMove_ShouldReturnFalse() {
        Order order = new Order(customerId, itemId, 1, PaymentType.ONHAND, new BigDecimal("100"), null, new BigDecimal("100"), null, null);
        order.setStatus(OrderStatus.ARRIVED);
        when(orderRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(order));

        boolean result = orderService.updateStatus(order.getOrderId(), OrderStatus.RESERVED);

        assertFalse(result);
        assertEquals("Sequence Error: Cannot move backward from ARRIVED", orderService.getMessage());
    }

    @Test
    void updateStatus_DPNotPaid_ShouldReturnFalse() {
        Order order = new Order(customerId, itemId, 1, PaymentType.PREORDER, new BigDecimal("100"), null, new BigDecimal("100"), null, new BigDecimal("30"));
        order.setStatus(OrderStatus.RESERVED);
        // dpPaid is 0 by default. Order.getTotalPaid() returns 0.
        when(orderRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(order));
        when(itemRepo.findByItemIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(preorderItem));

        boolean result = orderService.updateStatus(order.getOrderId(), OrderStatus.DP_PAID);

        assertFalse(result);
        // Fixed expected string to match BigDecimal formatting in error message
        assertTrue(orderService.getMessage().contains("Financial Error: Total paid (P0) does not meet DP requirement"), 
            "Actual message: " + orderService.getMessage());
    }

    @Test
    void updateStatus_ShippedWithoutTracking_ShouldReturnFalse() {
        Order order = new Order(customerId, itemId, 1, PaymentType.ONHAND, new BigDecimal("100"), null, new BigDecimal("100"), null, null);
        order.setStatus(OrderStatus.PACKED);
        order.setDpPaid(new BigDecimal("100")); // Balance is 0
        when(orderRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(order));
        when(itemRepo.findByItemIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(onhandItem));
        when(shipmentRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.empty());

        boolean result = orderService.updateStatus(order.getOrderId(), OrderStatus.SHIPPED);

        assertFalse(result);
        assertEquals("Logistics Error: Cannot set to SHIPPED without a Tracking Number.", orderService.getMessage());
    }

    @Test
    void updateStatus_ShippedWithBalance_ShouldReturnFalse() {
        Order order = new Order(customerId, itemId, 1, PaymentType.ONHAND, new BigDecimal("100"), null, new BigDecimal("100"), null, null);
        order.setStatus(OrderStatus.PACKED);
        // Balance is 100. Let's provide tracking first so it hits the balance check.
        when(orderRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(order));
        when(itemRepo.findByItemIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(onhandItem));
        
        Shipment shipment = new Shipment(order.getOrderId(), "Carrier", "TRK123", BigDecimal.ZERO);
        when(shipmentRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(shipment));

        boolean result = orderService.updateStatus(order.getOrderId(), OrderStatus.SHIPPED);

        assertFalse(result);
        assertEquals("Financial Error: Balance must be zero before shipping.", orderService.getMessage());
    }

    @Test
    void updateStatus_CompleteWithoutDeliveredShipment_ShouldReturnFalse() {
        Order order = new Order(customerId, itemId, 1, PaymentType.ONHAND, new BigDecimal("100"), null, new BigDecimal("100"), null, null);
        order.setStatus(OrderStatus.SHIPPED);
        order.setDpPaid(new BigDecimal("100")); // Zero balance
        when(orderRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(order));
        when(itemRepo.findByItemIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(onhandItem));
        
        Shipment shipment = new Shipment(order.getOrderId(), "Carrier", "TRK123", BigDecimal.ZERO);
        shipment.setShipmentStatus("SHIPPED");
        when(shipmentRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(shipment));

        boolean result = orderService.updateStatus(order.getOrderId(), OrderStatus.COMPLETED);

        assertFalse(result);
        // The service adds a formatting string for the status
        assertTrue(orderService.getMessage().contains("Logistics Error: Cannot complete order. Shipment is currently 'SHIPPED'"),
            "Actual message: " + orderService.getMessage());
    }

    // --- CANCEL ORDER TESTS ---

    @Test
    void cancelOrder_DepositPaid_ShouldReturnFalse() {
        Order order = new Order(customerId, itemId, 1, PaymentType.PREORDER, new BigDecimal("100"), null, new BigDecimal("100"), null, new BigDecimal("30"));
        order.setDpPaid(new BigDecimal("30"));
        when(orderRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(order));

        boolean result = orderService.cancelOrder(order.getOrderId());

        assertFalse(result);
        assertEquals("Cannot cancel: Deposit already paid.", orderService.getMessage());
    }

    @Test
    void cancelOrder_Success_ShouldRestoreStock() {
        Order order = new Order(customerId, itemId, 2, PaymentType.ONHAND, new BigDecimal("100"), null, new BigDecimal("200"), null, null);
        when(orderRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(order));
        when(itemRepo.findByItemIdAndStoreId(itemId, storeId)).thenReturn(Optional.of(onhandItem));

        boolean result = orderService.cancelOrder(order.getOrderId());

        assertTrue(result);
        assertEquals(12, onhandItem.getStock());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(itemRepo).save(onhandItem);
        verify(orderRepo).save(order);
    }

    // --- EDIT ORDER TESTS ---

    @Test
    void editOrder_InsufficientStock_ShouldReturnFalse() {
        Order order = new Order(customerId, itemId, 1, PaymentType.ONHAND, new BigDecimal("100"), null, new BigDecimal("100"), null, null);
        when(orderRepo.findByOrderIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(order));
        
        Item newItem = new Item("New Item", "Cat", new BigDecimal("150.00"), new BigDecimal("100.00"), 2, SaleType.ONHAND_ONLY);
        when(itemRepo.findByItemIdAndStoreId(any(), eq(storeId))).thenReturn(Optional.of(newItem));

        boolean result = orderService.editOrder(order.getOrderId(), UUID.randomUUID(), 5, null);

        assertFalse(result);
        assertEquals("Insufficient stock for strict on-hand item.", orderService.getMessage());
    }
}
