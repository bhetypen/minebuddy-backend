package com.minebuddy.service;

import com.minebuddy.dto.request.PaymentRequestDTO;
import com.minebuddy.dto.response.PaymentResponseDTO;
import com.minebuddy.model.Order;
import com.minebuddy.model.Payment;
import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.model.enums.PaymentType;
import com.minebuddy.repository.OrderRepository;
import com.minebuddy.repository.PaymentRepository;
import com.minebuddy.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;

    private String message;

    public PaymentService(OrderRepository orderRepo, PaymentRepository paymentRepo) {
        this.orderRepo = orderRepo;
        this.paymentRepo = paymentRepo;
    }

    public String getMessage() {
        return message;
    }

    // ---- PROCESS DOWN PAYMENT ----
    // PREORDER only. Transitions RESERVED -> DP_PAID once dp requirement is met.
    @Transactional
    public PaymentResponseDTO processDownPayment(PaymentRequestDTO req) {
        UUID storeId = TenantContext.getStoreId();
        Order order = orderRepo.findByOrderIdAndStoreId(req.orderId(), storeId).orElse(null);
        if (order == null) {
            this.message = "Order not found.";
            return null;
        }

        if (order.getPaymentType() != PaymentType.PREORDER) {
            this.message = "This is an ONHAND order. Use final payment instead.";
            return null;
        }

        if (isTerminal(order.getStatus())) {
            this.message = "Order is " + order.getStatus() + " and cannot accept payments.";
            return null;
        }

        // Overpayment guard: dp + existing payments must not exceed the total
        if (wouldOverpay(order, req.amount())) {
            this.message = String.format(
                    "Payment (₱%s) would exceed remaining balance (₱%s).",
                    req.amount(), order.getBalance()
            );
            return null;
        }

        // Financial update via domain method (handles recalculateBalance internally)
        order.addDownPayment(req.amount());

        // Status transition: only move forward from RESERVED once dp requirement met
        if (order.getStatus() == OrderStatus.RESERVED
                && order.getDpPaid().compareTo(order.getDpRequired()) >= 0) {
            order.setStatus(OrderStatus.DP_PAID);
        }

        Payment saved = persistTransaction(req);
        orderRepo.save(order);

        this.message = "Down payment recorded.";
        return toResponseDTO(saved);
    }

    // ---- PROCESS FINAL PAYMENT ----
    // ONHAND or PREORDER. Transitions to FULLY_PAID once balance hits zero.
    @Transactional
    public PaymentResponseDTO processFinalPayment(PaymentRequestDTO req) {
        UUID storeId = TenantContext.getStoreId();
        Order order = orderRepo.findByOrderIdAndStoreId(req.orderId(), storeId).orElse(null);
        if (order == null) {
            this.message = "Order not found.";
            return null;
        }

        if (isTerminal(order.getStatus())) {
            this.message = "Order is " + order.getStatus() + " and cannot accept payments.";
            return null;
        }

        if (order.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            this.message = "Order is already fully paid.";
            return null;
        }

        if (wouldOverpay(order, req.amount())) {
            this.message = String.format(
                    "Payment (₱%s) exceeds remaining balance (₱%s).",
                    req.amount(), order.getBalance()
            );
            return null;
        }

        // Financial update via domain method
        order.addFinalPayment(req.amount());

        if (order.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            // Only update status to FULLY_PAID if it's currently at an earlier stage.
            // This prevents moving backwards from PACKED or SHIPPED.
            if (rank(order.getStatus()) < rank(OrderStatus.FULLY_PAID)) {
                order.setStatus(OrderStatus.FULLY_PAID);
            }
        }

        Payment saved = persistTransaction(req);
        orderRepo.save(order);

        this.message = "Final payment recorded.";
        return toResponseDTO(saved);
    }

    private int rank(OrderStatus s) {
        return switch (s) {
            case RESERVED -> 1;
            case DP_PAID -> 2;
            case FOR_ORDERING -> 3;
            case ORDERED_FROM_SUPPLIER -> 4;
            case ARRIVED -> 5;
            case FULLY_PAID -> 6;
            case PACKED -> 7;
            case SHIPPED -> 8;
            case COMPLETED -> 9;
            case CANCELLED -> 99;
        };
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByOrderId(UUID orderId) {
        UUID storeId = TenantContext.getStoreId();
        return paymentRepo.findByOrderIdAndStoreId(orderId, storeId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        UUID storeId = TenantContext.getStoreId();
        return paymentRepo.findAllByStoreId(storeId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponseDTO> findById(UUID paymentId) {
        UUID storeId = TenantContext.getStoreId();
        return paymentRepo.findByPaymentIdAndStoreId(paymentId, storeId).map(this::toResponseDTO);
    }

    // ---- Helpers ----

    private boolean isTerminal(OrderStatus status) {
        return status == OrderStatus.CANCELLED || status == OrderStatus.COMPLETED;
    }

    private boolean wouldOverpay(Order order, BigDecimal proposedPayment) {
        return proposedPayment.compareTo(order.getBalance()) > 0;
    }

    private Payment persistTransaction(PaymentRequestDTO req) {
        Payment payment = new Payment(
                req.orderId(),
                req.amount(),
                req.method(),
                sanitize(req.reference()),
                sanitize(req.receiptUrl())
        );
        return paymentRepo.save(payment);
    }

    private String sanitize(String input) {
        return (input == null || input.isBlank()) ? null : input.trim();
    }

    private PaymentResponseDTO toResponseDTO(Payment p) {
        return new PaymentResponseDTO(
                p.getPaymentId().toString(),
                p.getOrderId().toString(),
                p.getAmount(),
                p.getPaymentMethod(),
                p.getPaymentReference(),
                p.getReceiptUrl(),
                p.getPaymentDate(),
                p.getPaymentUpdatedDate()
        );
    }
}