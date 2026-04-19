package com.minebuddy.controller;

import com.minebuddy.dto.request.CreateShipmentRequestDTO;
import com.minebuddy.dto.request.UpdateShipmentStatusRequestDTO;
import com.minebuddy.dto.response.ShipmentResponseDTO;
import com.minebuddy.service.ShippingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService service;

    public ShippingController(ShippingService service) {
        this.service = service;
    }

    @GetMapping
    public List<ShipmentResponseDTO> getAll() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> findById(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<ShipmentResponseDTO> findByOrderId(@PathVariable UUID orderId) {
        return service.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-carrier")
    public List<ShipmentResponseDTO> findByCarrier(@RequestParam String carrier) {
        return service.findByCarrier(carrier);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateShipmentRequestDTO dto) {
        ShipmentResponseDTO shipment = service.createShipment(dto);
        if (shipment == null) {
            return ResponseEntity.badRequest().body(Map.of("message", service.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("shipment", shipment, "message", service.getMessage()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateShipmentStatusRequestDTO dto) {
        boolean ok = service.updateShipmentStatus(id, dto);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", service.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", service.getMessage()));
    }

    @PostMapping("/{id}/delivered")
    public ResponseEntity<?> markDelivered(@PathVariable UUID id) {
        boolean ok = service.markDelivered(id);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", service.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", service.getMessage()));
    }
}
