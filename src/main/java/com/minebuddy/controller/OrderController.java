package com.minebuddy.controller;

import com.minebuddy.dto.request.OrderRequestDTO;
import com.minebuddy.dto.response.OrderResponseDTO;
import com.minebuddy.dto.OrderSummaryDTO;
import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.service.OrderService;
import com.minebuddy.service.OrderService.OrderResult;
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
        OrderResult result = service.createOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("order", result.order(), "message", result.message()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> edit(@PathVariable UUID id, @RequestBody EditOrderPayload payload) {
        String message = service.editOrder(id, payload.itemId(), payload.quantity(), payload.shippingFee());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestBody StatusPayload payload) {
        String message = service.updateStatus(id, payload.status());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID id) {
        String message = service.cancelOrder(id);
        return ResponseEntity.ok(Map.of("message", message));
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
