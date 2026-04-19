package com.minebuddy.controller;

import com.minebuddy.dto.response.ShippingLabelDTO;
import com.minebuddy.service.ShippingLabelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipping-labels")
public class ShippingLabelController {

    private final ShippingLabelService service;

    public ShippingLabelController(ShippingLabelService service) {
        this.service = service;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ShippingLabelDTO> getLabel(@PathVariable UUID orderId) {
        return service.createLabel(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public List<ShippingLabelDTO> getPendingLabels() {
        return service.getPendingLabels();
    }

    @GetMapping("/ready-to-ship-count")
    public Map<String, Long> getReadyToShipCount() {
        return Map.of("count", service.getReadyToShipCount());
    }
}
