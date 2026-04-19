package com.minebuddy.service;

import com.minebuddy.dto.request.AddressRequestDTO;
import com.minebuddy.dto.response.AddressResponseDTO;
import com.minebuddy.model.Address;
import com.minebuddy.repository.AddressRepository;
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

        Address saved = addressRepository.save(address);
        return toResponseDTO(saved);
    }

    public List<AddressResponseDTO> listAll() {
        return addressRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<AddressResponseDTO> searchAddresses(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }

        return addressRepository
                .findByCity(
                        searchTerm, searchTerm, searchTerm, searchTerm
                )
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public boolean existsById(String addressId) {
        return addressRepository.existsById(UUID.fromString(addressId));
    }

    public AddressResponseDTO getAddressById(String addressId) {
        Address address = addressRepository.findById(UUID.fromString(addressId))
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