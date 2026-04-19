package com.minebuddy.service;

import com.minebuddy.dto.response.ShippingLabelDTO;
import com.minebuddy.model.Address;
import com.minebuddy.model.Customer;
import com.minebuddy.model.Item;
import com.minebuddy.model.Order;
import com.minebuddy.model.enums.OrderStatus;
import com.minebuddy.repository.AddressRepository;
import com.minebuddy.repository.CustomerRepository;
import com.minebuddy.repository.ItemRepository;
import com.minebuddy.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShippingLabelService {

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final AddressRepository addressRepo;
    private final ItemRepository itemRepo;

    public ShippingLabelService(OrderRepository orderRepo,
                                CustomerRepository customerRepo,
                                AddressRepository addressRepo,
                                ItemRepository itemRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.addressRepo = addressRepo;
        this.itemRepo = itemRepo;
    }

    @Transactional(readOnly = true)
    public Optional<ShippingLabelDTO> createLabel(UUID orderId) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return Optional.empty();

        Customer customer = customerRepo.findById(order.getCustomerId()).orElse(null);
        if (customer == null) return Optional.empty();

        Address address = addressRepo.findById(customer.getAddressId()).orElse(null);
        Item item = itemRepo.findById(order.getItemId()).orElse(null);

        String customerName = customer.getFirstName() + " " + customer.getLastName();
        String fullAddress = formatAddress(address);
        String landmark = (address != null) ? address.getLandmark() : "N/A";
        String itemName = (item != null) ? item.getName() : "Unknown Item";

        return Optional.of(new ShippingLabelDTO(
                order.getOrderId().toString(),
                order.getItemId().toString(),
                customerName,
                customer.getPhoneNumber(),
                fullAddress,
                landmark,
                itemName,
                order.getQuantity(),
                order.getBalance(),
                order.getStatus().name()
        ));
    }

    @Transactional(readOnly = true)
    public List<ShippingLabelDTO> getPendingLabels() {
        return orderRepo.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.FULLY_PAID
                          || o.getStatus() == OrderStatus.PACKED)
                .map(o -> createLabel(o.getOrderId()).orElse(null))
                .filter(label -> label != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getReadyToShipCount() {
        return orderRepo.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.FULLY_PAID
                          || o.getStatus() == OrderStatus.PACKED)
                .count();
    }

    private String formatAddress(Address address) {
        if (address == null) return "No Address Registered";

        StringBuilder sb = new StringBuilder();
        sb.append(address.getLine1());

        if (address.getLine2() != null && !address.getLine2().isBlank()) {
            sb.append(", ").append(address.getLine2());
        }

        sb.append(", ").append(address.getBarangay())
          .append(", ").append(address.getCity())
          .append(", ").append(address.getProvince())
          .append(" ").append(address.getZip());

        return sb.toString();
    }
}
