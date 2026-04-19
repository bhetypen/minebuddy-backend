package com.minebuddy.controller;

import com.minebuddy.dto.request.AddressRequestDTO;
import com.minebuddy.dto.response.AddressResponseDTO;
import com.minebuddy.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @PostMapping
    public AddressResponseDTO createAddress(@RequestBody AddressRequestDTO dto) {
        return service.createAddress(dto);
    }

    @GetMapping
    public List<AddressResponseDTO> getAll() {
        return service.listAll();
    }

    @GetMapping("/search")
    public List<AddressResponseDTO> searchAddresses(@RequestParam String q) {
        return service.searchAddresses(q);
    }

    @GetMapping("/{addressId}")
    public AddressResponseDTO findById(@PathVariable String addressId) {
        return service.getAddressById(addressId);
    }

    @GetMapping("/{addressId}/exists")
    public boolean addressExists(@PathVariable String addressId) {
        return service.existsById(addressId);
    }
}