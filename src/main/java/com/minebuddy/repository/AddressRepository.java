package com.minebuddy.repository;

import com.minebuddy.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByCity(
            String city,
            String province,
            String barangay,
            String line1
    );
}
