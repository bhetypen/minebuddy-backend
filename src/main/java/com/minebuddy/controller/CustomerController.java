package com.minebuddy.controller;

import com.minebuddy.dto.request.CustomerRequestDTO;
import com.minebuddy.dto.response.CustomerResponseDTO;
import com.minebuddy.model.Customer;
import com.minebuddy.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    // ----- Register a new customer -----
    @PostMapping
    public CustomerResponseDTO registerCustomer(@RequestBody CustomerRequestDTO dto) {
        Customer c = service.registerCustomer(dto);
        return toResponseDTO(c);
    }

    // ----- Get all customers -----
    @GetMapping
    public List<CustomerResponseDTO> getAll() {
        return service.listAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ----- Search customers -----
    @GetMapping("/search")
    public List<CustomerResponseDTO> searchCustomers(@RequestParam String q) {
        return service.searchCustomers(q)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ----- Check if customer exists -----
    @GetMapping("/{customerId}/exists")
    public boolean customerExists(@PathVariable UUID customerId) {
        return service.existsById(customerId);
    }

    // ----- Find customer by ID -----
    @GetMapping("/{customerId}")
    public CustomerResponseDTO findById(@PathVariable UUID customerId) {
        Customer c = service.findById(customerId);
        return c != null ? toResponseDTO(c) : null;
    }

    // ----- Convert Customer → DTO -----
    private CustomerResponseDTO toResponseDTO(Customer c) {
        return new CustomerResponseDTO(
                c.getCustomerId(),
                c.getFullName(),
                c.getHandle(),
                c.getPlatform(),
                c.getPhoneNumber(),
                c.getAddressId()
        );
    }
}