package com.minebuddy.controller;

import com.minebuddy.dto.request.OrderRequestDTO;
import com.minebuddy.dto.response.OrderResponseDTO;
import com.minebuddy.dto.OrderSummaryDTO;
import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderResponseDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/summaries")
    public List<OrderSummaryDTO> getAllSummaries() {
        return service.getAllOrderSummaries();
    }

    @GetMapping("/search")
    public List<OrderResponseDTO> search(@RequestParam("q") String query) {
        return service.searchOrders(query);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<OrderSummaryDTO> findSummaryById(@PathVariable UUID id) {
        return service.findSummaryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrderRequestDTO dto) {
        OrderResponseDTO order = service.createOrder(dto);
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of("message", service.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("order", order, "message", service.getMessage()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> edit(@PathVariable UUID id, @RequestBody EditOrderPayload payload) {
        boolean ok = service.editOrder(
                id,
                payload.itemId(),
                payload.quantity(),
                payload.shippingFee()
        );
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", service.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", service.getMessage()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestBody StatusPayload payload) {
        boolean ok = service.updateStatus(id, payload.status());
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", service.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", service.getMessage()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID id) {
        boolean ok = service.cancelOrder(id);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", service.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", service.getMessage()));
    }

    @PostMapping("/batch-arrival/{itemId}")
    public ResponseEntity<?> batchArrival(@PathVariable UUID itemId) {
        int count = service.processBatchArrival(itemId);
        return ResponseEntity.ok(Map.of(
                "message", "Processed " + count + " orders to ARRIVED.",
                "count", count
        ));
    }

    public record EditOrderPayload(UUID itemId, int quantity, BigDecimal shippingFee) {}
    public record StatusPayload(OrderStatus status) {}
}