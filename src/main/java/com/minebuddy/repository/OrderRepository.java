package com.minebuddy.repository;

import com.minebuddy.model.Order;
import com.minebuddy.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByStoreId(UUID storeId);

    List<Order> findByItemIdAndStatusAndStoreId(UUID itemId, OrderStatus status, UUID storeId);

    java.util.Optional<Order> findByOrderIdAndStoreId(UUID orderId, UUID storeId);

    boolean existsByOrderIdAndStoreId(UUID orderId, UUID storeId);

    @Query("""
        SELECT o FROM Order o
        WHERE o.storeId = :storeId
          AND (LOWER(CAST(o.orderId AS string)) LIKE LOWER(CONCAT('%', :term, '%'))
           OR EXISTS (
               SELECT 1 FROM Customer c
               WHERE c.customerId = o.customerId
                 AND c.storeId = :storeId
                 AND LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :term, '%'))
           )
           OR EXISTS (
               SELECT 1 FROM Item i
               WHERE i.itemId = o.itemId
                 AND i.storeId = :storeId
                 AND LOWER(i.name) LIKE LOWER(CONCAT('%', :term, '%'))
           ))
        """)
    List<Order> search(@Param("storeId") UUID storeId, @Param("term") String term);
}
