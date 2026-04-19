package com.minebuddy.controller;

import com.minebuddy.dto.request.PaymentRequestDTO;
import com.minebuddy.dto.response.PaymentResponseDTO;
import com.minebuddy.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:4200}")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST /api/payments/down-payment
    // Records a down payment on a PREORDER. Returns the new payment record.
    @PostMapping("/down-payment")
    public ResponseEntity<Map<String, Object>> processDownPayment(@Valid @RequestBody PaymentRequestDTO req) {
        PaymentResponseDTO payment = paymentService.processDownPayment(req);
        Map<String, Object> response = new HashMap<>();
        response.put("message", paymentService.getMessage());

        if (payment == null) {
            return ResponseEntity.badRequest().body(response);
        }
        response.put("payment", payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/payments/final-payment
    // Records a final payment. Works for both ONHAND and PREORDER orders.
    @PostMapping("/final-payment")
    public ResponseEntity<Map<String, Object>> processFinalPayment(@Valid @RequestBody PaymentRequestDTO req) {
        PaymentResponseDTO payment = paymentService.processFinalPayment(req);
        Map<String, Object> response = new HashMap<>();
        response.put("message", paymentService.getMessage());

        if (payment == null) {
            return ResponseEntity.badRequest().body(response);
        }
        response.put("payment", payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/payments
    @GetMapping
    public List<PaymentResponseDTO> getAll() {
        return paymentService.getAllPayments();
    }

    // GET /api/payments/by-order/{orderId}
    @GetMapping("/by-order/{orderId}")
    public List<PaymentResponseDTO> getByOrder(@PathVariable UUID orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }

    // GET /api/payments/{paymentId}
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable UUID paymentId) {
        return paymentService.findById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}