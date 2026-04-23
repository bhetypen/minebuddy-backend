package com.minebuddy.service;

import com.minebuddy.dto.request.CustomerRequestDTO;
import com.minebuddy.model.Customer;
import com.minebuddy.repository.CustomerRepository;
import com.minebuddy.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Customer registerCustomer(CustomerRequestDTO dto) {
        Customer customer = new Customer(
                dto.fName(),
                dto.lName(),
                dto.handle(),
                dto.platform(),
                dto.phone(),
                dto.addressId(),
                null // Set by @PrePersist
        );

        return repo.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> listAll() {
        UUID storeId = TenantContext.getStoreId();
        return repo.findAllByStoreId(storeId);
    }

    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }

        UUID storeId = TenantContext.getStoreId();
        return repo.search(storeId, searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID customerId) {
        UUID storeId = TenantContext.getStoreId();
        return repo.existsByCustomerIdAndStoreId(customerId, storeId);
    }

    @Transactional(readOnly = true)
    public Customer findById(UUID customerId) {
        UUID storeId = TenantContext.getStoreId();
        return repo.findByCustomerIdAndStoreId(customerId, storeId).orElse(null);
    }
}
