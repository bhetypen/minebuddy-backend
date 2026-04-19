package com.minebuddy.service;

import com.minebuddy.dto.request.CustomerRequestDTO;
import com.minebuddy.model.Customer;
import com.minebuddy.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public Customer registerCustomer(CustomerRequestDTO dto) {
        Customer customer = new Customer(
                dto.fName(),
                dto.lName(),
                dto.handle(),
                dto.platform(),
                dto.phone(),
                dto.addressId(),
                LocalDateTime.now()
        );

        return repo.save(customer);
    }

    public List<Customer> listAll() {
        return repo.findAll();
    }

    //Search Customer by name, handle, ID or phone number
    public List<Customer> searchCustomers(String searchTerm) {
        if(searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }

        String lower = searchTerm.toLowerCase().trim();

        return repo.findAll().stream()
                .filter(c -> c.getFirstName().toLowerCase().contains(lower)
                        || c.getLastName().toLowerCase().contains(lower)
                        || c.getFullName().toLowerCase().contains(lower)
                        || c.getHandle().toLowerCase().contains(lower)
                        || (c.getCustomerId() != null && c.getCustomerId().toString().toLowerCase().contains(lower))
                        || c.getPhoneNumber().toLowerCase().contains(searchTerm))
                .toList();
    }

    //check if customer exists by ID
    public boolean existsById(UUID customerId) {
        return repo.existsById(customerId);
    }

    //Find a single customer by ID
    public Customer findById(UUID customerId) {
        return repo.findById(customerId).orElse(null);
    }




}
