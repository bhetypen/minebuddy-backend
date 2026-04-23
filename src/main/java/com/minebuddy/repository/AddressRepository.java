package com.minebuddy.repository;

import com.minebuddy.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findAllByStoreId(UUID storeId);

    Optional<Address> findByAddressIdAndStoreId(UUID addressId, UUID storeId);

    boolean existsByAddressIdAndStoreId(UUID addressId, UUID storeId);

    List<Address> findByStoreIdAndCityContainingIgnoreCaseOrStoreIdAndProvinceContainingIgnoreCaseOrStoreIdAndBarangayContainingIgnoreCaseOrStoreIdAndLine1ContainingIgnoreCase(
            UUID storeId1, String city,
            UUID storeId2, String province,
            UUID storeId3, String barangay,
            UUID storeId4, String line1
    );
}
