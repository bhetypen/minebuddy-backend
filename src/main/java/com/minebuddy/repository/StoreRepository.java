package com.minebuddy.repository;

import com.minebuddy.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    // For verifying store names during the "Happy Path" creation
   Optional<Store> findByName(String name);

        // For future use to check if a store is active
    boolean existsByName(String name);

}
