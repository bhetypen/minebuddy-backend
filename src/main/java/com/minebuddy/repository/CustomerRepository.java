package com.minebuddy.repository;

import com.minebuddy.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByStoreId(UUID storeId);

    Optional<Customer> findByCustomerIdAndStoreId(UUID customerId, UUID storeId);

    boolean existsByCustomerIdAndStoreId(UUID customerId, UUID storeId);

    @Query("""
        SELECT c FROM Customer c
        WHERE c.storeId = :storeId
          AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(c.handle)    LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(c.phone)     LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(CAST(c.customerId AS string)) LIKE LOWER(CONCAT('%', :term, '%')))
        """)
    List<Customer> search(@Param("storeId") UUID storeId, @Param("term") String term);

    long countByStoreId(UUID storeId);
}
