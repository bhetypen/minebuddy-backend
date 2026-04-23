package com.minebuddy.service;

import com.minebuddy.dto.request.AddressRequestDTO;
import com.minebuddy.dto.response.AddressResponseDTO;
import com.minebuddy.model.Address;
import com.minebuddy.repository.AddressRepository;
import com.minebuddy.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public AddressResponseDTO createAddress(AddressRequestDTO dto) {
        Address address = new Address();
        address.setLine1(dto.line1());
        address.setLine2(dto.line2());
        address.setBarangay(dto.barangay());
        address.setCity(dto.city());
        address.setProvince(dto.province());
        address.setRegion(dto.region());
        address.setZip(dto.zip());
        address.setLandmark(dto.landmark());
        // storeId is set automatically via @PrePersist

        Address saved = addressRepository.save(address);
        return toResponseDTO(saved);
    }

    public List<AddressResponseDTO> listAll() {
        UUID storeId = TenantContext.getStoreId();
        return addressRepository.findAllByStoreId(storeId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<AddressResponseDTO> searchAddresses(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }

        UUID storeId = TenantContext.getStoreId();
        return addressRepository
                .findByStoreIdAndCityContainingIgnoreCaseOrStoreIdAndProvinceContainingIgnoreCaseOrStoreIdAndBarangayContainingIgnoreCaseOrStoreIdAndLine1ContainingIgnoreCase(
                        storeId, searchTerm, storeId, searchTerm, storeId, searchTerm, storeId, searchTerm
                )
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public boolean existsById(String addressId) {
        UUID storeId = TenantContext.getStoreId();
        return addressRepository.existsByAddressIdAndStoreId(UUID.fromString(addressId), storeId);
    }

    public AddressResponseDTO getAddressById(String addressId) {
        UUID storeId = TenantContext.getStoreId();
        Address address = addressRepository.findByAddressIdAndStoreId(UUID.fromString(addressId), storeId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));

        return toResponseDTO(address);
    }

    private AddressResponseDTO toResponseDTO(Address a) {
        StringBuilder summary = new StringBuilder();
        summary.append(a.getLine1());

        if (a.getLine2() != null && !a.getLine2().isBlank()) {
            summary.append(", ").append(a.getLine2());
        }

        summary.append(", ").append(a.getBarangay());
        summary.append(", ").append(a.getCity());
        summary.append(", ").append(a.getProvince());

        return new AddressResponseDTO(
                a.getAddressId().toString(),
                summary.toString()
        );
    }
}